package com.cookie.android.util.livedata

import com.cookie.android.util.runOnMainDelay

/**
 * 用于点击的LiveData
 * Author: ZhangLingfei
 * Date : 2019/11/5 0005
 */
open class LiveClick<T> : LiveEvent<T>(){
    private var intervalMs = 300

    private var mDelayRunnable: Runnable? = null

    fun setIntervalMs(intervalMs: Int): LiveClick<T> {
        this.intervalMs = intervalMs
        return this
    }

    @JvmOverloads
    fun click(it: T? = null) {
        if (mDelayRunnable != null)
            return
        runOnMainDelay(DelayRunnable(), intervalMs)
        postEvent(it)
    }

    private inner class DelayRunnable : Runnable {
        init {
            mDelayRunnable = this
        }

        override fun run() {
            mDelayRunnable = null
        }
    }
}
