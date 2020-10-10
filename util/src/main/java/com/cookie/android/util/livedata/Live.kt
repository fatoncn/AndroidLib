package com.cookie.android.util.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.cookie.android.util.Utils

/**
 * 具有生命周期的订阅者
 * Author: ZhangLingfei
 * Date : 2019/9/6 0006
 */
interface Live<T> {
    /**
     * 随owner观察，只有owner活跃时才会通知，owner销毁后会移除
     * @param owner
     * @param observer
     */
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)

    /**
     * 随owner观察，在owner销毁前都会通知，owner销毁后会移除
     * @param observer
     */
    fun observeForever(owner: LifecycleOwner, observer: Observer<in T>)

    /**
     * 永久观察（生命周期伴随Store，存在泄漏风险）
     * @param observer
     */
    fun observeForever(observer: Observer<in T>)
    fun removeObserver(observer: Observer<in T>)

}

class NoTransformAction : RuntimeException()

interface Transform<R, T> {
    fun transform(from: R): T
}