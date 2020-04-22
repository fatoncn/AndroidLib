package com.cookie.android.util.livedata.observer

import androidx.lifecycle.LiveData
import com.cookie.android.util.livedata.StoreLiveData

abstract class OnceObserver<T> : SafeObserver<T> {

    private val mLiveData: LiveData<out T>?
    private val mLiveData0: StoreLiveData<out T>?

    constructor(liveData: LiveData<out T>) {
        mLiveData = liveData
        mLiveData0 = null
    }

    constructor(liveData: StoreLiveData<out T>) {
        mLiveData = null
        mLiveData0 = liveData
    }

    override fun onSafeChanged(t: T) {
        mLiveData?.removeObserver(this)
        mLiveData0?.removeObserver(this)
        onGet(t)
    }

    protected abstract fun onGet(t: T)
}
