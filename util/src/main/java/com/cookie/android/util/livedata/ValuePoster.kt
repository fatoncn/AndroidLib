package com.cookie.android.util.livedata

interface ValuePoster<T> {
    fun postValue(value: T)
    val value:T
}