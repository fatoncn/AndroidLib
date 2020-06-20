package com.cookie.android.util.async

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

open class SafeTask<T>(lifecycleOwner: LifecycleOwner, private var task: Task<T>? = null) : Task<T> {

    constructor(lifecycleOwner: LifecycleOwner, task: ((T) -> Unit)) : this(lifecycleOwner, Task(task)) {

    }

    private var mDestroyed: Boolean = false

    init {
        lifecycleOwner.lifecycle.addObserver(object : SafeLifecycleObserver() {
            override fun onSafeStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    mDestroyed = true
                    task = null
                }
            }
        })
    }

    @CallSuper

    override fun run(param: T) {
        if (mDestroyed)
            return
        task?.run(param)
        safeRun(param)
    }

    open fun safeRun(param: T) {}
}