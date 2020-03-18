package com.futureelectronics.osso;

import android.util.Log;
//import com.crashlytics.android.Crashlytics;

/**
 * Created by Kyle Harman on 12/29/2016.
 */

public class AppLog {

    public static void v(String tag, String msg){
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg){
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg){
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg){
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg){
        Log.e(tag, msg);
    }

//    public static void v(String tag, String msg){
//        Crashlytics.log(Log.VERBOSE, tag, msg);
//    }
//
//    public static void d(String tag, String msg){
//        Crashlytics.log(Log.DEBUG, tag, msg);
//    }
//
//    public static void i(String tag, String msg){
//        Crashlytics.log(Log.INFO, tag, msg);
//    }
//
//    public static void w(String tag, String msg){
//        Crashlytics.log(Log.WARN, tag, msg);
//    }
//
//    public static void e(String tag, String msg){
//        Crashlytics.log(Log.ERROR, tag, msg);
//    }

}
