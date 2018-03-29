package com.icool.reader.component.reader.utils;

import android.util.Log;


/**
 * log print
 * Created by ZhaoZongyao on 2018/1/30.
 */

public class ReaderLogger {

    private static final boolean READER_DEBUG = false;

    public static void i(String TAG, String log) {
        if (!READER_DEBUG) return;
        Log.i(TAG, log);
    }

    public static void d(String TAG, String log) {
        if (!READER_DEBUG) return;
        Log.d(TAG, log);
    }

    public static void w(String TAG, String log) {
        if (!READER_DEBUG) return;
        Log.w(TAG, log);
    }

    public static void e(String TAG, String log) {
        if (!READER_DEBUG) return;
        Log.e(TAG, log);
    }


}
