package com.cookie.android.util.livedata.observer

import androidx.lifecycle.LiveData


/**
 * RunnableObserver
 * Author: ZhangLingfei
 * Date : 2019/9/6 0006
 */
class RunnableObserver(val liveData: LiveData<out Any>, private val run: Runnable) : OnceObserver<Any>(liveData) {
    override fun onGet(t: Any) {
        run.run()
    }
}
