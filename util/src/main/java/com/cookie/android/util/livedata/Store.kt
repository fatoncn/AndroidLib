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

fun <T, R> Store<T>.valueFrom(live: Live<R>) {
    from(live) {
        value
    }
}

fun <T, R> Live<T>.toValue(live: Store<R>) {
    to(live) {
        live.value
    }
}

fun <T, R> Live<T>.toValueWithOld(live: Store<R>, transformWithOld: T.(R) -> R) {
    to(live) {
        transformWithOld(live.value)
    }
}

fun <T, R> Store<T>.valueFromWithOld(live: Live<R>, transformWithOld: R.(T) -> T) {
    from(live) {
        transformWithOld(value)
    }
}

fun <T, R> Live<T>.toValueWithStore(live: Store<R>, transformWithStore: T.(Store<R>) -> Unit) {
    to(live) {
        transformWithStore(live)
        throw NoTransformAction()
    }
}

fun <T, R> Store<T>.valueFromWithStore(live: Live<R>, transformWithStore: R.(Store<T>) -> Unit) {
    val to = this
    from(live) {
        transformWithStore(to)
        throw NoTransformAction()
    }
}