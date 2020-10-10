package com.cookie.android.util.livedata

import androidx.annotation.MainThread
import com.cookie.android.util.Utils

/**
 * LivePoster
 * Author: ZhangLingfei
 * Date : 2019/11/5 0005
 */
interface LivePoster<T> : Live<T>{
    fun postValue(value: T)
    fun <R> from(live: Live<R>, transform: Transform<R, T>)
}

@MainThread
fun <T,R> Live<T>.to(live: LivePoster<R>, value: T.() -> R) {
    Utils.assertMainThread("Live.to(live,transform)")
    live.from(this,value)
}

fun <T,R> LivePoster<T>.from(live: Live<R>, value: R.() -> T) {
    from(live, object : Transform<R, T> {
        override fun transform(from: R): T {
            return from.value()
        }
    })
}