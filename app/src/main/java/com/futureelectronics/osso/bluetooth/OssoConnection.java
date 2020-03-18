package com.futureelectronics.osso.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import com.futureelectronics.osso.AppLog;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleCustomOperation;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.Timeout;
import com.polidea.rxandroidble2.internal.connection.RxBleGattCallback;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by Kyle Harman on 2/6/2019.
 */
public class OssoConnection {
    private final static String TAG = OssoConnection.class.getSimpleName();

    public RxBleDevice bleDevice;
    public Disposable connDisposable;
    public RxBleConnection bleConnection;

    private Disposable scanDisposable;

    public BluetoothGattCharacteristic tempChara;
    public BluetoothGattCharacteristic humidityChara;
    public BluetoothGattCharacteristic irTempChara;
    public BluetoothGattCharacteristic uvIndexChara;
    public BluetoothGattCharacteristic gpsChara;
    public BluetoothGattCharacteristic barkingChara;
    public BluetoothGattCharacteristic stepsChara;
    public BluetoothGattCharacteristic batteryLowChara;

    private ConnectListener mListener;

    private static int MAX_CONNECTION_TRIES = 4;
    private int mConnTries = 0;

    private Handler mHandler = new Handler();

    public OssoConnection(){

    }

    public OssoConnection(RxBleDevice bleDevice, Disposable connectionDisposable){
        this.bleDevice = bleDevice;
        this.connDisposable = connectionDisposable;
    }

    public boolean getCharacteristicsFromServices(RxBleDeviceServices services)
    {
        boolean allEnvCharasFound = false;
        boolean allAccCharasFound = false;
        for (BluetoothGattService service : services.getBluetoothGattServices()) {
            if(service.getUuid().equals(UUIDDatabase.OSSO_ACC_SERVICE)){
                if((stepsChara = service.getCharacteristic(UUIDDatabase.OSSO_ACC_CHAR)) != null){
                    allAccCharasFound = true;
                }
            }
            else if(service.getUuid().equals(UUIDDatabase.OSSO_CUSTOM_SERVICE)){
                tempChara = service.getCharacteristic(UUIDDatabase.OSSO_TEMP_CHAR);
                humidityChara = service.getCharacteristic(UUIDDatabase.OSSO_HUMIDITY_CHAR);
                irTempChara = service.getCharacteristic(UUIDDatabase.OSSO_IR_TEMP_CHAR);
                uvIndexChara = service.getCharacteristic(UUIDDatabase.OSSO_UV_INDEX_CHAR);
                gpsChara = service.getCharacteristic(UUIDDatabase.OSSO_GPS_CHAR);
                barkingChara = service.getCharacteristic(UUIDDatabase.OSSO_BARKING_CHAR);
                batteryLowChara = service.getCharacteristic(UUIDDatabase.OSSO_BATTERY_LOW_CHAR);
                if(tempChara != null
                        && humidityChara != null
                        && irTempChara != null
                        && uvIndexChara != null
                        && gpsChara != null
                        && barkingChara != null
                        && batteryLowChara != null){
                    allEnvCharasFound = true;
                }
            }
            if (allAccCharasFound && allEnvCharasFound){
                break;
            }
        }

        return allCharacteristicsFound();
    }

    public boolean allCharacteristicsFound()
    {
        if (stepsChara != null
                && tempChara != null
                && humidityChara != null
                && irTempChara != null
                && uvIndexChara != null
                && gpsChara != null
                && barkingChara != null
                && batteryLowChara != null){
            return true;
        }

        return false;
    }

    public boolean isConnected()
    {
        if(bleDevice != null && bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED){
            return true;
        }
        return false;
    }

    public static OssoConnection establishConnection(RxBleDevice rxBleDevice, ConnectListener listener)
    {
        OssoConnection connection = new OssoConnection();
        connection.bleDevice = rxBleDevice;
        connection.mListener = listener;

        connection.mConnTries = 0;

        connection.connect(false);

        return connection;
    }

    public void connect(boolean autoConnect){
        if(bleDevice == null){
            return;
        }

        if(isConnected()){
            return;
        }

        if(connDisposable != null && !connDisposable.isDisposed()){
            connDisposable.dispose();
        }
        connDisposable = bleDevice.establishConnection(autoConnect, new Timeout(6, TimeUnit.SECONDS))
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnDispose(() -> {AppLog.d(TAG, "Cancelled mStopConnectingRunner."); mHandler.removeCallbacks(mStopConnectingRunner);})
                .flatMapSingle((rxBleConnection) -> {bleConnection = rxBleConnection; return rxBleConnection.discoverServices(10, TimeUnit.SECONDS);})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(OssoConnection.this::onServicesDiscovered, OssoConnection.this::onConnectionFailure);

        if(autoConnect){
            AppLog.d(TAG, "Creating callback to mStopConnectingRunner.");
            mHandler.postDelayed(mStopConnectingRunner, 7000);
        }
    }

