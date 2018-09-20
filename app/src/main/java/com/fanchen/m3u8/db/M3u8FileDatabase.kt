package com.fanchen.m3u8.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fanchen.m3u8.bean.M3u8File
import java.util.concurrent.locks.ReentrantLock
import android.content.ContentValues
import java.util.*


/**
 * M3u8FileDatabase
 * Created by fanchen on 2018/9/12.
 */
class M3u8FileDatabase(context: Context, version: Int = 1) : SQLiteOpenHelper(context, "m3u8_download.db", null, version) {

    private val SQL = "create table tab_m3u8_file (id integer primary key autoincrement,m3u8Path text, url text,m3u8VideoName text,state integer,startTime long,endTime long,onlyId text)"
    private val lock = ReentrantLock()
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insert(file: M3u8File): Long {
        if (isExists(file)) return -1L
        return use {
            val cv = ContentValues() //生成ContentValues对象
            cv.put("url", file.url) //往ContentValues对象存放数据，键-值对模式
            cv.put("m3u8Path", file.m3u8Path)
            cv.put("m3u8VideoName", file.m3u8VideoName)
            cv.put("state", file.state)
            cv.put("startTime", file.startTime)
            cv.put("endTime", file.endTime)
            cv.put("onlyId", file.onlyId)
            it.insert("tab_m3u8_file", null, cv)  //调用insert方法，将数据插入数据库
        } ?: -1L
    }

    fun insert(url: String, videoName: String): Long {
        return insert(M3u8File(url, videoName))
    }

    fun delete(file: M3u8File): Int {
        return delete(file.url)
    }

    fun delete(url: String): Int {
        return use {
            val selectionArgs = arrayOf(url)
            it.delete("tab_m3u8_file", "url = ?", selectionArgs)
        } ?: -1
    }

    fun delete(id: Int): Int {
        return use {
            val selectionArgs = arrayOf(id.toString())
            it.delete("tab_m3u8_file", "id = ?", selectionArgs)
        } ?: -1
    }

    fun delete() {
        use {
            it.delete("tab_m3u8_file", null, null)
        }
    }

    fun update(url: String, file: M3u8File): Int {
        return use {
            val selectionArgs = arrayOf(url)
            val cv = ContentValues() //生成ContentValues对象
            cv.put("url", file.url) //往ContentValues对象存放数据，键-值对模式
            cv.put("m3u8Path", file.m3u8Path)
            cv.put("m3u8VideoName", file.m3u8VideoName)
            cv.put("state", file.state)
            cv.put("startTime", file.startTime)
            cv.put("endTime", file.endTime)
            cv.put("onlyId", file.onlyId)
            it.update("tab_m3u8_file", cv, "url = ?", selectionArgs)
        } ?: -1
    }

    fun update(id: Int, file: M3u8File): Int {
        return use {
            val selectionArgs = arrayOf(id.toString())
            val cv = ContentValues() //生成ContentValues对象
            cv.put("url", file.url) //往ContentValues对象存放数据，键-值对模式
            cv.put("m3u8Path", file.m3u8Path)
            cv.put("m3u8VideoName", file.m3u8VideoName)
            cv.put("state", file.state)
            cv.put("startTime", file.startTime)
            cv.put("endTime", file.endTime)
            cv.put("onlyId", file.onlyId)
            it.update("tab_m3u8_file", cv, "id = ?", selectionArgs)
        } ?: -1
    }

    fun update(file: M3u8File) {
        use {
            val selectionArgs = arrayOf(file.id.toString())
            val cv = ContentValues() //生成ContentValues对象
            cv.put("url", file.url) //往ContentValues对象存放数据，键-值对模式
            cv.put("m3u8Path", file.m3u8Path)
            cv.put("m3u8VideoName", file.m3u8VideoName)
            cv.put("state", file.state)
            cv.put("startTime", file.startTime)
            cv.put("endTime", file.endTime)
            cv.put("onlyId", file.onlyId)
            it.update("tab_m3u8_file", cv, "id = ?", selectionArgs)
        }
    }

