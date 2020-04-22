package com.cookie.android.util.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * 具有生命周期的订阅者
 * Author: ZhangLingfei
 * Date : 2019/9/6 0006
 */
interface Live<T> {
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)
    fun observeForever(observer: Observer<in T>)
    fun removeObserver(observer: Observer<in T>)
}