    private static class GattRefreshOperation implements RxBleCustomOperation<Void> {
        private long delay_ms = 500;

        GattRefreshOperation() {
        }

        GattRefreshOperation(long delay_ms) {
            this.delay_ms = delay_ms;
        }

        @NonNull
        @Override
        public Observable<Void> asObservable(BluetoothGatt bluetoothGatt,
                                             RxBleGattCallback rxBleGattCallback,
                                             Scheduler scheduler) throws Throwable {

            return Observable.fromCallable(() -> refreshDeviceCache(bluetoothGatt))
//                    .delay(delay_ms, TimeUnit.MILLISECONDS, Schedulers.computation())
                    .subscribeOn(scheduler);
        }

        private Void refreshDeviceCache(final BluetoothGatt gatt) {
            AppLog.d(TAG, "Gatt Refresh " + (OssoConnection.refreshDeviceCache(gatt) ? "succeeded" : "failed"));
            return null;
        }
    }

    private static boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            final Method method = gatt.getClass().getMethod("refresh", new Class[0]);
            if (method != null) {
                return (Boolean) method.invoke(gatt, new Object[0]);
            }
        }
        catch (Exception e) {
            AppLog.e(TAG, "An exception occurred while refreshing gatt device cache: "+e);
        }
        return false;
    }

    private void tryGattRefresh()
    {
        if(bleConnection != null){
            try {
                bleConnection.queue(new GattRefreshOperation(0)).timeout(2, TimeUnit.SECONDS).subscribe(
                        readValue -> {
                            AppLog.d(TAG, "Refresh OK: " + readValue);
                            connect(false);
                        }, throwable -> {
                            AppLog.d(TAG, "Refresh exeception: " + throwable);
                            connect(false);
                        }
                );
            }catch (Exception e){AppLog.d(TAG, "Got exception trying gatt refresh: " + e);}
        }
        else{
            // connection is null so just try to reconnect
            connect(false);
        }
    }

    private void scanBleDevices() {
        scanDisposable = BleController.getInstance().getClient().scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
//                            .setDeviceAddress("B4:99:4C:34:DC:8B")
                        .setDeviceName("BlueNRG")
                        // add custom filters if needed
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> scanDisposable = null)
                .subscribe(scanResult -> {
                    // Empty scan subscribe
                });

        mHandler.postDelayed(() -> stopScanning(), 10000);
    }

    private synchronized void stopScanning()
    {
        if (scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
        scanDisposable = null;
    }

    private Runnable mStopConnectingRunner = () -> {
        if(bleDevice != null && bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTING) {
            if(connDisposable != null && !connDisposable.isDisposed()) {
                connDisposable.dispose();
                onConnectionFailure(new Throwable("Timeout trying to connect."));
            }
        }
    };

    private void onConnectionFailure(Throwable throwable) {
        mHandler.removeCallbacks(mStopConnectingRunner);
        if(mConnTries < MAX_CONNECTION_TRIES){
            mConnTries++;
            if(mConnTries == 2){
                tryGattRefresh();
            }
            else{
                if(mConnTries == 3){
                    scanBleDevices();
                }
                bleConnection = null;
                connect(true);
            }
        }
        else {
            bleConnection = null;
            if (mListener != null) {
                mListener.onConnectionFailure(this, throwable);
            }
        }
    }

    private void onServicesDiscovered(RxBleDeviceServices services) {
        mHandler.removeCallbacks(mStopConnectingRunner);
        mConnTries = 0;
        stopScanning();
        if(getCharacteristicsFromServices(services) && mListener != null){
            mListener.onConnectedAndReady(this);
        }
        else{
            if(connDisposable != null && !connDisposable.isDisposed()){
                connDisposable.dispose();
            }
            connDisposable = null;
            bleConnection = null;
            // Not a proper OSSO device
            if(mListener != null){
                mListener.onConnectionFailure(this, new Throwable("Unable to get all the proper characteristics."));
            }
        }
    }

    public void setConnectListener(ConnectListener listener){
        mListener = listener;
    }

    public interface ConnectListener {
        void onConnectedAndReady(OssoConnection ossoConnection);
        void onConnectionFailure(OssoConnection ossoConnection, Throwable throwable);
    }
}
