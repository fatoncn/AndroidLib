package com.cookie.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SpUtil
 * Author: ZhangLingfei
 * Date : 2019/1/6 0006
 */
public class SpUtil {


    /**
     * 根据传入的键获取内容
     *
     * @param fileName      文件名
     * @param key           传入的key
     * @param defaultObject 默认  返回值
     * @return 返回获取到的内容
     */
    public <T> T getSp(String fileName, @NonNull String key, T defaultObject) {
        SharedPreferences sp = ContextUtil.get().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return (T) sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return (T) ((Integer) sp.getInt(key, (Integer) defaultObject));
        } else if (defaultObject instanceof Boolean) {
            return (T) ((Boolean) sp.getBoolean(key, (Boolean) defaultObject));
        } else if (defaultObject instanceof Float) {
            return (T) ((Float) sp.getFloat(key, (Float) defaultObject));
        } else if (defaultObject instanceof Long) {
            return (T) ((Long) sp.getLong(key, (Long) defaultObject));
        } else if (defaultObject == null) {
            return (T) sp.getString(key, null);
        }

        return null;
    }

    /**
     * 放入
     *
     * @param file_name 文件名
     * @param key       键
     * @param object    值
     */
    public void putSp(String file_name, @NonNull String key, @NonNull Object object) {
        SharedPreferences sp = ContextUtil.get().getSharedPreferences(file_name, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
    }


    /***
     * 创建一个  解决 SharePreferancesCompat.app方法的  兼容类
     */
    protected static class SharedPreferencesCompat {

        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射  查找  Apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;

                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 使用  apply 方法
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (editor != null) {
                editor.commit();
            }
        }
    }
}
