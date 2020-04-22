package com.cookie.android.util.livedata.observer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.cookie.android.util.livedata.Live;

public abstract class OnceValidObserver<T> extends SafeObserver<T> {

    private Live<T> mStoreLiveData;
    private LiveData<T> mLiveData;

    public OnceValidObserver(LiveData<T> liveData) {
        mLiveData = liveData;
    }

    public OnceValidObserver(Live<T> liveData) {
        mStoreLiveData = liveData;
    }

    @Override
    protected void onSafeChanged(T v) {
        if (mLiveData != null && onGet(v)) {
            mLiveData.removeObserver(this);
        }
        if (mStoreLiveData != null && onGet(v)) {
            mStoreLiveData.removeObserver(this);
        }
    }

    /**
     * @param t
     * @return 是否取得了有效值，返回true则不再监听
     */
    protected abstract boolean onGet(@NonNull T t);
}
