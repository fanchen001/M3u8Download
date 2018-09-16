package com.fanchen.m3u8.util

import com.fanchen.m3u8.M3u8Config
import com.fanchen.m3u8.bean.M3u8
import com.fanchen.m3u8.bean.M3u8File
import java.io.BufferedReader
import java.io.InputStream
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * M3u8Util
 * Created by Administrator on 2018/9/12.
 */
object M3u8Util {

    /**
     *
     * 将lines 转换成M3u8
     */
    fun lines2M3u8s(lines: List<String>, m3u8File: M3u8File): List<M3u8> {
        M3u8Config.log("lines2M3u8s start")
        val m3u8List = ArrayList<M3u8>()
        val checkList = checkM3u8List(lines)
        if (checkList.isNotEmpty()) {
            checkList.forEach {
                val entry = url2Entry(URLUtil.getUrl(m3u8File.url, it))
                if (entry != null) {
                    val subLines = BufferedReader(StringReader(String(entry.bytes))).readLines()
                    m3u8File.url = entry.url
                    m3u8List.add(lines2M3u8(subLines, m3u8File))
                }
            }
        } else {
            m3u8List.add(lines2M3u8(lines, m3u8File))
        }
        M3u8Config.log("M3u8List size => ${m3u8List.size}")
        return m3u8List
    }

    private fun lines2M3u8(lines: List<String>, m3u8File: M3u8File): M3u8 {
        val m3u8 = M3u8(m3u8File.url, m3u8File.m3u8VideoName,m3u8File.url)
        var seconds = 0f
        var position = 0
        lines.forEach {
            if (it.startsWith("#EXTINF:")) {
                seconds = readTime(it)
            } else if (!it.startsWith("#")) {
                m3u8.addTsUrl(it, seconds, position++)
                if (m3u8.tsList.size == 1) m3u8.tsFileLength = getTsFileLength(URLUtil.getUrl(m3u8.url, m3u8.tsList[0].urlPath))
                seconds = 0f
            }
        }
        return m3u8
    }

    fun url2Entry(url: String): Entry? {
        val conn = URL(url).openConnection() as? HttpURLConnection ?: return null
        try {
            conn.readTimeout = M3u8Config.readTimeout
            conn.connectTimeout = M3u8Config.connTimeout
            conn.instanceFollowRedirects = false//自己处理重定向
            conn.connect()
            M3u8Config.log("$url responseCode => ${conn.responseCode}")
            if (conn.responseCode in 200..300) {//200 - 299
                val cedoing = conn.getHeaderField("Content-Encoding")
                val bytes = warpInputStream(cedoing, conn.inputStream).readBytes()
                val urlString = conn.url.toString()
                M3u8Config.log("$urlString read success")
                return Entry(bytes, urlString)
            } else if (conn.responseCode == 301 || conn.responseCode == 302) {//重定向
                val rUrl = URLUtil.getUrl(url, conn.getHeaderField("Location"))
                M3u8Config.log("$url goto => $rUrl")
                return url2Entry(rUrl)
            }
        }catch (e:Exception){
            M3u8Config.log("$url Exception => $e")
        }
        return null
    }

    /***
     * 一级索引 (可以这一层索引，直接是下面的二级索引）
     */
    fun checkM3u8List(lines: List<String>): List<String> {
        return lines.filter { it.endsWith("m3u8") || it.endsWith("m3u") }
    }

    /**
     *  包装InputStream 进行解压等操作
     */
    fun warpInputStream(type: String?, inputStream: InputStream): InputStream {
        return when {
            "gzip" == type?.toLowerCase() -> GZIPInputStream(inputStream)
            "deflate" == type?.toLowerCase() -> InflaterInputStream(inputStream, Inflater(true))
            else -> inputStream
        }
    }

    /**
     * 获取第一个ts文件长度
     */
    fun getTsFileLength(tsUrl: String): Long {
        val conn = URL(tsUrl).openConnection() as? HttpURLConnection ?: return 0
        conn.readTimeout = M3u8Config.readTimeout
        conn.connectTimeout = M3u8Config.connTimeout
        conn.requestMethod = "GET"
        conn.connect()
        val length = conn.getHeaderField("Content-Length")
        try {
            M3u8Config.log("frist ts length ${length.toLong()}")
            return length.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }

    /**
     * 获取ts的时长
     */
    fun readTime(str: String): Float {
        try {
            val split = str.split(",")
            return split[0].replace("#EXTINF:", "").toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0f
    }

    data class Entry(var bytes: ByteArray = kotlin.ByteArray(0), var url: String = "")
}