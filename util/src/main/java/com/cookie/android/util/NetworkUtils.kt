package com.cookie.android.util

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager

object NetworkUtils {
//    const val NET_2G = "2G"
//    const val NET_3G = "3G"
//    const val NET_4G = "4G"
//    const val NET_WIFI = "WIFI"
//    const val NET_UNKNOW = "UNKNOW"
//    const val NET_NO = "NONET"
//
//    /**
//     * 获取   当前网络类型
//     *
//     * @return 返回 当前的网络类型
//     */
//    fun getNetworkType(context: Context): String {
//        val connectivitymanager = context
//                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkinfo = connectivitymanager.activeNetworkInfo
//        return if (networkinfo != null && networkinfo.isAvailable) {
//            val i = networkinfo.type
//            if (i == 1) return NET_WIFI
//            val telephonymanager = context
//                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            when (telephonymanager.networkType) {
//                1, 2, 4, 7, 11 -> return NET_2G
//                3, 5, 6, 8, 9, 10, 12, 14, 15 -> return NET_3G
//                13 -> return NET_4G
//            }
//            NET_UNKNOW
//        } else {
//            NET_NO
//        }
//    }

//    /**
//     * 判断网络是否连接
//     *
//     * @param context
//     * @return
//     */
//    fun isConnected(context: Context): Boolean {
//        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connMgr.activeNetworkInfo
//        return networkInfo != null && networkInfo.isConnected
//    }
//
//    /**
//     * 判断WIFI是否连接
//     *
//     * @param context
//     * @return
//     */
//    fun isWifiConnected(context: Context): Boolean {
//        return isConnected(context, ConnectivityManager.TYPE_WIFI)
//    }
//
//    /**
//     * 判断移动网络是否连接
//     *
//     * @param context
//     * @return
//     */
//    fun isMobileConnected(context: Context): Boolean {
//        return isConnected(context, ConnectivityManager.TYPE_MOBILE)
//    }

//    private fun isConnected(context: Context, type: Int): Boolean {
//        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        //检测API是否小于21，因为API21之后getNetworkInfo(int networkType)方法被弃用
//        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            val networkInfo = connMgr.getNetworkInfo(type)
//            networkInfo != null && networkInfo.isConnected
//        } else {
//            isConnected(connMgr, type)
//        }
//    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun isConnected(connMgr: ConnectivityManager, type: Int): Boolean {
//        val networks = connMgr.allNetworks
//        var networkInfo: NetworkInfo?
//        for (mNetwork in networks) {
//            networkInfo = connMgr.getNetworkInfo(mNetwork)
//            if (networkInfo != null && networkInfo.type == type && networkInfo.isConnected) {
//                return true
//            }
//        }
//        return false
//    }

    //没有连接网络
    const val NETWORK_NONE = -1

    //移动网络
    const val NETWORK_MOBILE = 0

    //无线网络
    const val NETWORK_WIFI = 1

    //其他网络
    const val NETWORK_OTHERS = 2

    /**
     * 检测网络类型
     *
     * @param context
     * @return
     */
    fun checkNetWorkState(context: Context): Int {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            //检测API是否小于21，因为API21之后getNetworkInfo(int networkType)方法被弃用
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //获取WIFI连接的信息
                val wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                //获取移动数据连接的信息
                val mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                return if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected) {
                    NETWORK_WIFI
                } else if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected) {
                    NETWORK_MOBILE
                } else {
                    NETWORK_NONE
                }
            } else {
                val mNetworkInfo = connectivityManager.activeNetworkInfo
                if (mNetworkInfo != null && mNetworkInfo.isAvailable) {
                    return when (mNetworkInfo.type) {
                        ConnectivityManager.TYPE_WIFI -> {
                            //Log.e("------------>", "NETWORK_WIFI");
                            NETWORK_WIFI
                        }
                        ConnectivityManager.TYPE_MOBILE -> {
                            //Log.e("------------>", "NETWORK_MOBILE");
                            NETWORK_MOBILE
                        }
                        else -> {
                            NETWORK_OTHERS
                        }
                    }
                } else {
                    //Log.e("------------>", "NETWORK_NONE");
                    return NETWORK_NONE
                }
            }
        }
        return NETWORK_NONE
    }

    /**
     * 判断网络是否可用
     *
     * @return true 有网, false 没有网络.
     */
    val isNetConnect: Boolean
        get() {
            return when (checkNetWorkState(ContextUtil.get())) {
                NETWORK_WIFI -> {
                    true
                }
                NETWORK_MOBILE -> {
                    true
                }
                NETWORK_NONE -> {
                    false
                }
                else -> {
                    false
                }
            }
        }
}