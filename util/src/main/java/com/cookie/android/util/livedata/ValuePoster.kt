package com.cookie.android.util.livedata

/**
 * ValuePoster
 * Author: ZhangLingfei
 * Date : 2019/11/5 0005
 */
interface ValuePoster<T> {
    fun postValue(value: T)
}
