package com.cookie.android.util.arch.view

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

interface ViewElement : LifecycleOwner {
    fun getRootActivity(): FragmentActivity
    fun getParentElement(): ViewElement?
    fun getChildFragmentManager(): FragmentManager
}