    fun query(url: String): M3u8File? {
        return use {
            val selectionArgs = arrayOf(url)
            val cs = it.query("tab_m3u8_file", null, "url = ?", selectionArgs, null, null, null, null)
            var m3u8File: M3u8File? = null
            if (cs.moveToNext()) {
                m3u8File = M3u8File()
                m3u8File.m3u8Path = cs.getString(cs.getColumnIndex("m3u8Path"))
                m3u8File.url = cs.getString(cs.getColumnIndex("url"))
                m3u8File.m3u8VideoName = cs.getString(cs.getColumnIndex("m3u8VideoName"))
                m3u8File.endTime = cs.getLong(cs.getColumnIndex("endTime"))
                m3u8File.startTime = cs.getLong(cs.getColumnIndex("startTime"))
                m3u8File.state = cs.getInt(cs.getColumnIndex("state"))
                m3u8File.id = cs.getInt(cs.getColumnIndex("id"))
                m3u8File.onlyId = cs.getString(cs.getColumnIndex("onlyId"))
            }
            cs.close()
            m3u8File
        }
    }

    fun queryAll(state:Int): LinkedList<M3u8File>? {
        return use {
            val selectionArgs = arrayOf(state.toString())
            val cs = it.query("tab_m3u8_file", null, "state = ?", selectionArgs, null, null, null, null)
            val list = LinkedList<M3u8File>()
            while (cs.moveToNext()) {
                val m3u8File = M3u8File()
                m3u8File.m3u8Path = cs.getString(cs.getColumnIndex("m3u8Path"))
                m3u8File.url = cs.getString(cs.getColumnIndex("url"))
                m3u8File.m3u8VideoName = cs.getString(cs.getColumnIndex("m3u8VideoName"))
                m3u8File.endTime = cs.getLong(cs.getColumnIndex("endTime"))
                m3u8File.startTime = cs.getLong(cs.getColumnIndex("startTime"))
                m3u8File.state = cs.getInt(cs.getColumnIndex("state"))
                m3u8File.id = cs.getInt(cs.getColumnIndex("id"))
                m3u8File.onlyId = cs.getString(cs.getColumnIndex("onlyId"))
                list.add(m3u8File)
            }
            cs.close()
            list
        }
    }

    fun queryAll(): LinkedList<M3u8File>? {
        return use {
            val cs = it.query("tab_m3u8_file", null, null, null, null, null, null, null)
            val list = LinkedList<M3u8File>()
            while (cs.moveToNext()) {
                val m3u8File = M3u8File()
                m3u8File.m3u8Path = cs.getString(cs.getColumnIndex("m3u8Path"))
                m3u8File.url = cs.getString(cs.getColumnIndex("url"))
                m3u8File.m3u8VideoName = cs.getString(cs.getColumnIndex("m3u8VideoName"))
                m3u8File.endTime = cs.getLong(cs.getColumnIndex("endTime"))
                m3u8File.startTime = cs.getLong(cs.getColumnIndex("startTime"))
                m3u8File.state = cs.getInt(cs.getColumnIndex("state"))
                m3u8File.id = cs.getInt(cs.getColumnIndex("id"))
                m3u8File.onlyId = cs.getString(cs.getColumnIndex("onlyId"))
                list.add(m3u8File)
            }
            cs.close()
            list
        }
    }

    fun isExists(file: M3u8File): Boolean {
        return query(file.url) != null
    }

    fun isExists(url: String): Boolean {
        return query(url) != null
    }

    fun <T : SQLiteOpenHelper, R> T.use(block: (SQLiteDatabase) -> R): R? {
        lock.lock()
        var db: SQLiteDatabase? = null
        try {
            return block(this.writableDatabase.apply { db = this })
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db?.close()
            lock.unlock()
        }
        return null
    }
}