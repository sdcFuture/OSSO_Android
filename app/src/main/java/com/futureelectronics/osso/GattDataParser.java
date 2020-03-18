package com.futureelectronics.osso;

import com.google.android.gms.maps.model.LatLng;
import com.polidea.rxandroidble2.helpers.ValueInterpreter;

/**
 * Created by Kyle Harman on 2/6/2019.
 */
public class GattDataParser {

    public static double getTemp(byte[] b){
        Integer temp = ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_SINT16, 0);
        if(temp == null){
            return 0.0;
        }
        return temp/10.0;
    }

    public static double getHumidity(byte[] b){
        Integer temp = ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT16, 0);
        if(temp == null){
            return 0.0;
        }
        return temp/10.0;
    }

    public static double getIrTemp(byte[] b){
        Integer temp = ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_SINT16, 0);
        if(temp == null){
            return 0.0;
        }
        return temp/10.0;
    }

    public static Integer getUvIndex(byte[] b){
        if(b.length < 1){
            return null;
        }
        return (b[0] & 0x0F);
    }

    public static Integer getUvExposureMins(byte[] b){
        Integer mins = ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT16, 0);
        if(mins != null){
            mins = mins >> 4;
        }
        return mins;
    }

    public static Integer getBarks(byte[] b){
        return ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT16, 0);
    }

    public static Integer getSteps(byte[] b){
        return ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT16, 0);
    }

    public static LatLng getLocation(byte[] b){
        double latitude, longitude;
        try {
            latitude = (double)ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_SINT16, 10);
            if(latitude < 0.0){
                latitude -= ((double) ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT32, 6)) / 10000.0;
            }
            else {
                latitude += ((double) ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT32, 6)) / 10000.0;
            }

            longitude = (double)ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_SINT16, 4);
            if(longitude < 0.0){
                longitude -= ((double) ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT32, 0)) / 10000.0;
            }
            else {
                longitude += ((double) ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT32, 0)) / 10000.0;
            }
        }
        catch (NullPointerException e){
            return null;
        }

        if(latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180){
            return null;
        }

        return new LatLng(latitude, longitude);
    }

    public static boolean getIsBatteryLow(byte[] b){
        if(b.length < 1){
            return false;
        }
        return (b[0]&0x01) == 1;
    }

    public static Integer getFWVersion(byte[] b){
        return ValueInterpreter.getIntValue(b, ValueInterpreter.FORMAT_UINT8, 1);
    }
}
