package com.cookie.android.util

import android.app.Application

object LibConfig {
    private lateinit var application: Application

    fun initCookieLib(app: Application, debug: Boolean = isApkDebug) {
        application = app
        isApkDebug = debug
    }

    var isApkDebug = BuildConfig.DEBUG
        private set
    val app: Application
        get() = application

}