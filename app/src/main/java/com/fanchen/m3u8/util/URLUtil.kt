package com.fanchen.m3u8.util

/**
 * URLUtil
 * Created by fanchen on 2018/9/12.
 */
object URLUtil {

    /**
     * Ts 真實下載地址
     */
    fun getUrl(url: String, path: String): String {
        return if (path.startsWith("http") || path.startsWith("ftp")) {
            path
        } else if (path.startsWith("//")) {
            getBaseUrl(url).split("/")[0] + path
        } else if (path.startsWith("/")) {
            getBaseUrl(url) + path.substring(1)
        } else {
            getPreUrl(url) + path
        }
    }

    fun getBaseUrl(url: String): String {
        val split = url.split("/")
        return if (split.size >= 3) split[0] + "//" + split[2] + "/" else ""
    }

    fun getPreUrl(url: String): String {
        val split = url.split("/")
        return url.replace(split[split.size - 1], "")
    }
}