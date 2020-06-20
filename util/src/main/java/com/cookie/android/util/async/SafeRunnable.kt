package com.cookie.android.util.async

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

open class SafeRunnable(lifecycleOwner: LifecycleOwner, private var runnable: Runnable? = null) : Runnable {

    constructor(lifecycleOwner: LifecycleOwner, runnable: () -> Unit) : this(lifecycleOwner, Runnable(runnable)) {

    }

    private var mDestroyed: Boolean = false

    init {
        lifecycleOwner.lifecycle.addObserver(object : SafeLifecycleObserver() {
            override fun onSafeStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    runnable = null
                    mDestroyed = true
                }
            }
        })
    }

    @CallSuper
    override fun run() {
        if (mDestroyed)
            return
        runnable?.run()
        safeRun()
    }

    open fun safeRun() {}

}