package com.cookie.android.util.livedata

import com.cookie.android.util.async.Task

/**
 * 存储式LiveData
 * 根据LiveData源码改造，可实现即时读值写值（存储特性）、延时通知（避免重复通知）
 *
 * @param <T>
</T> */
class Store<T>:StoreImpl<T>{
    constructor() : super()
    constructor(default: T?) : super(default)

    fun modify(transform: T.()->Unit) {
        modify(Task(transform))
    }
}