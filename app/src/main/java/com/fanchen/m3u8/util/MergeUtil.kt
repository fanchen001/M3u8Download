package com.fanchen.m3u8.util

import android.text.TextUtils
import com.fanchen.m3u8.bean.M3u8
import java.io.File
import java.io.FileOutputStream

/**
 * merge
 * Created by fanchen on 2018/9/10.
 */
object MergeUtil {

    /**
     * 将M3U8对象的所有ts切片合并为1个
     *
     * @param m3u8
     * @param tofile
     * @throws IOException
     */
    fun merge(m3u8: M3u8, tofile: String) {
        val file = File(tofile)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            m3u8.tsList.forEach {
                fos?.write(File(m3u8.getTsDirPath(), it.getName()).readBytes())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath
     * @param boolean 是否删除根目录
     * 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    fun deleteDirectory(sPath: String, boolean: Boolean = true): Boolean {
        if (TextUtils.isEmpty(sPath)) return false
        val dirFile = File(if (!sPath.endsWith(File.separator)) sPath + File.separator else sPath)
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        // 删除文件夹下的所有文件(包括子目录)
        val files = dirFile.listFiles()
        var flag = false
        for (newFile in files) {
            // 删除子文件
            if (newFile.isFile) {
                flag = deleteFile(newFile.absolutePath)
                if (!flag) break
            } else { // 删除子目录
                flag = deleteDirectory(newFile.absolutePath)
                if (!flag) break
            }
        }
        return if (!flag) false else if (boolean) dirFile.delete() else true
        // 删除当前目录
    }

    /**
     * 删除单个文件
     *
     * @param sPath
     * 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    fun deleteFile(sPath: String): Boolean {
        var flag = false
        val file = File(sPath)
        // 路径为文件且不为空则进行删除
        if (file.isFile && file.exists()) {
            file.delete()
            flag = true
        }
        return flag
    }
}