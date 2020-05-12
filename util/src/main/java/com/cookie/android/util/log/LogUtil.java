package com.cookie.android.util.log;

import android.text.TextUtils;

import com.cookie.android.util.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;


public class LogUtil extends LogConfig{

    public static void v() {
        printLog(V, null, DEFAULT_MESSAGE);
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... objects) {
        printLog(V, tag, objects);
    }

    public static void d() {
        printLog(D, null, DEFAULT_MESSAGE);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(Object msg, int depth) {
        printLog(D, null, msg, depth);
    }

    public static void d(String tag, Object... objects) {
        printLog(D, tag, objects);
    }

    public static void i() {
        printLog(I, null, DEFAULT_MESSAGE);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... objects) {
        printLog(I, tag, objects);
    }

    public static void w() {
        printLog(W, null, DEFAULT_MESSAGE);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... objects) {
        printLog(W, tag, objects);
    }

    public static void e() {
        printLog(E, null, DEFAULT_MESSAGE);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... objects) {
        printLog(E, tag, objects);
    }

    public static void e(Throwable e) {
        printLog(E, null, e);
    }

    public static void e(String tag, Throwable e) {
        printLog(E, tag, e);
    }

    public static void json(String jsonFormat) {
        printLog(JSON, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(JSON, tag, jsonFormat);
    }

    public static void file(Object msg) {
        printFile(null, getDefaultDir(), null, msg, true);
    }

    public static void file(String fileName, Object msg) {
        printFile(null, getDefaultDir(), fileName, msg, true);
    }

    public static void file(File targetDirectory, Object msg) {
        printFile(null, targetDirectory, null, msg, true);
    }

    public static void file(String tag, File targetDirectory, Object msg) {
        printFile(tag, targetDirectory, null, msg, true);
    }

    public static void file(String tag, File targetDirectory, String fileName, Object msg) {
        printFile(tag, targetDirectory, fileName, msg, true);
    }

    public static void http(HttpLogModel httpLog) {
        printLog(HTTP, HTTP_LOG_TAG, httpLog);
    }

    public static void http(String tag, HttpLogModel httpLog) {
        printLog(HTTP, tag, httpLog);
    }

    private static void printLog(int type, String tagStr, Object object) {
        printLog(type, tagStr, object, 1);
    }

    private static void printLog(int type, String tagStr, Object object, int depth) {
        String[] contents = wrapperContent(tagStr, object, depth);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];

        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
                printDefault(type, tag, headString + msg);
                break;
            case JSON:
                JsonLog.printJson(tag, msg, headString);
                break;
            case HTTP:
                HttpLog.printHttp(tag, (HttpLogModel) object, headString);
                break;
        }
    }

    private static void printFile(String tagStr, File targetDirectory, String fileName, Object objectMsg, boolean limit) {

        if (targetDirectory == null) {
            throw new IllegalArgumentException("file log target directory can not be null");
        }

        String[] contents = wrapperContent(tagStr, objectMsg, 0);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];

        FileLog.printFile(tag, targetDirectory, fileName, headString, msg);
    }

    private static String[] wrapperContent(String tagStr, Object objects, int depth) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = 5 + depth;
        String className = stackTrace[index].getFileName();
        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();

        String tag = (tagStr == null ? className : tagStr);
        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = String.format("[ (%s:%d)#%s ] ", className, lineNumber, methodName);

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {

        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(LogUtil.toString(object)).append("\n");
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return LogUtil.toString(object);
        }
    }

    public static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim());
    }

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Logger.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Logger.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static String toString(Object obj){
        if (obj == null){
            return "";
        }
        if (obj instanceof Throwable){
            return getThrowableString((Throwable)obj);
        }
        return obj.toString();
    }

    private static String getThrowableString(Throwable e){
        StringWriter stringWriter = new StringWriter(512);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

}
