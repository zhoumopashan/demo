package com.haier.xiaoyi.util;

import android.util.Log;

import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class Logger {
    private static final String TAG = "luodemo";
    public static final boolean LOGABLE = WifiP2pConfigInfo.isDebug;

    public static void d(String alt, String msg) {
        if (LOGABLE) {
            Log.d(TAG, alt + " : " + msg);
        }
    }

    public static void i(String alt, String msg) {
        if (LOGABLE) {
            Log.i(TAG, alt + " : " + msg);
        }
    }

    public static void v(String alt, String msg) {
        if (LOGABLE) {
            Log.v(TAG, alt + " : " + msg);
        }
    }

    public static void w(String alt, String msg) {
        if (LOGABLE) {
            Log.w(TAG, alt + " : " + msg);
        }
    }

    public static void e(String alt, String msg) {
        if (LOGABLE) {
            Log.e(TAG, alt + " : " + msg);
        }
    }

    public static void e(String alt, String msg, Throwable t) {
        if (LOGABLE) {
            Log.e(TAG, alt + " : " + msg, t);
        }
    }

    public static void e(String alt, Exception e) {
        if (LOGABLE) {
            Log.e(TAG, alt + ":" + Log.getStackTraceString(e));
        }
    }
}
