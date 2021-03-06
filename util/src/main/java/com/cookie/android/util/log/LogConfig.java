package com.cookie.android.util.log;


import android.os.Environment;

import com.cookie.android.util.LibConfig;
import com.cookie.android.util.Logger;

import java.io.File;

public class LogConfig {

    static final String DEFAULT_MESSAGE = "execute";
    static final String LINE_SEPARATOR = System.getProperty("line.separator") == null ? "" : System.getProperty("line.separator");
    static final String NULL_TIPS = "Log with null object";
    static final String PARAM = "Param";
    static final String NULL = "null";
    static final String HTTP_LOG_TAG = "http_log";

    static final int JSON_INDENT = 4;


    static final int V = 0x1;
    static final int D = 0x2;
    static final int I = 0x3;
    static final int W = 0x4;
    static final int E = 0x5;
    public static final int JSON = 0x7;
    public static final int HTTP = 0x8;

    private static boolean isHttpLogToFile = false;

    public static void setHttpLogToFile(boolean isLogToFile) {
        LogConfig.isHttpLogToFile = isLogToFile;
    }

    static boolean isHttpLogToFile() {
        return isHttpLogToFile;
    }

    private static File sDefaultLogDir;
    static int fileDays = 7;

    public static void initLogDirName(String dirName) {
        if (dirName == null || dirName.isEmpty())
            return;
        File systemDir = LibConfig.INSTANCE.getApp().getFilesDir();
        if (systemDir != null) {
            File dir = new File(systemDir.getPath() + "/" + dirName + "/");
            dir.mkdirs();
            sDefaultLogDir = dir;
        }
    }

    public static void initLogDir(File dir) {
        sDefaultLogDir = dir;
    }

    public static void setFileDays(int fileDays) {
        LogConfig.fileDays = fileDays;
    }

    public static File getDefaultDir() {
        if (sDefaultLogDir == null)
            initLogDirName("cookie_log");
        return sDefaultLogDir;
    }

    public static void printDefault(int type, String tag, String msg) {

        int index = 0;
        int maxLength = 4000;
        int countOfSub = msg.length() / maxLength;

        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + maxLength);
                printSub(type, tag, sub);
                index += maxLength;
            }
            printSub(type, tag, msg.substring(index, msg.length()));
        } else {
            printSub(type, tag, msg);
        }
        if (isHttpLogToFile) {
            FileLog.printFile(msg);
        }
    }

    private static void printSub(int type, String tag, String sub) {
        switch (type) {
            case V:
                Logger.v(tag, sub);
                break;
            case D:
                Logger.d(tag, sub);
                break;
            case I:
                Logger.i(tag, sub);
                break;
            case W:
                Logger.w(tag, sub);
                break;
            case E:
                Logger.e(tag, sub);
                break;
        }
    }

}
