package com.fanchen.m3u8.bean

import com.fanchen.m3u8.M3u8Config
import java.io.File

/**
 * M3u8索引文件
 * Created by fanchen on 2018/9/12.
 */
data class M3u8File(var url: String = "" // m3u8文件索引地址
                    , var m3u8VideoName: String = "" //m3u8下载的视频保存名称
                    , var m3u8Path: String = M3u8Config.m3u8Path //m3u8索引文件本地保存地址
                    , var id: Int = -1,// 数据库保存Id
                    var state: Int = M3u8State.STETE_NON,//下载状态
                    var startTime: Long = System.currentTimeMillis(),//任务添加时间
                    var endTime: Long = -1,//任务结束时间
                    var m3u8: M3u8? = null
) {
    fun fileExists(): Boolean {
        return file().exists()
    }

    fun file(): File {
        val file = File(File(m3u8Path), "${url.hashCode()}.m3u8")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        return file
    }
}