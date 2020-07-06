package com.cookie.android.util.livedata;

import android.annotation.SuppressLint;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.cookie.android.util.async.Task;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.INITIALIZED;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static com.cookie.android.util.UtilExtKt.assertMainThread;
import static com.cookie.android.util.Utils.runOnMainDelay;

/**
 * {@link Store}的java的实现部分
 * @param <T>
 */
public abstract class StoreImpl<T> extends LivePosterImpl<T> implements LiveValue<T>,ValuePoster<T>  {

    private final Object mDataLock = new Object();
    protected SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers =
            new SafeIterableMap<>();

    private volatile Object mData = NOT_SET;
    private int mVersion = START_VERSION;
    //可以修改LiveData的活跃判断条件
    private Lifecycle.State mActiveState = STARTED;

    private boolean mDispatchingValue;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean mDispatchInvalidated;
    private boolean mValuePosting;
    private final Runnable mPostValueRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mDataLock) {
                mValuePosting = false;
            }
            dispatchingValue(null);
        }
    };

    public StoreImpl() {
    }

    /**
     * 可以定义一个默认值，以避免null值
     *
     * @param defaultValue
     */
    public StoreImpl(T defaultValue) {
        postValue(defaultValue);
    }

    private SafeIterableMap<StoreImpl<?>, Source<?>> mSources = new SafeIterableMap<>();

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

    /**
     * Stops to listen the given {@code LiveData}.
     *
     * @param toRemote {@code LiveData} to stop to listen
     * @param <S>      the type of data hold by {@code source} LiveData
     */
    @MainThread
    public <S> void removeSource(@NonNull LiveData<S> toRemote) {
        SourceCompat<?> sourceCompat = mCompatSources.remove(toRemote);
        if (sourceCompat != null) {
            sourceCompat.unplug();
        }
    }

    @MainThread
    public <S> void addSource(@NonNull StoreImpl<S> source, @NonNull Observer<? super S> onChanged) {
        Source<S> e = new Source<>(source, onChanged);
        Source<?> existing = mSources.putIfAbsent(source, e);
        if (existing != null && existing.mObserver != onChanged) {
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

    @MainThread
    public <S> void removeSource(@NonNull StoreImpl<S> toRemote) {
        Source<?> source = mSources.remove(toRemote);
        if (source != null) {
            source.unplug();
        }
    }

    /**
     * Returns true if this Live has observers.
     *
     * @return true if this Live has observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasObservers() {
        return mObservers.size() > 0;
    }

    @CallSuper
    @Override
    protected void onActive() {
        super.onActive();
        for (Map.Entry<StoreImpl<?>, Source<?>> source : mSources) {
            source.getValue().plug();
        }
    }

    @CallSuper
    @Override
    protected void onInactive() {
        super.onInactive();
        for (Map.Entry<StoreImpl<?>, Source<?>> source : mSources) {
            source.getValue().unplug();
        }
    }

    private static class Source<V> implements Observer<V> {
        final StoreImpl<V> mStore;
        final Observer<? super V> mObserver;
        int mVersion = START_VERSION;

        Source(StoreImpl<V> store, final Observer<? super V> observer) {
            mStore = store;
            mObserver = observer;
        }

        void plug() {
            mStore.observeForever(this);
        }

        void unplug() {
            mStore.removeObserver(this);
        }

        @Override
        public void onChanged(@Nullable V v) {
            int version = mStore.getVersion();
            if (version < -1 || mVersion != version) {
                mVersion = version;
                mObserver.onChanged(v);
            }
        }
    }

    public void setActiveState(Lifecycle.State activeState) {
        if (activeState.isAtLeast(INITIALIZED))
            mActiveState = activeState;
    }

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
        if (observer.mLastVersion >= mVersion) {
            return;
        }
        observer.mLastVersion = mVersion;
        //noinspection unchecked
        observer.mObserver.onChanged((T) mData);
    }

    @Override
    protected void dispatchingValue(@Nullable ObserverWrapper initiator) {
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
            return;
        }
        mDispatchingValue = true;
        do {
            mDispatchInvalidated = false;
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } else {
                for (Iterator<Map.Entry<Observer<? super T>, ObserverWrapper>> iterator =
                     mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                    considerNotify(iterator.next().getValue());
                    if (mDispatchInvalidated) {
                        break;
                    }
                }
            }
        } while (mDispatchInvalidated);
        mDispatchingValue = false;
    }

    /**
     * 随owner观察，只有owner活跃时才会通知，当owner销毁时会移除
     * @param owner
     * @param observer
     */
    @Override
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
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
        owner.getLifecycle().addObserver(wrapper);
    }

    @MainThread
    @Override
    public void observeForever(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
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
        owner.getLifecycle().addObserver(wrapper);
    }

    @Override
    @MainThread
    public void observeForever(@NonNull Observer<? super T> observer) {
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
        wrapper.activeStateChanged(true);
    }

    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
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
    }

    /**
     * Removes all observers that are tied to the given {@link LifecycleOwner}.
     *
     * @param owner The {@code LifecycleOwner} scope for the observers to be removed.
     */
    @SuppressWarnings("WeakerAccess")
    @MainThread
    public void removeObservers(@NonNull final LifecycleOwner owner) {
        assertMainThread("removeObservers");
        for (Map.Entry<Observer<? super T>, ObserverWrapper> entry : mObservers) {
            if (entry.getValue().isAttachedTo(owner)) {
                removeObserver(entry.getKey());
            }
        }
    }

    /**
     * Posts a task to a main thread to set the given value. So if you have a following code
     * executed in the main thread:
     * <pre class="prettyprint">
     * liveData.postValue("a");
     * liveData.setValue("b");
     * </pre>
     * The value "b" would be set at first and later the main thread would override it with
     * the value "a".
     * <p>
     * If you called this method multiple times before a main thread executed a posted task, only
     * the last value would be dispatched.
     *
     * @param value The new value
     */

    @Override
    public void postValue(T value) {
        boolean postTask;
        synchronized (mDataLock) {
            postTask = !mValuePosting;
            mValuePosting = true;
            mData = value;
        }
        mVersion++;
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
        }
        if (!postTask) {
            return;
        }
        runOnMainDelay(mPostValueRunnable, postDelayMs);
    }

    public void update() {
        if (mData != NOT_SET)
            postValue((T) mData);
    }

    /**
     * 基于现有的值修改后重新通知
     *
     * @param fun
     */
    public void modify(@NonNull Task<T> fun) {
        fun.run(getValue());
        postValue(getValue());
    }

    @Override
    public T getValue() {
        Object data = mData;
        if (data != NOT_SET) {
            //noinspection unchecked
            return (T) data;
        }
        return null;
    }

    @Override
    int getVersion() {
        return mVersion;
    }

    private class AlwaysActiveObserver extends LifecycleBoundObserver{

        AlwaysActiveObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(owner, observer);
        }

        @Override
        boolean shouldBeActive() {
            return true;
        }
    }

    @SuppressLint("RestrictedApi")
    private class LifecycleBoundObserver extends ObserverWrapper implements LifecycleEventObserver {
        @NonNull
        final LifecycleOwner mOwner;

        LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(observer);
            mOwner = owner;
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(mActiveState);
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
            mOwner.getLifecycle().removeObserver(this);
        }
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
}
