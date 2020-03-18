package com.futureelectronics.osso;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.bluetooth.BluetoothGattService;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futureelectronics.osso.bluetooth.BleController;
import com.futureelectronics.osso.bluetooth.OssoConnection;
import com.futureelectronics.osso.data.Osso;
import com.futureelectronics.osso.data.ScannedDevice;
import com.futureelectronics.osso.databinding.ScanFragmentBinding;

import com.google.android.material.snackbar.Snackbar;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;

public class ScanFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OssoConnection.ConnectListener{

    private final static String TAG = ScanFragment.class.getSimpleName();

    private ScanViewModel mViewModel;
    private ScanFragmentBinding mBinding;
    private ScannedAdapter mScannedAdapter;

    private OssoListViewModel mListViewModel;

    private Disposable scanDisposable;
    private Handler mHandler = new Handler();

    private final static int SCAN_TIME_MS = 10000;

    private RxBleDevice bleDevice;
    private Disposable stateDisposable;
    private OssoConnection mOssoConnection;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = ScanFragmentBinding.inflate(inflater, container, false);
        mBinding.setIsListEmpty(true);

        mScannedAdapter = new ScannedAdapter(mScannedDevClickCallback);
        mBinding.scanList.setAdapter(mScannedAdapter);
        mBinding.scanList.setItemAnimator(null);
        mBinding.swipeContainer.setOnRefreshListener(this);

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mLeScanStopper);
                stopScanning();
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (isScanning()) {
            /*
             * Stop scanning in onPause callback.
             */
            scanDisposable.dispose();
        }
        if(stateDisposable != null && !stateDisposable.isDisposed()){
            stateDisposable.dispose();
        }
        stateDisposable = null;
        if(mBinding.getIsConnecting() && mOssoConnection != null){
            if(mOssoConnection.connDisposable != null && !mOssoConnection.connDisposable.isDisposed()){
                mOssoConnection.connDisposable.dispose();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mScannedAdapter.getItemCount() == 0){
            startScanning();
        }
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void stopScanning()
    {
        if (isScanning()) {
            scanDisposable.dispose();
            mBinding.setIsScanning(false);
        }
    }

    private void startScanning()
    {
        BleController.getInstance().disconnectAll();
        if (LocationPermission.checkLocationPermissionGranted(getContext())) {
            scanBleDevices();
            mBinding.setIsScanning(true);
        } else {
            LocationPermission.requestLocationPermission(getActivity());
        }
    }

    private Runnable mLeScanStopper = () -> {
        stopScanning();
        mBinding.setIsListEmpty(mScannedAdapter.getItemCount() == 0);
    };

    private void scanBleDevices() {
        scanDisposable = BleController.getInstance().getClient().scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
//                            .setDeviceAddress("B4:99:4C:34:DC:8B")
                        .setDeviceName("BlueNRG")
                        // add custom filters if needed
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(scanResult -> {
                    mScannedAdapter.addScanResult(scanResult);
                    mBinding.setIsListEmpty(mScannedAdapter.getItemCount() == 0);
                }, this::onScanFailure);

        mHandler.postDelayed(mLeScanStopper, SCAN_TIME_MS);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ScanViewModel.class);

        mListViewModel = ViewModelProviders.of(this).get(OssoListViewModel.class);
        mListViewModel.getAllOssos().observe(this, (myOssos) -> {
            if(myOssos != null){
                ArrayList<String> addrList = new ArrayList<>();
                for(Osso osso : myOssos){
                    addrList.add(osso.address);
                }
                mScannedAdapter.setCurrentOssoAddrList(addrList);
            }
            else {
                mScannedAdapter.setCurrentOssoAddrList(null);
            }
        });
    }

    private void dispose() {
        scanDisposable = null;
    }

    public void onRefresh(){
        if(isScanning()){
            mHandler.removeCallbacks(mLeScanStopper);
            scanDisposable.dispose();
            scanDisposable = null;
        }
        mScannedAdapter.clearScanResults();
        startScanning();
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            AppLog.e(TAG, "Scan failure! "+throwable);
//            Snackbar.make(mBinding.coordinatorLayout, "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
            Snackbar.make(mBinding.coordinatorLayout, mBinding.getRoot().getResources().getString(R.string.msg_snackbar_err, throwable.getMessage()), Snackbar.LENGTH_LONG).show();
            stopScanning();
        }
    }

    private final ScannedDevClickCallback mScannedDevClickCallback = new ScannedDevClickCallback() {
        @Override
        public void onClick(String ossoAddress) {
            if(!mBinding.getIsConnecting()) {
                boolean wasScanning = mBinding.getIsScanning();
                stopScanning();
                mBinding.setIsConnecting(true);

                if(wasScanning) {
                    new Handler().postDelayed(() -> {
                        connectDevice(ossoAddress);
                    }, 500);
                }
                else{
                    connectDevice(ossoAddress);
                }

            }
        }
    };

    private void connectDevice(String address)
    {
        bleDevice = BleController.getInstance().getClient().getBleDevice(address);

        stateDisposable = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
        mOssoConnection = OssoConnection.establishConnection(bleDevice, this);
        Snackbar.make(mBinding.coordinatorLayout, R.string.msg_connecting, Snackbar.LENGTH_INDEFINITE).show();
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        AppLog.d(TAG, "Connection State: "+newState.toString());
    }

    public void onConnectionFailure(OssoConnection ossoConnection, Throwable throwable) {
        AppLog.d(TAG, "Connection error: "+throwable);
        try {
//            Snackbar.make(mBinding.coordinatorLayout, "Error: " + throwable.getMessage(), Snackbar.LENGTH_LONG).show();
            Snackbar.make(mBinding.coordinatorLayout, mBinding.getRoot().getResources().getString(R.string.msg_snackbar_err, throwable.getMessage()), Snackbar.LENGTH_LONG).show();
        }catch (Exception e){}
        if(ossoConnection != null) {
            ossoConnection.bleConnection = null;
            if (ossoConnection.connDisposable != null && !ossoConnection.connDisposable.isDisposed()) {
                ossoConnection.connDisposable.dispose();
            }
            ossoConnection.connDisposable = null;
        }

        if(stateDisposable != null && !stateDisposable.isDisposed()){
            stateDisposable.dispose();
        }
        stateDisposable = null;
    }

    public void onConnectedAndReady(OssoConnection ossoConnection) {
        if(ossoConnection != null) {
            mOssoConnection = ossoConnection;
            onOssoConnected(ossoConnection.bleDevice.getMacAddress());
        }
    }

    private void onOssoConnected(String address)
    {
        ScannedDevice device = mScannedAdapter.getScannedDevice(address);
        if(device == null){
            return;
        }
        Osso osso = new Osso();
        osso.address = device.getAddress();
        osso.petName = device.getName();
        osso.rssi = device.getRssi();

        mViewModel.insertOsso(osso);

        mBinding.setIsConnecting(false);

        BleController.getInstance().addConnection(address, mOssoConnection);

        ScanFragmentDirections.ActionScanFragmentToOssoDetailsFragment navAction = ScanFragmentDirections.actionScanFragmentToOssoDetailsFragment(osso.address, OssoDetailsFragment.START_TAB_SETTINGS);

        Navigation.findNavController(getActivity(), R.id.scan_list).navigate(navAction);
    }
}
