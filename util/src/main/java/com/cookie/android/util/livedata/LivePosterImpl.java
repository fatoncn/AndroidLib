package com.cookie.android.util.livedata;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;


import org.jetbrains.annotations.NotNull;

import static com.cookie.android.util.Utils.assertMainThread;

/**
 * 基类Live实现
 * Author: ZhangLingfei
 * Date : 2019/8/12 0012
 */
abstract class LivePosterImpl<T> implements LivePoster<T> {
    protected static final Object NOT_SET = new Object();
    static final int START_VERSION = -1;
    // how many observers are in active state
    private int mActiveCount = 0;
    protected int postDelayMs = 0;

    protected SafeIterableMap<Live<?>, Transform<?, T>> storeSourceMap = new SafeIterableMap<>();

    @Override
    @MainThread
    public <R> void from(@NotNull Live<R> live, @NotNull Transform<R, T> transform) {
        assertMainThread("LivePosterImpl.from(live,transform)");
        Transform<?, T> exist = storeSourceMap.putIfAbsent(live, transform);
        if (exist != null && exist != transform) {
            throw new DuplicateSourceError();
        }
        if (exist != null) {
            return;
        }
        //禁止循环数据流
        if (live instanceof LivePosterImpl && ((LivePosterImpl<R>) live).storeSourceMap.get(this) != null) {
            throw new RecursiveSourceError();
        }
        live.observeForever(new Observer<R>() {
            @Override
            public void onChanged(R r) {
                try {
                    postValue(transform.transform(r));
                } catch (NoTransformAction ignored) {

                }
            }
        });
    }

    static class DuplicateSourceError extends IllegalStateException {
        DuplicateSourceError() {
            super("cannot add duplicate source with different transform!");
        }
    }

    static class RecursiveSourceError extends IllegalStateException {
        RecursiveSourceError() {
            super("cannot add recursive source!");
        }
    }

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

    int getVersion() {
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
    }

    @CallSuper
    protected void onInactive() {
    }
}
