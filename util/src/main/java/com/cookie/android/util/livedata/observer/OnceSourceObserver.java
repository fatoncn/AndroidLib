package com.cookie.android.util.livedata.observer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.cookie.android.util.livedata.StoreLiveData;

public abstract class OnceSourceObserver<T> extends SafeObserver<T> {

    private LiveData<T> mSource;
    private MediatorLiveData mLiveData;
    private StoreLiveData mLiveData0;

    public OnceSourceObserver(MediatorLiveData liveData, LiveData<T> source) {
        mLiveData = liveData;
        mSource = source;
    }

    public OnceSourceObserver(StoreLiveData liveData, LiveData<T> source) {
        mLiveData0 = liveData;
        mSource = source;
    }

    @Override
    protected final void onSafeChanged(T t) {
        if (mSource != null) {
            if (mLiveData != null)
                mLiveData.removeSource(mSource);
            if (mLiveData0!=null)
                mLiveData0.removeSource(mSource);
            onGet(t);
        }
    }

    protected abstract void onGet(@NonNull T t);
}
