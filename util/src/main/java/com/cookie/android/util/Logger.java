package com.cookie.android.util;

import android.util.Log;

/**
 * 日志输出工具类
 */
public class Logger {
    private static LoggerLevel show_falg = LoggerLevel.VERBOSE;

    //    private static final String TAG = "MyLogger";

    public enum LoggerLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT
    }

    private static boolean openLog = false;

    private static boolean needLog() {
        return openLog || LibConfig.INSTANCE.isApkDebug();
    }

    public static void openLog(boolean openLog) {
        Logger.openLog = openLog;
    }

    private static StackTraceElement getTopTrace() {
        return Thread.currentThread().getStackTrace()[5];
    }

    private static String getDefaultTag() {
        StackTraceElement trace = getTopTrace();
        return trace.getClassName() + ":" + trace.getMethodName();
    }

    public static void d(String tag, String msg) {
        if (needLog())
            if (LoggerLevel.DEBUG.ordinal() >= show_falg.ordinal()) {
                Log.d(tag, msg);
            }
    }

    public static void i(String tag, String msg) {
        if (needLog())
            if (LoggerLevel.INFO.ordinal() >= show_falg.ordinal()) {
                Log.i(tag, msg);
            }
    }

    public static void i() {
        i(getDefaultTag(), "");
    }

    public static void w(String tag, String msg) {
        if (needLog())
            if (LoggerLevel.WARN.ordinal() >= show_falg.ordinal()) {
                Log.w(tag, msg);
            }
    }

    public static void e(String tag, String msg) {
        if (needLog())
            if (LoggerLevel.ERROR.ordinal() >= show_falg.ordinal()) {
                Log.e(tag, msg);
            }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (needLog())
            if (LoggerLevel.ERROR.ordinal() >= show_falg.ordinal()) {
                Log.e(tag, msg, tr);
            }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (needLog())
            if (LoggerLevel.DEBUG.ordinal() >= show_falg.ordinal()) {
                Log.e(tag, msg, tr);
            }
    }

    public static void v(String tag, String msg) {
        if (needLog())
            if (LoggerLevel.DEBUG.ordinal() >= show_falg.ordinal()) {
                Log.v(tag, msg);
            }
    }

    public static void v(String msg) {
        if (needLog())
            if (LoggerLevel.DEBUG.ordinal() >= show_falg.ordinal()) {
                Log.v(getDefaultTag(), msg);
            }
    }

    public static void d(String msg) {
        if (needLog())
            if (LoggerLevel.DEBUG.ordinal() >= show_falg.ordinal()) {
                Log.d(getDefaultTag(), msg);
            }
    }

    public static void i(String msg) {
        i(getDefaultTag(), msg);
    }

    public static void w(String msg) {
        if (needLog())
            if (LoggerLevel.WARN.ordinal() >= show_falg.ordinal()) {
                Log.w(getDefaultTag(), msg);
            }
    }

    public static void e(String msg) {
        if (needLog())
            if (LoggerLevel.ERROR.ordinal() >= show_falg.ordinal()) {
                Log.e(getDefaultTag(), msg);
            }
    }

    public static void printException(Throwable e) {
        //FIXME:加上异常上报
        if (needLog()) {
            e.printStackTrace();
        }
    }
}
