package com.icool.reader.utils;

import android.util.Log;

/**
 * 日志打印
 * Created by ZhaoZongyao on 2018/3/27.
 */

public class Logger {

    private static boolean debug = true;

    public static void e(String TAG, String messae) {
        if (debug)
            Log.e(TAG, messae);
    }

    public static void i(String TAG, String messae) {
        if (debug)
            Log.i(TAG, messae);
    }

    public static void d(String TAG, String messae) {
        if (debug)
            Log.d(TAG, messae);
    }

    public static void w(String TAG, String messae) {
        if (debug)
            Log.w(TAG, messae);
    }
}
