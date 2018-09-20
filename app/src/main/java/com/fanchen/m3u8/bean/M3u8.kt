package com.fanchen.m3u8.bean

import com.fanchen.m3u8.M3u8Config
import java.io.File
import java.util.ArrayList

/**
 * M3u8
 * Created by fanchen on 2018/9/12.
 */
data class M3u8(var url: String = "",
                var fileName: String = "",
                var parentUrl: String = "",
                var tsList: ArrayList<M3u8Ts> = ArrayList(), //M3u8 文件的 ts 切片集合
                var tsFileLength: Long = 0L,//第一个Ts 文件的大小 ，用来估算 文件总大小，不一定准确
                var error: Throwable? = null,
                var errorTs: M3u8Ts? = null,
                private var currentt: Int = -1
) {

    fun addTsUrl(url: String, seconds: Float, position: Int) {
        tsList.add(M3u8Ts(url, seconds, position))
    }

    /**
     * 检查文件是否全部下载完成
     */
    fun checkSuccess(): Boolean {
        return getTsDirPath().listFiles().size == tsList.size
    }

    fun checkError(): Boolean {
        val list = getTsDirPath().listFiles()
        return list.size != tsList.size && list.isNotEmpty()
    }

    /**
     * 获取视频总时间
     */
    fun getTimeFloat(): Float {
        var time = 0f
        tsList.forEach { time += it.seconds }
        return time
    }

    /**
     * 获取文件大概大小
     */
    fun getFileLength(): Long {
        return tsFileLength * tsList.size
    }

    @Synchronized
    fun getCurrenttTs(): Int {
        if (currentt == -1) currentt = getTsDirPath().listFiles().size - 1
        return currentt
    }

    @Synchronized
    fun setCurrenttTs(): Int {
        currentt = getCurrenttTs() + 1
        return currentt
    }

    fun getTsDirPath(): File {
        val lastIndexOf = fileName.lastIndexOf(".")
        return if (lastIndexOf != -1) {
            val dirName = fileName.substring(0, lastIndexOf)
            val dir = File(M3u8Config.m3u8Path, dirName)
            if (!dir.exists()) dir.mkdir()
            dir
        } else {
            val dir = File(M3u8Config.m3u8Path, fileName)
            if (!dir.exists()) dir.mkdir()
            dir
        }
    }

    fun getFilePath(): String {
        return File(M3u8Config.m3u8Path, fileName).absolutePath
    }

    /**
     * 格式化文件大小
     */
    fun formtFileLength(): String {
        val length = getFileLength()
        val GB: Long = 1024 * 1024 * 1024
        val MB: Long = 1024 * 1024
        val KB: Long = 1024
        return when {
            length > GB -> "${String.format("%.2f", length / GB.toFloat())}Gb"
            length > MB -> "${String.format("%.2f", length / MB.toFloat())}Mb"
            length > KB -> "${String.format("%.2f", length / MB.toFloat())}Kb"
            else -> "${length}b"
        }
    }

    /**
     * 格式化时间
     */
    fun formtTime(): String {
        val time = getTimeFloat().toLong()
        val hour = (time / 60 / 60).toInt()
        val min = ((time - hour * 3600) / 60).toInt()
        val seconds = (time - hour * 3600 - min * 60).toInt()
        return String.format("%02d:%02d:%02d", hour, min, seconds)
    }

}