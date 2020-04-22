package com.cookie.android.util.livedata;

/**
 * 预留类型（类LiveData）
 * Author: ZhangLingfei
 * Date : 2019/10/12 0012
 */
public abstract class LiveValue<T> extends BaseLive<T>{
    public abstract T getValue();
}
