package com.cookie.android.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * ContextUtil
 * Author: ZhangLingfei
 * Date : 2019/1/6 0006
 */
public class ContextUtil {

    @NonNull
    public static Application get() {
        return LibConfig.INSTANCE.getApp();
    }

    /**
     * @param context
     * @return
     * @deprecated 改用 {@link #getVersionName()}
     */
    @Deprecated
    public static String getVersionName(Context context) {
        return getVersionName();
    }


    /**
     * 获取App版本名称
     *
     * @return
     */
    public static String getVersionName() {
        try {
            return BuildConfig.VERSION_NAME;
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }


    /**
     * 获取App版本code
     *
     * @return
     */
    public static long getVersionCode() {
        try {
            return BuildConfig.VERSION_CODE;
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    /**
     * 获取应用程序名称
     *
     * @param context
     * @deprecated 改用 {@link #getAppName()}}
     */
    @Deprecated
    public static String getAppName(Context context) {
        return getAppName();
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName() {
        try {
            PackageManager packageManager = get().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(get().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return get().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备给予的最大内存
     *
     * @return
     */
    public static int getMaxMemory() {
        ActivityManager activityManager = (ActivityManager) ContextUtil.get().getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        if (activityManager != null)
            return activityManager.getMemoryClass();
        return 0;
    }

    public static boolean isLowRAMDevice() {
        ActivityManager activityManager = (ActivityManager) ContextUtil.get().getSystemService(ACTIVITY_SERVICE);
        if (activityManager != null)
            return activityManager.isLowRamDevice();
        return false;
    }
}
