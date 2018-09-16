package com.fanchen.m3u8

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File


/**
 * M3u8Config
 * Created by fanchen on 2018/9/12.
 */
@SuppressLint("StaticFieldLeak")
object M3u8Config {
    var readTimeout = 60 * 1000//读取超时时间
    var connTimeout = 60 * 1000//链接超时时间
    var m3u8Path = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}M3u8Download"//默认下载保存地址
    var isDebug = true
    var context: Context? = null
    var threadCount = 5 //默认5线程下载，最高支持5线程
        set(value) {
            field = when {
                value <= 0 -> 3
                value > 5 -> 5
                else -> value
            }
        }

    fun log(msg: String) {
        if (!isDebug) return
        Log.e("M3u8Download", msg)
    }

}