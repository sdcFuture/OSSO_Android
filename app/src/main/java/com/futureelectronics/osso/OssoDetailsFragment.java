package com.futureelectronics.osso;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futureelectronics.osso.bluetooth.BleController;
import com.futureelectronics.osso.bluetooth.OssoConnection;
import com.futureelectronics.osso.data.Osso;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.futureelectronics.osso.databinding.FragmentOssoDetailsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class OssoDetailsFragment extends Fragment implements OssoConnection.ConnectListener {
    private final static String TAG = OssoDetailsFragment.class.getSimpleName();

    public static final String ARG_OSSO_ID = "osso_id";
    public static final String ARG_OSSO_ADDRESS = "osso_address";
    public static final String ARG_START_TAB = "start_tab";
    public static final String START_TAB_DASHBOARD = "dashboard";
    public static final String START_TAB_LOCATION = "location";
    public static final String START_TAB_SETTINGS = "settings";

    private OssoDataViewModel mViewModel;

    private BleController mBleController;
    private OssoConnection mConnection;
    private Osso mOsso;
    private Handler mHandler = new Handler();
    private boolean ossoValuesChanged = false;
    private Disposable readDisposable;
    private Disposable stateDisposable;

    private boolean mOssoUpdateStarted = false;

    private FragmentOssoDetailsBinding mBinding;

    public OssoDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(OssoDataViewModel.class);
        if (getArguments() != null) {
            OssoDetailsFragmentArgs args = OssoDetailsFragmentArgs.fromBundle(getArguments());
            mViewModel.selectOsso(args.getOssoAddress());
        }

        if(mBinding != null && mBinding.nestedBottomNavigation != null){
            final NavController nestedNavController = Navigation.findNavController(mBinding.getRoot().findViewById(R.id.nested_nav_host_fragment));

            if(getArguments()!=null)
            {
                String startTab = OssoDetailsFragmentArgs.fromBundle(getArguments()).getStartTab();
                if(startTab == START_TAB_SETTINGS){
                    NavOptions.Builder navOptBuilder = new NavOptions.Builder().setLaunchSingleTop(true);
                    nestedNavController.navigate(R.id.fragment_osso_settings, null, navOptBuilder.build());
                }
            }

            NavigationUI.setupWithNavController(mBinding.nestedBottomNavigation, nestedNavController);
        }

        mBleController = BleController.getInstance();

        mViewModel.getOsso().observe(this, new Observer<Osso>() {
            @Override
            public void onChanged(Osso osso) {
                mOsso = osso;
                if(mOssoUpdateStarted){
                    mOssoUpdateStarted = false;
                    return;
                }
                if(osso != null)
                {
                    if(mBinding != null) {
                        ((MainActivity) getActivity()).setActionBarTitle(mBinding.getRoot().getResources().getString(R.string.osso_details_frag_title, osso.petName));
                    }

                    if(mConnection == null)
                    {
                        if(mBleController.getConnection(osso.address) != null) {
                            AppLog.d(TAG, "Osso changed and started reading data");
                            mConnection = mBleController.getConnection(osso.address);
                            mConnection.setConnectListener(OssoDetailsFragment.this);
                            readAllData();
                        }
                        else if(!mBinding.getIsConnecting()){
                            connectDevice(osso.address);
                        }
                    }
                }
                else
                {
                    mHandler.removeCallbacksAndMessages(null);
                    if(readDisposable != null && !readDisposable.isDisposed()){
                        readDisposable.dispose();
                    }
                    readDisposable = null;
                    mConnection = null;
                }

            }
        });
    }

    private Runnable mReadDataRunner = this::readAllData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentOssoDetailsBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        if(readDisposable != null && !readDisposable.isDisposed()){
            readDisposable.dispose();
        }
        readDisposable = null;
        if(mOsso != null){
            mViewModel.updateOssoInDb(mOsso);
        }
        if(stateDisposable != null && !stateDisposable.isDisposed()){
            stateDisposable.dispose();
        }
        stateDisposable = null;
    }

    private void connectDevice(String address)
    {
        RxBleDevice bleDevice = BleController.getInstance().getClient().getBleDevice(address);

        stateDisposable = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
        OssoConnection.establishConnection(bleDevice, this);
        mBinding.setIsConnecting(true);
        Snackbar.make(mBinding.coordinatorLayout, R.string.msg_connecting, Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(mConnection != null && mOsso != null){
            readAllData();
        }
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        AppLog.d(TAG, "Connection State: "+newState.toString());
    }

    public void onConnectionFailure(OssoConnection ossoConnection, Throwable throwable) {
        AppLog.d(TAG, "Connection error: "+throwable);

        if(ossoConnection != null) {
            ossoConnection.bleConnection = null;
            if(mConnection != null && mConnection.bleDevice.getMacAddress().equalsIgnoreCase(ossoConnection.bleDevice.getMacAddress())){
                mBleController.disconnect(mConnection.bleDevice.getMacAddress());
                mConnection = null;
            }
            else if (ossoConnection.connDisposable != null && !ossoConnection.connDisposable.isDisposed()) {
                ossoConnection.connDisposable.dispose();
            }
            ossoConnection.connDisposable = null;
        }

        try {
            mBinding.setIsConnecting(false);
//            Snackbar.make(mBinding.coordinatorLayout, "Error: " + throwable.getMessage(), Snackbar.LENGTH_SHORT).addCallback(mSnackbarCb).show();
            Snackbar.make(mBinding.coordinatorLayout, mBinding.getRoot().getResources().getString(R.string.msg_snackbar_err, throwable.getMessage()), Snackbar.LENGTH_SHORT).addCallback(mSnackbarCb).show();

        }catch (Exception e){}
    }

    private BaseTransientBottomBar.BaseCallback<Snackbar> mSnackbarCb = new BaseTransientBottomBar.BaseCallback<Snackbar>() {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            super.onDismissed(transientBottomBar, event);

            if(event == DISMISS_EVENT_TIMEOUT && mConnection == null && !mBinding.getIsConnecting()){
                try {
                    Snackbar.make(mBinding.coordinatorLayout, R.string.msg_disconnected, Snackbar.LENGTH_INDEFINITE).setAction(R.string.msg_action_reconnect, (v) -> {
                        connectDevice(mOsso.address);
                    }).show();
                } catch (Exception e){}
            }
        }
    };

    public void onConnectedAndReady(OssoConnection ossoConnection) {
        try{
            mBinding.setIsConnecting(false);
            Snackbar.make(mBinding.coordinatorLayout, R.string.msg_connected, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e){}

        if(ossoConnection != null) {
            mConnection = ossoConnection;
            mBleController.addConnection(mOsso.address, mConnection);
            readAllData();
        }
    }

    class ReadResults
    {
        final int barks;
        final int steps;
        final double temp;
        final double humidity;
        final double irTemp;
        final int uvIndex;
        final int exposureTime;
        final LatLng location;
        final int batteryLow;
        final String fw;


        ReadResults(byte[] bBarks, byte[] bSteps, byte[] bTemp, byte[] bHumidity, byte[] bIrTemp, byte[] bUvData, byte[] bGpsData, byte[] bBatteryFw){
            barks = GattDataParser.getBarks(bBarks);
            steps = GattDataParser.getSteps(bSteps);
            temp = GattDataParser.getTemp(bTemp);
            humidity = GattDataParser.getHumidity(bHumidity);
            irTemp = GattDataParser.getIrTemp(bIrTemp);
            uvIndex = GattDataParser.getUvIndex(bUvData);
            exposureTime = GattDataParser.getUvExposureMins(bUvData);
            location = GattDataParser.getLocation(bGpsData);
            batteryLow = GattDataParser.getIsBatteryLow(bBatteryFw) ? 1 : 0;
            fw = GattDataParser.getFWVersion(bBatteryFw).toString();
        }
    }

    private void readAllData()
    {
        if(mConnection == null || !mConnection.isConnected()) {
            return;
        }

        if(readDisposable==null) {
            readDisposable = Single.zip(
                    mConnection.bleConnection.readCharacteristic(mConnection.barkingChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.stepsChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.tempChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.humidityChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.irTempChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.uvIndexChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.gpsChara),
                    mConnection.bleConnection.readCharacteristic(mConnection.batteryLowChara),
                    ReadResults::new)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onAllReadSuccess, this::onReadFailure);
        }

        mHandler.postDelayed(mReadDataRunner, 1000);
    }

    private void onAllReadSuccess(ReadResults results)
    {
        if(mOsso.barks != results.barks) {
            mOsso.barks = results.barks;
            ossoValuesChanged = true;
        }
        if(mOsso.steps != results.steps) {
            mOsso.steps = results.steps;
            ossoValuesChanged = true;
        }
        if(mOsso.temperature != results.temp) {
            mOsso.temperature = results.temp;
            ossoValuesChanged = true;
        }

        if(mOsso.humidity != results.humidity) {
            mOsso.humidity = results.humidity;
            ossoValuesChanged = true;
        }

        if(mOsso.ir_temperature != results.irTemp) {
            mOsso.ir_temperature = results.irTemp;
            ossoValuesChanged = true;
        }

        if(mOsso.uv_index != results.uvIndex || mOsso.exposure_time != results.exposureTime) {
            mOsso.uv_index = results.uvIndex;
            mOsso.exposure_time = results.exposureTime;
            ossoValuesChanged = true;
        }

        if(results.location != null && (mOsso.lastLatitude != results.location.latitude || mOsso.lastLongitude != results.location.longitude)) {
            mOsso.lastLatitude = results.location.latitude;
            mOsso.lastLongitude = results.location.longitude;
            ossoValuesChanged = true;
        }

        if(mOsso.battery != results.batteryLow || !results.fw.equals(mOsso.fw)) {
            mOsso.battery = results.batteryLow;
            mOsso.fw = results.fw;
            ossoValuesChanged = true;
        }

        if(ossoValuesChanged){
            mOssoUpdateStarted = true;
            mViewModel.updateOsso(mOsso);
        }

        if(readDisposable != null && !readDisposable.isDisposed()){
            readDisposable.dispose();
        }
        readDisposable = null;
    }

    private void onReadFailure(Throwable throwable) {
        try {
//            Snackbar.make(mBinding.coordinatorLayout, "Error: " + throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
            Snackbar.make(mBinding.coordinatorLayout, mBinding.getRoot().getResources().getString(R.string.msg_snackbar_err, throwable.getMessage()), Snackbar.LENGTH_SHORT).show();
        }catch (Exception e){}
        AppLog.e(TAG, "Read error: "+throwable);
        if(readDisposable != null && !readDisposable.isDisposed()){
            readDisposable.dispose();
        }
        readDisposable = null;
    }
}
