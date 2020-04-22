package com.cookie.android.util.livedata.observer

import androidx.lifecycle.LiveData
import com.cookie.android.util.Task

/**
 * TaskObserver
 * Author: ZhangLingfei
 * Date : 2019/9/6 0006
 */
class TaskObserver<V>(val liveData: LiveData<out V>, private val run: Task<V>) : OnceObserver<V>(liveData) {
    override fun onGet(t: V) {
        run.run(t)
    }

}
