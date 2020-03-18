package com.futureelectronics.osso.bluetooth;

import android.content.Context;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.internal.RxBleLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyle Harman on 2/4/2019.
 */
public class BleController {
    private static BleController INSTANCE = null;
    private RxBleClient rxBleClient;

    private Map<String, OssoConnection> mConnections = new HashMap<>();

    private BleController(Context context)
    {
        rxBleClient = RxBleClient.create(context.getApplicationContext());
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
    }

    public static synchronized void init(Context context)
    {
        if (INSTANCE == null){
            INSTANCE = new BleController(context);
        }
    }

    public static BleController getInstance(){
        return INSTANCE;
    }

    public static BleController getInstance(Context context){
        init(context);
        return INSTANCE;
    }

    public RxBleClient getClient()
    {
        return rxBleClient;
    }

    public void addConnection(String address, OssoConnection ossoConnection){
        synchronized (BleController.class) {
            mConnections.put(address, ossoConnection);
        }
    }

    public OssoConnection getConnection(String address){
        OssoConnection connection;
        synchronized (BleController.class) {
            connection = mConnections.get(address);
        }
        return connection;
    }

    public void disconnect(String address){
        synchronized (BleController.class) {
            OssoConnection connection = mConnections.get(address);
            if(connection != null){
                if(connection.connDisposable != null && !connection.connDisposable.isDisposed()) {
                    connection.connDisposable.dispose();
                }
                mConnections.remove(address);
            }
        }
    }

    public void disconnectAll()
    {
        synchronized (BleController.class) {
            for (OssoConnection connection : mConnections.values()) {
                if (connection != null && connection.connDisposable != null && !connection.connDisposable.isDisposed()) {
                    connection.connDisposable.dispose();
                }
            }
            mConnections.clear();
        }
    }
}
