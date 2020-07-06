package com.cookie.android.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * 兼容原LiveData的Source实现
 * Author: ZhangLingfei
 * Date : 2019/8/12 0012
 */
internal class SourceCompat<V>(private val mLiveData: LiveData<V>, val mObserver: Observer<in V>) : Observer<V> {
    var mVersion = LivePosterImpl.START_VERSION

    fun plug() {
        mLiveData.observeForever(this)
    }

    fun unplug() {
        mLiveData.removeObserver(this)
    }

    override fun onChanged(v: V?) {
        val version = getCompatLiveDataVersion(mLiveData)
        if (version < -1 || mVersion != version) {
            mVersion = version
            mObserver.onChanged(v)
        }
    }

    private fun getCompatLiveDataVersion(liveData: LiveData<*>): Int {
        return try {
            val field = LiveData::class.java.getDeclaredField("mVersion")
            field.isAccessible = true
            field.getInt(liveData)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            -2
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            -3
        }
    }
}
