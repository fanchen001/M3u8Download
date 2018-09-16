package com.fanchen.m3u8.bean

/**
 * M3u8State
 * Created by fanchen on 2018/9/12.
 */
object M3u8State {
    val STETE_NON = -1 // 空闲
    val STETE_DOWNLOAD = 0 //正在下载
    val STETE_STOP = 1 //停止下载
    val STETE_ERROR = 2//下载出错
    val STETE_SUCCESS = 3 // 成功
}