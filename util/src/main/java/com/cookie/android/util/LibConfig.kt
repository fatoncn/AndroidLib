package com.cookie.android.util

import android.app.Application

object LibConfig {
    private lateinit var application: Application

    fun initCookieLib(app: Application) {
        application = app
    }
    
    val isApkDebug = BuildConfig.DEBUG
    val app: Application
        get() = application

}