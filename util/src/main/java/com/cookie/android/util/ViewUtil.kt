package com.cookie.android.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.cookie.android.util.LibConfig

fun Int.inflate(context:Context = LibConfig.app): View {
    return LayoutInflater.from(LibConfig.app).inflate(this,FrameLayout(context),false)
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