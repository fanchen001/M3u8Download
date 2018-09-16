package com.fanchen.m3u8.listener

import com.fanchen.m3u8.bean.M3u8
import com.fanchen.m3u8.bean.M3u8Ts

/**
 * OnM3u8DownloadListenr
 * Created by fanchen on 2018/9/12.
 */
interface OnM3u8DownloadListenr {
    /**
     * 开始下载
     */
    fun onStart(m3u8: M3u8)

    /**
     * 下载出错
     */
    fun onError(m3u8: M3u8, ts: M3u8Ts,error: Throwable)

    /**
     * 停止下载
     */
    fun onStop(m3u8: M3u8)

    /**
     * 正在合并文件
     */
    fun onMerge(m3u8: M3u8)

    /**
     * 下載，合併完成
     */
    fun onSuccess(m3u8: M3u8)

    /**
     * 更新进度
     */
    fun onProgress(m3u8: M3u8, ts: M3u8Ts, total: Int, current: Int)
}