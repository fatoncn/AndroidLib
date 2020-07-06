package com.cookie.android.util.livedata

/**
 * LivePoster
 * Author: ZhangLingfei
 * Date : 2019/11/5 0005
 */
interface LivePoster<T> : Live<T>{
    fun postValue(value: T)
}
