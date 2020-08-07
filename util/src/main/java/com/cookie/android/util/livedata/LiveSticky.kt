package com.cookie.android.util.livedata

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface LiveSticky<T> : Live<T>{
    fun observeSticky(owner: LifecycleOwner, observer:Observer<in T>)
    fun observeStickyForever(owner: LifecycleOwner, observer:Observer<in T>)
    fun observeStickyForever(observer:Observer<in T>)
}