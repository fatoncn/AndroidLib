package com.cookie.android.util.livedata.observer;

import java.util.List;

import androidx.lifecycle.Observer;

import com.cookie.android.util.Utils;

/**
 * 只有值（基本类型、null）发生改变的时候才会通知
 * Author: ZhangLingfei
 * Date : 2019/7/17 0017
 */
public abstract class PrimitiveObserver<T> implements Observer<T> {

    private final boolean primitiveValue;//当基本类型值发生变化时才会被通知
    private final boolean useDefaultValue;//针对基本类型泛型的null值使用对应默认值
    protected Object oldValue = NOT_SET;
    protected static final Object NOT_SET = new Object();

    public PrimitiveObserver() {
        this(false);
    }

    /**
     * 配置primitiveValue值
     *
     * @param primitiveValue 为true时，若T为基本类型，则只有其值发生变化时才会被通知
     */
    public PrimitiveObserver(boolean primitiveValue) {
        this(primitiveValue, false);
    }

    public PrimitiveObserver(boolean primitiveValue, boolean useDefaultValue) {
        this.primitiveValue = primitiveValue;
        this.useDefaultValue = useDefaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onChanged(T t) {
        if (!primitiveValue || oldValue == NOT_SET || isValueChanged((T) oldValue, t)) {
            if (t == null && useDefaultValue)
                t = getNullDefault();
            onValueChanged(t);
            oldValue = t;
        }
    }

    /**
     * 加入默认值的初衷是为了保证部分基本类型的监听能够稳定更新，
     * 但是部分逻辑需要通过非null值来判定有效性，避免无效的回调，
     * 该情况需要稳定更新的监听器，采用原始Observer即可
     *
     * @return
     */
    private T getNullDefault() {
        Class valueType = Utils.getSuperGenericClass(getClass());
        if (valueType == Integer.class)
            return (T) Integer.valueOf(0);
        else if (valueType == Long.class)
            return (T) Long.valueOf(0);
        else if (valueType == Byte.class)
            return (T) Byte.valueOf((byte) 0);
        else if (valueType == Short.class)
            return (T) new Short((short) 0);
        else if (valueType == String.class || valueType == CharSequence.class)
            return (T) "";
        else if (valueType == Boolean.class)
            return (T) Boolean.valueOf(false);
        return null;
    }

    private boolean isValueChanged(T oldValue, T value) {
        if (oldValue == null)
            return value != null;
        if (value == null)
            return true;
        if (oldValue instanceof List && ((List) oldValue).size() == 0)
            return ((List) value).size() > 0;
        if (oldValue instanceof SizeEntity && ((SizeEntity) oldValue).size() == 0)
            return ((SizeEntity) value).size() > 0;
        boolean isBoolean = oldValue instanceof Boolean;
        boolean isInt = oldValue instanceof Integer;
        boolean isLong = oldValue instanceof Long;
        boolean isByte = oldValue instanceof Byte;
        boolean isShort = oldValue instanceof Short;
        boolean isString = oldValue instanceof String;
        boolean isSimple = oldValue instanceof SimpleEntity;
        if (isSimple && oldValue == value)
            return true;
        return !(isBoolean || isInt || isLong || isByte || isShort || isString || isSimple) || !oldValue.equals(value);
    }

    protected abstract void onValueChanged(T t);

    /**
     * 定义size数据，减少回调次数
     */
    public interface SizeEntity {
        int size();
    }

    public interface SimpleEntity {

    }
}
