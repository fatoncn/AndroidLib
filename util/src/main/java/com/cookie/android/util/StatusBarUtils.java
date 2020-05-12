package com.cookie.android.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.ColorRes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Package_name:com.cookie.android.util.log
 * Author:zhaoqiang
 * Email:zhaoq_hero@163.com
 * Date:2017/03/28   16/25
 */
public class StatusBarUtils {

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        int statusBarHeight2 = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight2 = ContextUtil.get().getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            Logger.printException(e);
        }
        return statusBarHeight2;
    }

    /**
     * 设置  小米状态栏
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param activity 设置的窗口
     * @param darkMode 是否把状态栏字体及图标颜色设置为深色
     * @return 成功执行返回true
     */
    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkMode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            @SuppressLint("PrivateApi")
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkMode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    //修改 魅族 状态栏样式
    public static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static void setWindowStatusBarColor(Activity activity, int color, boolean isNightMode) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                if (isNightMode) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                } else {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }

                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setWindowStatusBarColor(Dialog dialog, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = dialog.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(UtilExtKt.resColor(colorResId));

                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 谷歌原生方式修改状态栏文字颜色
     *
     * @param activity
     * @param dark
     */
    public static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * 判断是否有刘海屏
     *
     * @param activity
     * @return
     */
    public static boolean hasAllScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> rects = displayCutout.getBoundingRects();
                    //通过判断是否存在rects来确定是否刘海屏手机
                    return rects != null && rects.size() > 0;
                }
            }
        }
        return false;
    }


    /**
     * 普遍状态下的状态栏设置，此时状态栏根据夜间模式变化
     *
     * @param baseActivity 页面
     * @param isNightMode  夜间模式
     */
    public static void initStatusBar(Activity baseActivity, boolean isNightMode,@ColorRes int colorRes) {
        StatusBarUtils.setWindowStatusBarColor(baseActivity, UtilExtKt.resColor(colorRes), isNightMode);

        StatusBarUtils.setMeizuStatusBarDarkIcon(baseActivity, !isNightMode);//设置  魅族状态栏样式
        StatusBarUtils.setMiuiStatusBarDarkMode(baseActivity, !isNightMode);//设置  小米状态栏样式
    }

    /**
     * 全屏模式下的状态栏设置，此时状态栏根据夜间模式变化
     *
     * @param baseActivity 页面
     * @param isDark  夜间模式
     */
    public static void initStatusBarForFullScreen(Activity baseActivity, boolean isDark, @ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            Window window = baseActivity.getWindow();
            View decorView = window.getDecorView();
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);

            //设置魅族状态栏样式
            StatusBarUtils.setMeizuStatusBarDarkIcon(baseActivity, isDark);
            //设置小米状态栏样式
            StatusBarUtils.setMiuiStatusBarDarkMode(baseActivity, isDark);

        } else {
            Window window = baseActivity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
            window.setAttributes(attributes);

            //设置魅族状态栏样式
            StatusBarUtils.setMeizuStatusBarDarkIcon(baseActivity, isDark);
            //设置小米状态栏样式
            StatusBarUtils.setMiuiStatusBarDarkMode(baseActivity, isDark);
        }

        //全屏模式下也要修改状态栏，因为长纸条暂时没有夜间模式
        StatusBarUtils.setWindowStatusBarColor(baseActivity, UtilExtKt.resColor(colorRes), isDark);

    }
}
