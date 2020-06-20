package com.cookie.android.util.async;

/**
 * Task
 * Author: ZhangLingfei
 * Date : 2019/4/3 0003
 */
public interface Task<T> {
    void run(T param);
}
