package com.cookie.android.util.livedata;

import java.util.Map;

import androidx.annotation.CallSuper;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.cookie.android.util.livedata.LivePoster;

/**
 * 基类Live实现
 * Author: ZhangLingfei
 * Date : 2019/8/12 0012
 */
abstract class BaseLive<T> implements LivePoster<T> {
    protected static final Object NOT_SET = new Object();
    static final int START_VERSION = -1;
    protected SafeIterableMap<LiveData<?>, SourceCompat<?>> mCompatSources = new SafeIterableMap<>();
    // how many observers are in active state
    private int mActiveCount = 0;
    protected int postDelayMs = 0;

    protected abstract class ObserverWrapper {
        final Observer<? super T> mObserver;
        boolean mActive;
        int mLastVersion = START_VERSION;

        ObserverWrapper(Observer<? super T> observer) {
            mObserver = observer;
        }

        abstract boolean shouldBeActive();

        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        void detachObserver() {
        }

        void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive;
            boolean wasInactive = mActiveCount == 0;
            mActiveCount += mActive ? 1 : -1;
            if (wasInactive && mActive) {
                onActive();
            }
            if (mActiveCount == 0 && !mActive) {
                onInactive();
            }
            if (mActive) {
                dispatchingValue(this);
            }
        }
    }

    protected abstract void dispatchingValue(ObserverWrapper observerWrapper);

    int getVersion(){
        return START_VERSION;
    }

    public void setPostDelayMs(int postDelayMs) {
        this.postDelayMs = postDelayMs;
    }

    /**
     * Returns true if this Live has active observers.
     *
     * @return true if this Live has active observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasActiveObservers() {
        return mActiveCount > 0;
    }

    @CallSuper
    protected void onActive() {
        for (Map.Entry<LiveData<?>, SourceCompat<?>> source : mCompatSources) {
            source.getValue().plug();
        }
    }

    @CallSuper
    protected void onInactive() {
        for (Map.Entry<LiveData<?>, SourceCompat<?>> source : mCompatSources) {
            source.getValue().unplug();
        }
    }
}
