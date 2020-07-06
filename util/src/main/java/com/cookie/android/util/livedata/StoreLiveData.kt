package com.cookie.android.util.livedata

@Deprecated(replaceWith = ReplaceWith("Store", "com.cookie.android.util.livedata.Store"), message = "old")
class StoreLiveData<T> : StoreImpl<T> {
    constructor() : super()
    constructor(default: T?) : super(default)
}