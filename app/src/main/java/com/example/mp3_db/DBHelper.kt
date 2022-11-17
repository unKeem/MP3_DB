package com.example.mp3_db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, dbName: String, version: Int) :
    SQLiteOpenHelper(context, dbName, null, version) {
    // DBHelper 처음 객체가 만들어질때 딱 한번만 실행된다.
    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
        create table musicTBL (
        id text primary key,
        title text,
        artist text,
        albumID text,
        duration integer,
        likes integer) 
        """.trimIndent()
        db?.execSQL(query)
    }

    //버전이 변경될때 콜백 함수
    override fun onUpgrade(db: SQLiteDatabase?, newVersion: Int, oldVersion: Int) {
        val query = """
            drop table musicTBL
        """.trimIndent()
        db?.execSQL(query)
        onCreate(db)
    }

    fun selectMusicAll(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            SELECT * FROM musicTBL
        """.trimIndent()
        //쿼리문을 실행하기위한 함수
        var db = this.readableDatabase
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {//커서를 다음행으로 이동시킨다
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID, duration, likes)
                    musicList?.add(music)
                }
            } else {
                musicList = null
            }
        } catch (e: Exception) {
            Log.d("mp3_db", "selectMusicAll 에러")
        } finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    fun insertMusic(music: Music): Boolean {
        var flag = false
        val query = """
            INSERT INTO musicTBL (id , title, artist, albumID, duration, likes) VALUES('${music.id}', '${music.title}', '${music.artist}', '${music.albumID}', '${music.duration}', '${music.likes})
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
        } catch (e: Exception) {
            Log.d("mp3_db", "insert 에러")
            flag = false
        } finally {
            db.close()
        }
        return flag
    }

    fun updateLike(music: Music): Boolean {
        var flag = false
        val query = """
            UPDATE musicTBL SET likes = ${music.likes} WHERE id = ${music.id}
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("mp3_db", "db updateLike 성공")
        } catch (e: Exception) {
            Log.d("mp3_db", "db updateLike 에러")
            flag = false
        } finally {
            db.close()
        }
        return flag
    }

    fun searchMusic(queries: String?): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            SELECT * FROM musicTBL WHERE title LIKE '${queries}%' OR artist LIKE '${queries}%'
        """.trimIndent()
        //쿼리문을 실행하기위한 함수
        var db = this.readableDatabase
        try {
            cursor = db.rawQuery(query, null)
            if (cursor?.count!! > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID, duration, likes)
                    musicList?.add(music)
                }
            } else {
                musicList = null
            }

        } catch (e: Exception) {
            Log.d("mp3_db", "searchtMusicAll 에러")
        } finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    fun selectMusicLike(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            SELECT * FROM musicTBL WHERE likes = 1
        """.trimIndent()
        //쿼리문을 실행하기위한 함수
        var db = this.readableDatabase
        try {
            cursor = db.rawQuery(query, null)
            if (cursor?.count!! > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID, duration, likes)
                    musicList?.add(music)
                }
            } else {
                musicList = null
            }
        } catch (e: Exception) {
            Log.d("mp3_db", "selectMusicLike 에러")
        } finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }
}
