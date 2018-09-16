package com.fanchen.m3u8

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.fanchen.m3u8.bean.M3u8
import com.fanchen.m3u8.bean.M3u8File
import com.fanchen.m3u8.bean.M3u8State
import com.fanchen.m3u8.bean.M3u8Ts
import com.fanchen.m3u8.db.M3u8FileDatabase
import com.fanchen.m3u8.listener.OnM3u8DownloadListenr
import com.fanchen.m3u8.listener.OnM3u8InfoListener
import com.fanchen.m3u8.util.M3u8Util
import java.io.BufferedReader
import java.io.StringReader
import java.util.*
import java.lang.Thread.interrupted
import java.util.concurrent.Executors

/**
 * M3u8Manager
 * Created by fanchen on 2018/9/12.
 */
object M3u8Manager : Runnable {
    val DOWNLOAD_START = 0
    val DOWNLOAD_STOP = 1
    val DOWNLOAD_SUCCESS = 2
    val DOWNLOAD_ERROR = 3
    val DOWNLOAD_PROGRESS = 4
    val DOWNLOAD_MERGE = 5
    val PRESE_SUCCESS = 6
    val PRESE_ERROR = 7

    private var quit = false//停止的标记.
    private var task: M3u8DownloadTask? = null
    private val lock = java.lang.Object()
    private val infoListeners = LinkedList<OnM3u8InfoListener>()
    private val downListeners = LinkedList<OnM3u8DownloadListenr>()
    private val mHandler = TaskHandler()
    private val executor = Executors.newFixedThreadPool(3)
    private var database: M3u8FileDatabase? = null
    private val queue = LinkedList<M3u8>()
    private var databaseTask: List<M3u8File>? = null
    private var restart = false;

