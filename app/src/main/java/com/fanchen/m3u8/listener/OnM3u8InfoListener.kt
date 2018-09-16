package com.fanchen.m3u8.listener

import com.fanchen.m3u8.bean.M3u8
import com.fanchen.m3u8.bean.M3u8File

/**
 * OnM3u8InfoListener
 * Created by fanchen on 2018/9/12.
 */
interface OnM3u8InfoListener {
    /**
     * 解析m3u8成功
     */
    fun onSuccess(m3u8File: M3u8File, infos: List<M3u8>)

    /**
     * 解析失败
     */
    fun onError(m3u8File: M3u8File, e: Throwable)
}