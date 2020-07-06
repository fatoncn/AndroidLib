package com.cookie.android.util.livedata

interface LiveValue<T> : Live<T> {
    val value:T
}