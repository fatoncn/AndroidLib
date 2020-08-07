package com.cookie.android.util.livedata;

import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.cookie.android.util.UtilExtKt;

import org.jetbrains.annotations.NotNull;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static com.cookie.android.util.UtilExtKt.isMainThread;

/**
 * 改动源码后的LiveData，用于事件驱动（如点击事件）的LiveData，如果得到了非空值，则进行通知，并在通知完成后置空。
 * 置空以后，就不会在二次observe进行二次通知了
 * <p>
 * OnceLiveData
 * <p>
 * Author: ZhangLingfei
 * Date : 2019/4/26 0026
 */
//FIXME:消息队列的处理还有待改善
public class LiveEvent<T> extends LivePosterImpl<T> implements LiveSticky<T> {

    private ConcurrentHashMap<ObserverWrapper, Object> mEvents = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ObserverWrapper, Boolean> mStickyMap = new ConcurrentHashMap<>();

    protected ConcurrentHashMap<Observer<? super T>, ObserverWrapper> mObservers =
            new ConcurrentHashMap<>();

    private T mEvent;
    private final Object mStickyLock = new Object();

    @Override
    protected void dispatchingValue(@Nullable ObserverWrapper initiator) {
        if (initiator != null) {
            considerNotify(initiator);
        } else {
            for (ObserverWrapper observerWrapper : mObservers.values()) {
                considerNotify(observerWrapper);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void considerNotify(ObserverWrapper observer) {
        if (!observer.mActive) {
            return;
        }
        // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
        //
        // we still first check observer.active to keep it as the entrance for events. So even if
        // the observer moved to an active state, if we've not received that event, we better not
        // notify for a more predictable notification order.
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false);
            return;
        }
        //noinspection unchecked
        Object event = mEvents.get(observer);
        if (sDisableRunnable == null) {
            Boolean sticky = mStickyMap.get(observer);
            if (sticky != null && sticky && mEvent != null) {
                event = mEvent;
                synchronized (mStickyLock) {
                    if (event == mEvent)
                        mEvent = null;
                }
            }
            if (event != NOT_SET && event != null) {
                if (event == mEvents.get(observer))
                    mEvents.put(observer, NOT_SET);
                observer.mObserver.onChanged((T) event);
            }
        }
    }

    private class AlwaysActiveObserver extends LifecycleBoundObserver {

        AlwaysActiveObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(owner, observer);
        }

        @Override
        boolean shouldBeActive() {
            return true;
        }
    }

    private class LifecycleBoundObserver extends ObserverWrapper implements LifecycleEventObserver {
        @NonNull
        final LifecycleOwner mOwner;

        LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(observer);
            mOwner = owner;
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
        }

        @Override
        public void onStateChanged(@NotNull LifecycleOwner source, @NotNull Lifecycle.Event event) {
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED) {
                removeObserver(mObserver);
                return;
            }
            activeStateChanged(shouldBeActive());
        }

        @Override
        boolean isAttachedTo(LifecycleOwner owner) {
            return mOwner == owner;
        }

        @Override
        void detachObserver() {
            mEvents.remove(this);
            mStickyMap.remove(this);
            mOwner.getLifecycle().removeObserver(this);
        }
    }

