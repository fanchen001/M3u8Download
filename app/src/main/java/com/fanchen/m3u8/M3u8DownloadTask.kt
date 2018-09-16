package com.fanchen.m3u8

import com.fanchen.m3u8.bean.M3u8
import com.fanchen.m3u8.bean.M3u8Ts
import com.fanchen.m3u8.util.MergeUtil
import com.fanchen.m3u8.util.URLUtil
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * M3u8DownloadTask
 * Created by fanchen on 2018/9/12.
 */
class M3u8DownloadTask(var m3u8: M3u8, var handler: M3u8Manager.TaskHandler) : Runnable {

    var isRunning = false
    private var executor: ExecutorService? = null//10个线程池

    @Synchronized
    fun start(): M3u8DownloadTask {
        if (isRunning) return this
        handler.sendMessage(M3u8Manager.DOWNLOAD_START, m3u8)
        this.run()
        return this
    }

    @Synchronized
    fun stop() {
        if (!isRunning) return
        handler.sendMessage(M3u8Manager.DOWNLOAD_STOP, m3u8)
        executor?.shutdownNow()
        isRunning = false
    }

    override fun run() {
        try {
            isRunning = true
            m3u8.error = null
            m3u8.errorTs = null
            executor?.shutdownNow()
            executor = Executors.newFixedThreadPool(M3u8Config.threadCount)
            val downloadList = m3u8.tsList.filter { !File(m3u8.getTsDirPath(), it.getName()).exists() }//過濾掉已近完成下載的ts
            downloadList.forEach { executor?.execute(DownloadRunnable(m3u8, it, executor, handler)) }//開始下載
            executor?.shutdown()//下载完成之后要关闭线程池
            while (executor != null && !executor!!.isTerminated) {
                Thread.sleep(500)//等待下载完成
            }
            if (isRunning && m3u8.checkSuccess()) {//执行到
                handler.sendMessage(M3u8Manager.DOWNLOAD_MERGE, m3u8)
                MergeUtil.merge(m3u8, m3u8.getFilePath())//合并文件
                MergeUtil.deleteDirectory(m3u8.getTsDirPath().absolutePath)//合并完成之后，删除ts零时文件
                handler.sendMessage(M3u8Manager.DOWNLOAD_SUCCESS, m3u8)
            } else if (m3u8.error == null) {
                handler.sendMessage(M3u8Manager.DOWNLOAD_STOP, m3u8)
            } else if (m3u8.error != null && m3u8.errorTs != null) {
                handler.sendMessage(M3u8Manager.DOWNLOAD_ERROR, m3u8,  m3u8.errorTs!!,m3u8.error!!)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            isRunning = false
            executor = null
        }
    }

    class DownloadRunnable(private var m3u8: M3u8, private var ts: M3u8Ts, private var executor: ExecutorService?, private var handler: M3u8Manager.TaskHandler) : Runnable {

        override fun run() {
            try {
                val file = File(m3u8.getTsDirPath(), ts.getName())
                if (file.exists()) file.createNewFile()
                file.writeBytes(url2Byte(URLUtil.getUrl(m3u8.url, ts.urlPath)))
                val size = m3u8.tsList.size
                val currentt = m3u8.setCurrenttTs()
                handler.sendMessage(M3u8Manager.DOWNLOAD_PROGRESS, m3u8, ts, size, currentt)
            } catch (e: Exception) {
                m3u8.error = e
                m3u8.errorTs = ts
                executor?.shutdownNow()//下载出现异常，停止本次m3u8下载
            }
        }

        private fun url2Byte(url: String): ByteArray {
            val conn = URL(url).openConnection() as? HttpURLConnection ?: throw Exception("openConnection error")
            conn.readTimeout = M3u8Config.readTimeout
            conn.connectTimeout = M3u8Config.connTimeout
            if (conn.responseCode in 200..300) return conn.inputStream.readBytes()
            throw Exception("$url => responseCode error ${conn.responseCode}")
        }
    }

}