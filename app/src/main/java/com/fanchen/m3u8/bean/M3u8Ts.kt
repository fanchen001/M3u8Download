package com.fanchen.m3u8.bean

/**
 * M3u8Ts
 * Created by fanchen on 2018/9/12.
 */
data class M3u8Ts(var urlPath: String = "",//ts下载地址
                  var seconds: Float = 0f,//ts文件视频时长
                  var position: Int = 0//当前ts在整个 m3u8 文件中的位置
) {
    /**
     * ts真实名稱
     */
    fun getFileName(): String {
        val sp = urlPath.split("/")
        return sp[sp.size - 1]
    }

    /**
     * ts顺序保存名称
     */
    fun getName(): String {
        val sp = urlPath.split(".")
        return if (sp.size == 1) return String.format("%05d", position) else String.format("%05d.%s", position, sp[sp.size - 1])
    }
}