//    @Nullable
//    public T getEvent() {
//        Object data = mEvent;
//        if (data != NOT_SET) {
//            //noinspection unchecked
//            return (T) data;
//        }
//        return null;
//    }

    /**
     * 事件通知
     *
     * @param event 若为null则不通知
     */
    public void postEvent(@Nullable T event) {
        postEvent(event, false);
    }

    @Override
    public void postValue(T value) {
        postEvent(value);
    }

    /**
     * 事件的通知是在下一次Looper才会执行，所以这里的回调操作是异步执行的
     *
     * @param event
     * @param sticky
     */
    private void postEvent(@Nullable T event, boolean sticky) {
        if (event != null)
            UtilExtKt.runOnMainDelay(new Runnable() {
                @Override
                public void run() {
                    if (sticky)
                        synchronized (mStickyLock) {
                            mEvent = event;
                        }
                    for (ObserverWrapper wrapper : mEvents.keySet())
                        mEvents.put(wrapper, event);
                    dispatchingValue(null);
                }
            }, postDelayMs);
    }

    /**
     * Sticky事件通知，该类通知会等待{@link #observe(LifecycleOwner, Observer)}
     *
     * @param event 若为null则不通知
     */
    public void postStickyEvent(@Nullable T event) {
        postEvent(event, true);
    }

    public LiveEvent<T> setSticky(boolean sticky) {
        return this;
    }

    public boolean hasObservers() {
        return mObservers.size() > 0;
    }

    @Override
    @MainThread
    public void observeForever(@NonNull Observer<? super T> observer) {
        observeForever(observer, false);
    }

    @Override
    @MainThread
    public void observeStickyForever(@NonNull Observer<? super T> observer) {
        observeForever(observer, true);
    }

    @MainThread
    private void observeForever(@NonNull Observer<? super T> observer, boolean sticky) {
        assertMainThread("observeForever");
        AlwaysActiveAndExistObserver wrapper = new AlwaysActiveAndExistObserver(observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && LifecycleBoundObserver.class.isInstance(existing)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        mEvents.put(wrapper, NOT_SET);
        mStickyMap.put(wrapper, sticky);
        wrapper.activeStateChanged(true);
    }

    private class AlwaysActiveAndExistObserver extends ObserverWrapper {

        AlwaysActiveAndExistObserver(Observer<? super T> observer) {
            super(observer);
        }

        @Override
        boolean shouldBeActive() {
            return true;
        }

    }

    @Override
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observe(owner, observer, false);
    }

    @MainThread
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observe(owner, observer, true);
    }

    @MainThread
    private void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer, boolean sticky) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        mEvents.put(wrapper, NOT_SET);
        mStickyMap.put(wrapper, sticky);
        owner.getLifecycle().addObserver(wrapper);
    }

    @Override
    @MainThread
    public void observeStickyForever(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observeForever(owner, observer, true);
    }

    @Override
    @MainThread
    public void observeForever(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observeForever(owner, observer, false);
    }

    @MainThread
    private void observeForever(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer, boolean sticky) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        AlwaysActiveObserver wrapper = new AlwaysActiveObserver(owner, observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        mEvents.put(wrapper, NOT_SET);
        mStickyMap.put(wrapper, sticky);
        owner.getLifecycle().addObserver(wrapper);
    }

    @Override
    @MainThread
    public void removeObserver(@NonNull final Observer<? super T> observer) {
        assertMainThread("removeObserver");
        ObserverWrapper removed = mObservers.remove(observer);
        if (removed == null) {
            return;
        }
        removed.detachObserver();
        removed.activeStateChanged(false);
        mStickyMap.remove(removed);
        mEvents.remove(removed);
    }

    private static void assertMainThread(String methodName) {
        if (!isMainThread()) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background"
                    + " thread");
        }
    }

    private Runnable sDisableRunnable;

    private class DelayRunnable implements Runnable {

        public DelayRunnable() {
            sDisableRunnable = this;
        }

        @Override
        public void run() {
            sDisableRunnable = null;
        }
    }

    public void disable(int ms) {
        UtilExtKt.runOnMainDelay(new DelayRunnable(), ms);
    }

    /**
     * Starts to listen the given {@code source} LiveData, {@code onChanged} observer will be called
     * when {@code source} value was changed.
     * <p>
     * {@code onChanged} callback will be called only when this {@code MediatorLiveData} is active.
     * <p> If the given LiveData is already added as a source but with a different Observer,
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param source    the {@code LiveData} to listen to
     * @param onChanged The observer that will receive the events
     * @param <S>       The type of data hold by {@code source} LiveData
     */
    @MainThread
    public <S> void addSource(@NonNull LiveData<S> source, @NonNull Observer<? super S> onChanged) {
        SourceCompat<S> e = new SourceCompat<>(source, onChanged);
        SourceCompat<?> existing = mCompatSources.putIfAbsent(source, e);
        if (existing != null && existing.getMObserver() != onChanged) {
            throw new IllegalArgumentException(
                    "This source was already added with the different observer");
        }
        if (existing != null) {
            return;
        }
        if (hasActiveObservers()) {
            e.plug();
        }
    }

    public void clearObservers() {
        assertMainThread("clearObservers");
        for (Observer<? super T> observer : mObservers.keySet()) {
            ObserverWrapper removed = mObservers.get(observer);
            if (removed == null) {
                return;
            }
            removed.detachObserver();
            removed.activeStateChanged(false);
        }
        mStickyMap.clear();
        mObservers.clear();
        mEvents.clear();
    }

    /**
     * Stops to listen the given {@code LiveData}.
     *
     * @param toRemote {@code LiveData} to stop to listen
     * @param <S>      the type of data hold by {@code source} LiveData
     */
    @MainThread
    public <S> void removeSource(@NonNull LiveData<S> toRemote) {
        SourceCompat<?> source = mCompatSources.remove(toRemote);
        if (source != null) {
            source.unplug();
        }
    }
}
