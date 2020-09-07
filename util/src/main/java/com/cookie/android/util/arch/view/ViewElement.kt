package com.cookie.android.util.arch.view

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

interface ViewElement : LifecycleOwner {
    val parentElement: ViewElement?
    fun getChildFragmentManager(): FragmentManager
    val rootActivity:FragmentActivity
    fun <T : ViewModel> getViewModel(clazz:Class<T>):T
}