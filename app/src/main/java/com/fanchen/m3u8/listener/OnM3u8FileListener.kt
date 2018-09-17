package com.fanchen.m3u8.listener

import com.fanchen.m3u8.bean.M3u8File
import java.util.*


/**
 * OnM3u8FileListener
 * Created by fanchen on 2018/9/17.
 */
interface OnM3u8FileListener {
    fun onQueryFile(m3u8s:LinkedList<M3u8File>)

    fun onQueryError(e : Throwable)
}