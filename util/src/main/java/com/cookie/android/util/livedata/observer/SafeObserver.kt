package com.cookie.android.util.livedata.observer

/**
 * 保证null安全的Observer，若接收到null则什么都不做
 * @param <T>
</T> */
abstract class SafeObserver<T> : PrimitiveObserver<T> {

    constructor() : super()

    /**
     * 配置PrimitiveObserver
     * @param primitiveValue 为true时，若T为基本类型，则只有其值发生变化时才会被通知
     */
    constructor(primitiveValue: Boolean) : super(primitiveValue)


    /**
     * 配置PrimitiveObserver
     * @param primitiveValue 为true时，若T为基本类型，则只有其值发生变化时才会被通知
     * @param useDefaultValue 为true时，针对基本类型T的null值使用对应默认值
     */
    constructor(primitiveValue: Boolean, useDefaultValue: Boolean) : super(primitiveValue, useDefaultValue)

    override fun onValueChanged(t: T?) {
        if (t != null) {
            onSafeChanged(t)
        }
    }

    protected abstract fun onSafeChanged(t: T)
}
