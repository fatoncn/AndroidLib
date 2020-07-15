package com.cookie.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.cookie.android.util.livedata.LiveValue
import com.cookie.android.util.livedata.Store

object NetWorkStateManager : BroadcastReceiver() {
    private val mNetWork = Store(NetworkUtils.isNetConnect)

    val netWork: LiveValue<Boolean>
        get() = mNetWork

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val mNetType = NetworkUtils.checkNetWorkState(context)
            Logger.d("现在的网络状态$mNetType")
            // 接口回调，传递当前网络状态的类型
            mNetWork.postValue(NetworkUtils.isNetConnect)
        }
    }

    //===================================单例模式===============================
    /**
     * 开启网络变化的广播监听器
     */
    fun start() {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextUtil.get().registerReceiver(this, filter)
    }
}