    init {
        try {
            if (M3u8Config.context != null) database = M3u8FileDatabase(M3u8Config.context!!)
            executor.execute(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun start() {
        if (databaseTask == null) {
            restart = true
        } else {
            val tasks = databaseTask?.filter { it.state != M3u8State.STETE_SUCCESS && it.state != M3u8State.STETE_STOP }
            tasks?.forEach { executor.execute(M3u8FileRunnable(it)) }
        }
    }

    fun start(m3u8File: M3u8File) {
        var m3u8: M3u8? = null
        queue.forEach { if (m3u8File.url == it.parentUrl) m3u8 = it }
        if (m3u8 == null) {//任务不存在下载列表中
            download(m3u8File)
        }
    }

    fun getTasks(): List<M3u8File>? {
        return databaseTask
    }

    fun stop() {
        queue.clear()
        task?.stop()
    }

    fun stop(m3u8File: M3u8File) {
        var m3u8: M3u8? = null
        queue.forEach {
            if (m3u8File.url == it.parentUrl)
                m3u8 = it
        }
        if (m3u8 != null) {
            queue.remove(m3u8!!)
        } else if (m3u8File.url == task?.m3u8?.parentUrl) {
            task?.stop()
        }
    }

    /**
     *
     */
    fun download(url: String) {
        download(M3u8File(url, "${url.hashCode()}.mp4"))
    }

    /**
     *
     */
    fun download(m3u8File: M3u8File) {
        try {
            executor.execute(M3u8FileRunnable(m3u8File))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    fun download(m3u8: M3u8) {
        for (m3u in queue) {//判斷任務是否已經存在
            if (m3u.parentUrl == m3u8.parentUrl) return
        }
        try {
            synchronized(lock) {
                queue.add(m3u8)
                lock.notify()
            }//没有执行项时等待
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun download(m3u8s: List<M3u8>) {
        m3u8s.forEach { download(it) }
    }

    fun destroy() {
        cancel(true)
        infoListeners.clear()
        downListeners.clear()
        executor.shutdownNow()
    }

    fun registerInfoListeners(infoListener: OnM3u8InfoListener) {
        if (!infoListeners.contains(infoListener)) {
            infoListeners.add(infoListener)
        }
    }

    fun unregisterInfoListeners(infoListener: OnM3u8InfoListener) {
        infoListeners.remove(infoListener)
    }

    fun registerDownloadListeners(downloadListenr: OnM3u8DownloadListenr) {
        if (!downListeners.contains(downloadListenr)) {
            downListeners.add(downloadListenr)
        }
    }

    fun unregisterDownloadListeners(downloadListenr: OnM3u8DownloadListenr) {
        downListeners.remove(downloadListenr)
    }

    fun updateAndInsert(url: String, file: M3u8File) {
        if (database == null) return
        try {
            executor.execute {
                if (database!!.isExists(file)) {
                    database!!.update(url, file)
                } else {
                    database!!.insert(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun update(url: String, state: Int) {
        if (database == null) return
        try {
            executor.execute {
                val file = database!!.query(url) ?: return@execute
                file.state = state
                if (file.state == M3u8State.STETE_SUCCESS) file.endTime = System.currentTimeMillis()
                database!!.update(url, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 描述：终止队列释放线程.
     */
    fun cancel(interrupt: Boolean) {
        try {
            quit = true
            task?.stop()
            if (interrupt) {
                interrupted()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSize(): Int {
        return queue.size
    }

    override fun run() {
        databaseTask = database?.queryAll()
        if (restart) {
            restart = false
            start()
        }
        while (!quit) {
            while (queue.size > 0) {
                val removeAt = queue.removeAt(0)
                M3u8Config.log("${removeAt.url} => download ts start")
                task = M3u8DownloadTask(removeAt, mHandler).start()
                M3u8Config.log("${removeAt.url} => download ts end")
            }
            try {
                M3u8Config.log("thread wait")
                synchronized(lock) { lock.wait() }//没有执行项时等待
            } catch (e: Exception) {
                if (quit) queue.clear()//被中断的是退出就结束，否则继续
            }
        }
    }

    class M3u8FileRunnable(private var m3u8File: M3u8File) : Runnable {

        override fun run() {
            try {
                val list = readM3u8()
                if (list == null || list.isEmpty()) {
                    m3u8File.state = M3u8State.STETE_NON
                    mHandler.sendMessage(M3u8Manager.PRESE_ERROR, m3u8File, Throwable("M3u8 list null"))
                } else {
                    m3u8File.state = M3u8State.STETE_ERROR
                    mHandler.sendMessage(M3u8Manager.PRESE_SUCCESS, m3u8File, list)
                }
            } catch (e: Throwable) {
                m3u8File.state = M3u8State.STETE_ERROR
                mHandler.sendMessage(M3u8Manager.PRESE_ERROR, m3u8File, e)
            }
        }

        private fun readM3u8(): List<M3u8>? {
            val file = m3u8File.file()
            return if (file.exists()) {//索引文件已下載
                M3u8Config.log("${m3u8File.url} => m3u8 exists")
                val reader = StringReader(String(file.readBytes()))
                val lines = BufferedReader(reader).readLines()
                M3u8Util.lines2M3u8s(lines, m3u8File)
            } else {//索引文件沒有下載
                M3u8Config.log("${m3u8File.url} => start down")
                val entry = M3u8Util.url2Entry(m3u8File.url) ?: return null
                M3u8Config.log("${m3u8File.url} => down success")
                m3u8File.url = entry.url
                file.createNewFile()
                file.writeBytes(entry.bytes)
                val lines = BufferedReader(StringReader(String(entry.bytes))).readLines()
                M3u8Util.lines2M3u8s(lines, m3u8File)
            }
        }

    }

    class TaskHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message?) {
            val objs = msg?.obj as? Array<Any> ?: return
            when (msg.what) {
                DOWNLOAD_PROGRESS -> downListeners.forEach {
                    it.onProgress(objs[0] as M3u8, objs[1] as M3u8Ts, objs[2] as Int, objs[3] as Int)
                }
                DOWNLOAD_MERGE -> downListeners.forEach {
                    it.onMerge(objs[0] as M3u8)
                }
                DOWNLOAD_START -> {
                    val m3u8 = objs[0] as M3u8
                    update(m3u8.parentUrl, M3u8State.STETE_DOWNLOAD)
                    downListeners.forEach {
                        it.onStart(m3u8)
                    }
                }
                DOWNLOAD_STOP -> {
                    val m3u8 = objs[0] as M3u8
                    update(m3u8.parentUrl, M3u8State.STETE_STOP)
                    downListeners.forEach {
                        it.onStop(m3u8)
                    }
                }
                DOWNLOAD_SUCCESS -> {
                    val m3u8 = objs[0] as M3u8
                    update(m3u8.parentUrl, M3u8State.STETE_SUCCESS)
                    downListeners.forEach {
                        it.onSuccess(m3u8)
                    }
                }
                DOWNLOAD_ERROR -> {
                    val m3u8 = objs[0] as M3u8
                    update(m3u8.parentUrl, M3u8State.STETE_ERROR)
                    downListeners.forEach {
                        it.onError(m3u8, objs[1] as M3u8Ts, objs[2] as Throwable)
                    }
                }
                PRESE_SUCCESS -> {
                    val file = objs[0] as M3u8File
                    file.state = M3u8State.STETE_NON
                    updateAndInsert(file.url, file)
                    if (file.id == -1)
                        infoListeners.forEach {
                            it.onSuccess(file, objs[1] as List<M3u8>)
                        }
                }
                PRESE_ERROR -> {
                    val file = objs[0] as M3u8File
                    file.state = M3u8State.STETE_ERROR
                    updateAndInsert(file.url, file)
                    if (file.id == -1)
                        infoListeners.forEach {
                            it.onError(file, objs[1] as Throwable)
                        }
                }
            }
        }

        fun sendMessage(what: Int, vararg args: Any) {
            val msg = Message.obtain()
            msg.what = what
            msg.obj = args
            sendMessage(msg)
        }

    }
}