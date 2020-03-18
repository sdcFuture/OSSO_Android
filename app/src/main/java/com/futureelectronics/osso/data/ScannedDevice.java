package com.futureelectronics.osso.data;

import com.polidea.rxandroidble2.RxBleDevice;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * Created by Kyle Harman on 12/11/2018.
 */
public class ScannedDevice {
    private final RxBleDevice mDevice;
    private int mRssi = 0;
    private byte[] mScanRecord;
    private long advIntervalNanos = 0;
    private long lastAdvTime = 0;
    private int updateCount = 0;
    private String mAddress, mName;

    private static final int SCANINFO_FRESH_TIMEOUT = 10000;

    public ScannedDevice(RxBleDevice device){
        mDevice = device;
        mAddress = device.getMacAddress();
        mName = "";
    }

    private ScannedDevice(){
        mDevice = null;
        mName = "";
    }

    public static ScannedDevice newFakeInstance(String nameStart){
        ScannedDevice device = new ScannedDevice();
        Random rn = new Random();
        int[] addr = new int[6];
        for(int i=0; i<addr.length; i++){
            addr[i] = rn.nextInt(255);
        }
        device.mAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5]);
        device.mName = String.format("%s%02X%02X", nameStart, addr[4], addr[5]);
        device.mRssi = rn.nextInt(60)-100;

        return device;
    }

    public static ScannedDevice newFakeInstance(){
        return ScannedDevice.newFakeInstance("BTDevice");
    }

    public void updateInfo(int rssi, byte[] scanRecord, long timeStamp){
        updateCount++;
        if(lastAdvTime != 0){
            long newAdvIntv = timeStamp - lastAdvTime;
            if(advIntervalNanos == 0){
                advIntervalNanos = newAdvIntv;
            }
            else if(newAdvIntv < (advIntervalNanos*7)/10 && updateCount<10){
                advIntervalNanos = (advIntervalNanos+newAdvIntv*2)/3;
            }
            else if(newAdvIntv < advIntervalNanos+3000000L){
                int i = Math.min(updateCount, 10);
                advIntervalNanos = (advIntervalNanos*(i-1)+newAdvIntv)/i;
            }
            else if(newAdvIntv < advIntervalNanos*2){
                advIntervalNanos = (advIntervalNanos*15+newAdvIntv)/16;
            }
        }
        lastAdvTime = timeStamp;

        mRssi = rssi;
        mScanRecord = scanRecord;

        if(mDevice != null && mName == "" && (mDevice.getName() == null || mDevice.getName().length() == 0)){
            mName = parseName(scanRecord);
        }
    }

    public long getAdvInterval(){
        if(advIntervalNanos != 0){
            return advIntervalNanos/1000000;
        }
        else{
            return 10000;
        }
    }

    public boolean isInfoFresh(){
        return (SystemClock.elapsedRealtime() - lastAdvTime/1000000) < SCANINFO_FRESH_TIMEOUT;
    }

    public RxBleDevice getDevice(){
        return mDevice;
    }

    public String getAddress(){
        return mAddress;
    }

    public byte[] getScanRecord(){
        return mScanRecord;
    }

    public int getRssi(){
        return mRssi;
    }

    public String getRssiString(){
        return String.valueOf(mRssi);
    }

//    public String getName(){
//        if(mDevice == null || mDevice.getName() == null || mDevice.getName().length() == 0){
//            return mName;
//        }
//
//        return mDevice.getName();
//    }

    public String getName(){
        if(mName.isEmpty() && mDevice != null && mDevice.getName() != null) {
            return mDevice.getName();
        }

        return mName;
    }

    public void setName(String name){
        mName = name;
    }

    /**
     * Try to parse out the BLE device name from the advertisement data.
     * @param advertisedData the advertisement data
     * @return The parsed out name
     */
    private static String parseName(byte[] advertisedData) {
        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0 || length > buffer.remaining()) break;

            byte type = buffer.get();
            switch (type) {
                case 0x08: // Shortened name
                case 0x09: // Complete name
                    byte[] nameBytes = new byte[length-1];
                    for(int i=0; i<(length-1); i++){
                        nameBytes[i] = buffer.get();
                    }
                    return new String(nameBytes, 0, length-1);

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return "";
    }
}
