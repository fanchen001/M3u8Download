package com.fanchen.m3u8.listener

import com.fanchen.m3u8.bean.M3u8File
import java.util.*

/**
 * OnM3u8DeleteListener
 * Created by fanchen on 2018/9/19.
 */
interface OnM3u8DeleteListener {
    /**
     *
     */
    fun onDelete(file: M3u8File)

    /**
     *
     */
    fun onDelete(files: LinkedList<M3u8File>)
}