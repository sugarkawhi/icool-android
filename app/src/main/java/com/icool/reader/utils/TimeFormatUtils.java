package com.icool.reader.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间格式化
 * Created by ZhaoZongyao on 2018/3/12.
 */

public class TimeFormatUtils {
    /**
     * 格式化为 小时:分钟
     *
     * @return hh:mm
     */
    public static String hhmm(long time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        return sdf.format(date);
    }

    /**
     * 格式化为 小时:分钟
     *
     * @return hh:mm
     */
    public static String yyyyMMddHHmmss(long time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return sdf.format(date);
    }
}
