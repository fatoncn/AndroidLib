package com.cookie.android.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.cookie.android.util.arch.controller.ViewController
import com.cookie.android.util.async.SafeRunnable
import com.cookie.android.util.livedata.Live
import java.util.concurrent.ConcurrentHashMap

/**
 * 扩展Util方法
 * Author: ZhangLingfei
 * Date : 2019/12/27 0027
 */
private val sMainThread = Looper.getMainLooper().thread
private val sUIHandler = Handler(Looper.getMainLooper())

fun isMainThread(): Boolean {
    return Thread.currentThread() === sMainThread
}

fun assertMainThread(methodName: String) {
    check(isMainThread()) {
        ("Cannot invoke " + methodName + " on a background"
                + " thread")
    }
}

fun runOnMainThread(task: Runnable) {
    if (isMainThread())
        task.run()
    else
        runOnNextLooper(task)
}

fun runOnMainThread(task: () -> Unit) {
    runOnMainThread(Runnable(task))
}

fun runOnNextLooper(task: Runnable) {
    sUIHandler.post(task)
}

fun runOnNextLooper(task: () -> Unit) {
    runOnNextLooper(Runnable(task))
}

/**
 * 在执行前易泄露
 *
 * @param task
 * @param delayMs
 */
fun runOnMainDelay(task: Runnable, delayMs: Int) {
    sUIHandler.postDelayed(task, delayMs.toLong())
}

fun runOnMainDelay(task: () -> Unit, delayMs: Int) {
    runOnMainDelay(Runnable(task), delayMs)
}

/**
 * 保证生命周期安全
 *
 * @param owner
 * @param task
 * @param delayMs
 */
fun LifecycleOwner.safeRunOnMainDelay(task: Runnable, delayMs: Int) {
    runOnMainDelay(SafeRunnable(this,task), delayMs)
}

fun LifecycleOwner.safeRunOnMainDelay(task: () -> Unit, delayMs: Int) {
    safeRunOnMainDelay(Runnable(task), delayMs)
}

fun LifecycleOwner.runOnMainThread(task: Runnable) {
    if (isMainThread())
        task.run()
    else
        runOnNextLooper(task)
}

fun LifecycleOwner.runOnMainThread(task: () -> Unit) {
    runOnMainThread(Runnable(task))
}

fun getApp(): Application {
    return ContextUtil.get()
}

fun LifecycleOwner.runOnNextLooper(task: Runnable) {
    sUIHandler.post(SafeRunnable(this,task))
}

fun LifecycleOwner.runOnNextLooper(task: () -> Unit) {
    runOnNextLooper(Runnable(task))
}

private val sActivityInfoMap = ConcurrentHashMap<String, ActivityInfo>()

/**
 * 获取activity在manifest中注册的label
 *
 * @param activity
 * @return
 */
fun Activity.activityLabel(): String {
    if (sActivityInfoMap.isEmpty()) {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val activities = packageInfo.activities
            for (activityInfo in activities)
                sActivityInfoMap[activityInfo.name] = activityInfo
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.printException(e)
        }

    }
    val info = sActivityInfoMap[componentName.className]
    return info?.nonLocalizedLabel?.toString() ?: ""
}

fun <T : ViewModel> FragmentActivity.getViewModel(clazz: Class<T>): T {
    return ViewModelProviders.of(this).get(clazz)
}

fun <T : ViewModel> Fragment.getViewModel(clazz: Class<T>): T {
    return ViewModelProviders.of(this).get(clazz)
}

fun <T : View> ViewController.findViewById(@IdRes resId: Int): T {
    return root.findViewById(resId)
}

fun <T> LifecycleOwner.observe(live: Live<T>, observer: Observer<T>) {
    live.observe(this, observer)
}

fun <T> LifecycleOwner.observe(live: Live<T>,  observer: (T) -> Unit) {
    live.observe(this, Observer(observer))
}

fun <T> LifecycleOwner.observe(live: LiveData<T>, observer: Observer<T>) {
    live.observe(this, observer)
}

fun <T> LifecycleOwner.observe(live: LiveData<T>, observer: (T) -> Unit) {
    live.observe(this, Observer(observer))
}

fun String?.safeTrim(): String {
    return this?.trim() ?: ""
}

fun String?.removeEmpty(): String {
    return Utils.removeEmpty(this)
}

fun resDimenPX(@DimenRes resId: Int) = ContextUtil.get().resources.getDimensionPixelSize(resId)

fun resDimenPXOffset(@DimenRes resId: Int) = ContextUtil.get().resources.getDimensionPixelOffset(resId)

fun resDimenFloatPx(@DimenRes resId: Int) = ContextUtil.get().resources.getDimension(resId)

@Suppress("DEPRECATION")
fun resColor(@ColorRes resId: Int) = ContextUtil.get().resources.getColor(resId)

@Suppress("DEPRECATION")
fun resColorStateList(@ColorRes resId: Int): ColorStateList = ContextUtil.get().resources.getColorStateList(resId)

fun resString(@StringRes resId: Int): String = ContextUtil.get().resources.getString(resId)

fun inflate(@LayoutRes resId: Int, container: ViewGroup = FrameLayout(ContextUtil.get())): View = LayoutInflater.from(ContextUtil.get()).inflate(resId, container, false)

fun inflate(@LayoutRes resId: Int): View = LayoutInflater.from(ContextUtil.get()).inflate(resId, FrameLayout(ContextUtil.get()), false)

fun String?.safeInt(): Int {
    return FormatUtils.toInt(this)
}

fun String?.safeLong(): Long {
    return FormatUtils.toLong(this)
}

fun screenWidth(): Int {
    val conf = DisplayMetrics()
    val wm = ContextUtil.get().getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    wm?.defaultDisplay?.getMetrics(conf)

    val screenSizeOne = conf.widthPixels
    val screenSizeTwo = conf.heightPixels

    val conf1 = ContextUtil.get().resources.configuration
    return when (conf1.orientation) {
        1 -> if (screenSizeOne < screenSizeTwo) screenSizeOne else screenSizeTwo
        2 -> if (screenSizeOne > screenSizeTwo) screenSizeOne else screenSizeTwo
        else -> {
            Log.e("CLDeviceUtil", "can\'t get screen width!")
            screenSizeOne
        }
    }
}

fun screenHeight(): Int {
    val conf = DisplayMetrics()
    val wm = ContextUtil.get().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(conf)
    val screenSizeOne = conf.widthPixels
    val screenSizeTwo = conf.heightPixels

    val conf1 = ContextUtil.get().resources.configuration
    return when (conf1.orientation) {
        1 -> if (screenSizeOne > screenSizeTwo) screenSizeOne else screenSizeTwo
        2 -> if (screenSizeOne < screenSizeTwo) screenSizeOne else screenSizeTwo
        else -> {
            Log.e("CLDeviceUtil", "can\'t get screen height!")
            screenSizeOne
        }
    }
}


fun isMeizuPhone(): Boolean {
    return Build.MANUFACTURER.contains("meizu", true)
}

fun isDoovPhone(): Boolean {
    return Build.MANUFACTURER.contains("doov", true)
}

fun isOppoPhone(): Boolean {
    return Build.MANUFACTURER.contains("oppo", true)
}

fun isVivoPhone(): Boolean {
    return Build.MANUFACTURER.contains("vivo", true)
}

fun isXiaomi(): Boolean {
    return Build.MANUFACTURER.contains("xiaomi", true)
}

fun isSamsung(): Boolean {
    return Build.MANUFACTURER.contains("samsung", true)
}

fun isHuawei(): Boolean {
    return Build.MANUFACTURER.contains("huawei", true)
}