package com.cookie.android.util.livedata.observer;

import androidx.lifecycle.Observer
import kotlin.Suppress
import kotlin.collections.ArrayList

abstract class ChangeObserve<T> : Observer<T> {

    private var hasSet = false

    override fun onChanged(t: T) {
        if (hasSet)
            onValueChanged(t)
        hasSet = true
    }

    abstract fun onValueChanged(t: T)
}


abstract class DiffObserver<T>(notifyFirstValue: Boolean = true) : Observer<T> {
    private val notSet = Any()
    private var old: Any? = if (notifyFirstValue) null else notSet
    private var fail = false

    protected fun changeFail() {
        fail = true
    }

    @Suppress("UNCHECKED_CAST")
    override fun onChanged(t: T) {
        fail = false
        if (old != notSet)
            onValueChanged(old as T?, t)
        if (!fail)
            old = copy(t)
    }

    abstract fun onValueChanged(oldOne: T?, newOne: T)

    protected abstract fun copy(t: T): Any?
}

abstract class ArrayObserver<T>(notifyFirstValue: Boolean = true) : DiffObserver<Array<T>>(notifyFirstValue) {

    override fun copy(t: Array<T>): Any? {
        return t.copyOf()
    }

}

abstract class ListObserver<T>(notifyFirstValue: Boolean = true) : DiffObserver<List<T>>(notifyFirstValue) {

    override fun copy(t: List<T>): Any? {
        return ArrayList(t)
    }

}


abstract class PrimitiveObserver<T>(notifyFirstValue: Boolean = true) : DiffObserver<T>(notifyFirstValue) {

    override fun copy(t: T): Any? {
        return t
    }

}