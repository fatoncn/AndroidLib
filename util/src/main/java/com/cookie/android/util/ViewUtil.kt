package com.cookie.android.util

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.cookie.android.util.LibConfig

fun Int.inflate(): View {
    return LayoutInflater.from(LibConfig.app).inflate(this,FrameLayout(LibConfig.app),false)
}

fun View.show(){
    visibility = View.VISIBLE
}

fun View.hide(){
    visibility = View.INVISIBLE
}

fun View.gone(){
    visibility = View.GONE
}