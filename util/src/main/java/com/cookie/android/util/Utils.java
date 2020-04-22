package com.cookie.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.cookie.android.util.gson.StringTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.cookie.android.util.gson.BoolTypeAdapter;
import com.cookie.android.util.gson.CompatibleTypeAdapter;
import com.cookie.android.util.gson.GsonCompatible;
import com.cookie.android.util.gson.IntTypeAdapter;
import com.cookie.android.util.gson.ListTypeAdapter;
import com.cookie.android.util.gson.LongTypeAdapter;
import com.cookie.android.util.gson.StringTypeAdapter;

/**
 * 对象util类
 * Author: ZhangLingfei
 * Date : 2018/11/26 0026
 */

public class Utils {

    public static boolean equal(Object a, Object b) {
//        return (a == null && b == null) || (a != null && a.equal(b));
        return Objects.equals(a, b);
    }

    public static Gson gson() {
        return gsonBuilder().create();
    }

    public static GsonBuilder gsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(String.class, new StringTypeAdapter())
                .registerTypeAdapter(Long.class, new LongTypeAdapter())
                .registerTypeAdapter(long.class, new LongTypeAdapter())
                .registerTypeAdapter(Integer.class, new IntTypeAdapter())
                .registerTypeAdapter(int.class, new IntTypeAdapter())
                .registerTypeAdapter(Boolean.class, new BoolTypeAdapter())
                .registerTypeHierarchyAdapter(List.class, new ListTypeAdapter())
                .registerTypeHierarchyAdapter(GsonCompatible.class, new CompatibleTypeAdapter());
    }

    public static boolean isSimpleClass(Type type) {
        boolean isBoolean = type == Boolean.class || type == boolean.class;
        boolean isByte = type == Byte.class || type == byte.class;
        boolean isShort = type == Short.class || type == short.class;
        boolean isInt = type == Integer.class || type == int.class;
        boolean isLong = type == Long.class || type == long.class;
        boolean isFloat = type == Float.class || type == float.class;
        boolean isDouble = type == Double.class || type == double.class;
        boolean isChar = type == char.class;
        boolean isString = (type instanceof Class && CharSequence.class.isAssignableFrom((Class<?>) type));
        return (isBoolean || isInt || isLong || isByte || isShort || isFloat || isDouble || isString || isChar);
    }

    /**
     * 获取activity在manifest中注册的label
     *
     * @param activity
     * @return
     */
    public static String getActivityLabel(@NonNull Activity activity) {
        return UtilExtKt.activityLabel(activity);
    }

    public static Type getSuperGenericParam(@NonNull Class clz) {
        return ((ParameterizedType) clz.getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public static Class getSuperGenericClass(@NonNull Class clz) {
        return getRawType(getSuperGenericParam(clz));
    }

    /**
     * Extract the raw class type from {@code type}. For example, the type representing
     * {@code List<? extends Runnable>} returns {@code List.class}.
     */
    public static Class<?> getRawType(@NonNull Type type) {
        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> void fillJson(@NonNull T target, @Nullable JsonObject json) {
        fillJson(target, json, null);
    }

    public static <T> void fillJson(@NonNull T target, @Nullable JsonObject json, @Nullable Gson gson) {
        if (json == null)
            return;
        if (gson == null)
            gson = gson();
        try {
            for (String key : json.keySet()) {
                try {
                    Field field = target.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    JsonElement element = json.get(key);
                    Object value;
                    if (List.class.isAssignableFrom(field.getType()))
                        value = gson.fromJson(element, TypeToken.get(field.getGenericType()).getType());
                    else
                        value = gson.fromJson(element, field.getType());
                    if (!isBasicType(field.getType()) || value != null) {
                        field.set(target, value);
                    } else {
                        field.set(target, getBasicDefaultValue(field.getType()));
                    }
                } catch (NoSuchFieldException e) {
                }
            }
        } catch (IllegalAccessException e) {
            Logger.printException(e);
        }
    }

    public static boolean isBasicType(@NonNull Class clz) {
        return clz == int.class || clz == boolean.class || clz == long.class || clz == byte.class
                || clz == short.class || clz == float.class || clz == double.class || clz == char.class;
    }

    @NonNull
    public static Object getBasicDefaultValue(@NonNull Class clz) {
        if (clz == boolean.class)
            return 0;
        else if (clz == char.class)
            return '\0';
        else
            return 0;
    }

    /**
     * 将含有id字段的List转换为以id为key的HashMap
     *
     * @param list
     * @param <Id>
     * @param <T>
     * @return
     */
    @NonNull
    public static <T, Id> Map<Id, T> toIdMap(@Nullable List<T> list, @NonNull FieldGetter<Id, T> fieldGetter) {
        HashMap<Id, T> result = new HashMap<>();
        if (list == null)
            return result;
        for (T entity : list)
            result.put(fieldGetter.getField(entity), entity);
        return result;
    }

    public static <K> void convertNullToEmptyString(Map<K, ? super String> params) {
        for (K key : params.keySet())
            if (params.get(key) == null)
                params.put(key, "");
    }

    public static boolean isMainThread() {
        return UtilExtKt.isMainThread();
    }

    public static void assertMainThread(String methodName) {
        UtilExtKt.assertMainThread(methodName);
    }

    public static void runOnMainThread(final Runnable task) {
        UtilExtKt.runOnMainThread(task);
    }

    public static void runOnMainThread(@NonNull LifecycleOwner owner, final Runnable task) {
        UtilExtKt.runOnMainThread(owner, task);
    }

    /**
     * 保证生命周期安全
     *
     * @param owner
     * @param task
     */
    public static void runOnNextLooper(@NonNull LifecycleOwner owner, final Runnable task) {
        UtilExtKt.runOnNextLooper(owner, task);
    }

    public static void runOnNextLooper(final Runnable task) {
        UtilExtKt.runOnNextLooper(task);
    }

    /**
     * 保证生命周期安全
     *
     * @param owner
     * @param task
     * @param delayMs
     */
    public static void runOnMainDelay(@NonNull LifecycleOwner owner, Runnable task, int delayMs) {
        UtilExtKt.runOnMainDelay(owner, task, delayMs);
    }

    /**
     * 在执行前易泄露
     *
     * @param task
     * @param delayMs
     */
    public static void runOnMainDelay(Runnable task, int delayMs) {
        UtilExtKt.runOnMainDelay(task, delayMs);
    }

    public static <T, R> int findIndex(List<T> list, T param, Function<T, R> get) {
        for (int i = 0; i < list.size(); i++)
            if (param != null && list.get(i) != null && Objects.equals(get.apply(param), get.apply(list.get(i))))
                return i;
        return -1;
    }

    public static String removeEmpty(String s) {
        if (s == null)
            return "";
        return s.replace(" ", "").replace("\n", "").trim();
    }

    public static String removeLineFeed(String s) {
        if (s == null)
            return "";
        return s.replace("\n", "");
    }

    public static String removeDivider(String title) {
        if (title == null)
            return "";
        return title.replace("|", "");
    }

    public static <T, R> boolean listEqual(List<T> list, List<T> list0, Function<T, R> function) {
        if (list == null || list0 == null)
            return list == list0;
        if (list.size() != list0.size())
            return false;
        for (int i = 0; i < list.size(); i++)
            if (fieldEqual(list.get(i), list0.get(i), function))
                return false;
        return true;
    }

    public static <T> boolean isEmpty(List<T> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }


    public static <T, R> boolean fieldEqual(T a, T b, Function<T, R> function) {
        if (a == b)
            return true;
        return a != null && b != null
                && !Objects.equals(function.apply(a), function.apply(b));
    }

    public static BundleBuilder BundleBuild() {
        return new BundleBuilder();
    }

    /**
     * 强转activity的类型
     *
     * @param context
     * @return
     */
    public static FragmentActivity scanForActivity(Context context) {
        if (context instanceof FragmentActivity)
            return (FragmentActivity) context;
        else if (context instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        return null;
    }

    public static int toInt(@Nullable Integer v) {
        if (v == null)
            return 0;
        else
            return v;
    }

    public static boolean toBool(@Nullable Boolean v) {
        if (v == null)
            return false;
        else
            return v;
    }

    public static <T> List<T> filterNull(List<T> data) {
        for (int i = 0; i < data.size(); i++)
            if (data.get(i) == null) {
                List<T> temp = new LinkedList<>(data);
                data.clear();
                for (T entity : temp)
                    if (entity != null)
                        data.add(entity);
            }
        return data;
    }

    /**
     * 根据给定的宽高，用view绘制出一张Bitmap
     *
     * @param view
     * @param width
     * @param height
     * @return
     */
    @NonNull
    public static Bitmap drawBitmapFromView(@NonNull View view, int width, int height) throws OutOfMemoryError {
        Bitmap.Config config;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            config = Bitmap.Config.RGB_565;
        } else {
            config = Bitmap.Config.ARGB_8888;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        //View绘制三部曲
        view.measure(View.MeasureSpec.makeMeasureSpec(canvas.getWidth(), View.MeasureSpec.EXACTLY)
                , View.MeasureSpec.makeMeasureSpec(canvas.getHeight(), View.MeasureSpec.EXACTLY));
        view.layout(0, 0, canvas.getWidth(), canvas.getHeight());
        view.draw(canvas);
        return bitmap;
    }

    public static <T> List<T> getSortSpan(Editable editable, int start, int end, Class<T> clz) {
        T[] spans = editable.getSpans(start, end, clz);
        List<T> result = Arrays.asList(spans);
        Collections.sort(result, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return editable.getSpanStart(o1) - editable.getSpanStart(o2);
            }
        });
        return result;
    }

    public static class BundleBuilder {
        private final Bundle mBundle = new Bundle();

        public BundleBuilder putString(String key, String v) {
            mBundle.putString(key, v);
            return this;
        }

        public BundleBuilder putBoolean(String key, boolean v) {
            mBundle.putBoolean(key, v);
            return this;
        }

        public BundleBuilder putInt(String key, int v) {
            mBundle.putInt(key, v);
            return this;
        }

        public BundleBuilder putLong(String key, long v) {
            mBundle.putLong(key, v);
            return this;
        }

        public Bundle build() {
            return mBundle;
        }
    }

    public interface FieldGetter<FieldType, T> {
        FieldType getField(T obj);
    }

    /**
     * 把list转换成需要的字段集合，通过fieldGetter获取字段，若字段返回空则过滤该项
     *
     * @param list
     * @param fieldGetter
     * @param <FieldType>
     * @param <T>
     * @return
     */
    public static <FieldType, T> List<FieldType> toFieldList(@Nullable Iterable<T> list
            , @NonNull FieldGetter<FieldType, T> fieldGetter) {
        List<FieldType> result = new ArrayList<>();
        if (list == null)
            return result;
        for (T entity : list) {
            FieldType field = fieldGetter.getField(entity);
            if (field != null)
                result.add(field);
        }
        return result;
    }

    /**
     * 把list转换成需要的字段集合，通过fieldGetter获取字段，若字段返回空则过滤该项
     *
     * @param list
     * @param fieldGetter
     * @param <FieldType>
     * @param <T>
     * @return
     */
    public static <FieldType, T> List<FieldType> toFieldList(@Nullable T[] list
            , @NonNull FieldGetter<FieldType, T> fieldGetter) {
        List<FieldType> result = new ArrayList<>();
        if (list == null)
            return result;
        for (T entity : list) {
            FieldType field = fieldGetter.getField(entity);
            if (field != null)
                result.add(field);
        }
        return result;
    }

    public static int getSignature() {
        Context context = ContextUtil.get();
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        int sig = 0;
        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] s = pi.signatures;
            sig = s[0].hashCode();
        } catch (Exception e) {
            Logger.printException(e);
        }
        return sig;
    }

    /**
     * 判断字符是否为中文
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
//        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
//        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
//            return true;
//        }
//        return false;
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    /**
     * 判断字符串中中文的个数
     *
     * @param s
     * @return 中文的个数
     */
    public static int getChineseCount(String s) {
        int count = 0;
//        charArray
        char[] ca = s.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            char c = ca[i];
            if (isChinese(c))
                count++;
        }
        return count;
    }

    /**
     * 判断字符串中高代理项的个数
     * <b>高代理项</b>将与<b>低代理项</b>配对构成一个字
     *
     * @param s
     * @return 高代理项个数
     */
    public static int getHighSurrogate(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isHighSurrogate(s.charAt(i))) {
                count++;
                i++;
            }
        }
        return count;
    }

    private static String APP_CHANNEL = "";

    public static String getApplicationMetaData(String metaKey) {
        try {
            Context mContext = ContextUtil.get();
            ApplicationInfo appInfo = mContext.getApplicationContext().getPackageManager()
                    .getApplicationInfo(mContext.getApplicationContext().getPackageName(),
                            PackageManager.GET_META_DATA);
            String value = appInfo.metaData.getString(metaKey);
            int iValue = -1;
            if (value == null) {
                iValue = appInfo.metaData.getInt(metaKey, -1);
            }
            if (iValue != -1) {
                value = String.valueOf(iValue);
            }
            return value;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Sunday字符串匹配算法
     *
     * @param s 主串
     * @param p 模式串
     * @return
     */
    public static int sunday(String s, String p) {
        char[] sArray = s.toCharArray();
        char[] pArray = p.toCharArray();
        int sLen = s.length();
        int pLen = p.length();
        int i = 0, j = 0;
        while (i <= sLen - pLen + j) {//这句话控制索引i,j的范围
            if (sArray[i] != pArray[j]) {//假如主串的sarry[i]与模式串的parray[j]不相等
                if (i == sLen - pLen + j) {//
                    break;//假如主串的sarry[i]与模式串的parray[j]不相等,并且i=sLen-pLen+j,说明这已经
                    //是在和主串中最后可能相等的字符段比较了,并且不相等,说明后面就再也没有相等的了,所以
                    //跳出循环,结束匹配
                }
                //假如是主串的中间字段与模式串匹配，且结果不匹配
                //则就从模式串的最后面开始,(注意是从后向前)向前遍历,找出模式串的后一位在对应的母串的字符是否在子串中存在
                int pos = contains(pArray, sArray[i + pLen - j]);
                if (pos == -1) {//表示不存在
                    i = i + pLen + 1 - j;
                    j = 0;
                } else {
                    i = i + pLen - pos - j;
                    j = 0;
                }
            } else {//假如主串的sarry[i]与模式串的parray[j]相等,则继续下面的操作
                if (j == pLen - 1) {//判断模式串的索引j是不是已经到达模式串的最后位置，
                    //j==pLen-1证明在主串中已经找到一个模式串的位置,
                    //且目前主串尾部的索引为i,主串首部的索引为i-j,打印模式串匹配的第一个位置
                    return i - j;
//                    //然后主串右移一个位置,再和模式串的首字符比较,从而寻找下一个匹配的位置
//                    i = i - j + 1;
//                    j = 0;
                } else {
                    //假如模式串的索引j!=pLen-1,说明模式串还没有匹配完,则i++,j++继续匹配,
                    i++;
                    j++;
                }
            }
        }
        return -1;
    }

    //注意每次都是从后向前
    public static int contains(char[] str, char ch) {
        for (int i = str.length - 1; i >= 0; i--) {
            if (str[i] == ch) {
                return i;
            }
        }
        return -1;
    }
}
