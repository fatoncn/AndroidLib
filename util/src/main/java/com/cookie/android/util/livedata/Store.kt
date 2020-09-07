package com.cookie.android.util.livedata

import androidx.lifecycle.Observer
import com.cookie.android.util.async.Task
import com.cookie.android.util.runOnMainThread

/**
 * 存储式LiveData
 * 根据LiveData源码改造，可实现即时读值写值（存储特性）、延时通知（避免重复通知）
 *
 * @param <T>
</T> */
open class Store<T> : StoreImpl<T> {
    constructor() : super()
    constructor(default: T?) : super(default)

    fun modify(transform: T.() -> Unit) {
        modify(Task(transform))
    }

    fun rePost(transform: T.() -> T) {
        postValue(transform(value))
    }

    override fun observeForever(observer: Observer<in T>) {
        runOnMainThread { super.observeForever(observer) }
    }
}