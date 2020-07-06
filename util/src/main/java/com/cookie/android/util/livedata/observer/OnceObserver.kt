package com.cookie.android.util.livedata.observer

import androidx.lifecycle.LiveData
import com.cookie.android.util.livedata.Store

abstract class OnceObserver<T> : SafeObserver<T> {

    private val mLiveData: LiveData<out T>?
    private val mM0: Store<out T>?

    constructor(liveData: LiveData<out T>) {
        mLiveData = liveData
        mM0 = null
    }

    constructor(liveData: Store<out T>) {
        mLiveData = null
        mM0 = liveData
    }

    override fun onSafeChanged(t: T) {
        mLiveData?.removeObserver(this)
        mM0?.removeObserver(this)
        onGet(t)
    }

    protected abstract fun onGet(t: T)
}
