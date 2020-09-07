package com.cookie.android.util.livedata

import android.view.View
import androidx.lifecycle.Observer

/**
 * 用LiveClick代理点击事件的传递，以实现点击拦截（如过频繁点击等）
 * Author: ZhangLingfei
 * Date : 2019/4/27 0027
 */
abstract class OnLiveClick : View.OnClickListener {

    private val mLiveClick = LiveViewClick()

    init {
        //点击监听器一般只被View持有，这里永久观察持有外部的Activity是没有关系的
        mLiveClick.observeForever(ClickObserver())
    }

    inner class ClickObserver : Observer<View> {

        override fun onChanged(t: View?) {
            t?.also {
                onViewClick(t)
            }
        }
    }

    override fun onClick(v: View?) {
        if (v == null)
            return
        mLiveClick.click(v)
    }

    abstract fun onViewClick(v: View)
}
