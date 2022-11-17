package com.example.mp3_db

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3_db.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        val REQ_READ = 99
        val DB_NAME = "musicDB"
        var VERSION = 1
        const val LIKES_EMPTY = 0
    }

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MusicRecyclerAdapter
    private var musicList: MutableList<Music>? = mutableListOf<Music>()

    //승인받을 퍼미션 항목 요청
    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //승인되었는지 점검
        if (isPermitted()) {
            startProcess()
        } else {
            //외부저장소 읽기 권한이 없다면, 유저에게 읽기 권한 신청
            ActivityCompat.requestPermissions(this, permissions, REQ_READ)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_READ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startProcess()
        } else {
            Toast.makeText(this, "권한 요청을 승인하셔야 하이뮤직이 실행됩니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startProcess() {
        //먼저 데이타베이스에서 음원정보를 가져온다. 없으면 공유메모리에서 음원 정보 가져온다.
        val dbHelper = DBHelper(this, DB_NAME, VERSION)
        musicList = dbHelper.selectMusicAll()
        //만약데이터베이스에 없으면 컨텐트리졸버를 통해서 공유메모리에서 음원정보를 가져온다.
        if (musicList == null) {
            val playMusicList = getMusicList()
            if (playMusicList != null) {
                for (i in 0..playMusicList.size - 1) {
                    val music = playMusicList.get(i)
                    dbHelper.insertMusic(music)
                }
                musicList = playMusicList
            } else {
                Log.d("mp3_db", "MainActivity/startProcess(): 외장메모리 음원파일없음")
            }
        }

        //4.리싸이클러뷰에 제공할 어댑터
        adapter = MusicRecyclerAdapter(this, musicList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getMusicList(): MutableList<Music>? {
        var tempMusicList: MutableList<Music>? = mutableListOf<Music>()
        //1.음원정보주소(공유메모리에서 음원 정보)
        val musicURL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //2.음원에서 가져올 정보의 배열
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        //3. 콘텐트리졸버 쿼리를 작성해 musiclist로 가져온다
        val cursor = contentResolver.query(musicURL, projection, null, null, null)
        if (cursor?.count!! > 0) {
            while (cursor!!.moveToNext()) {
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'", "")
                val artist = cursor.getString(2).replace("'", "")
                val albumId = cursor.getString(3)
                val duration = cursor.getInt(4)
                val music = Music(id, title, artist, albumId, duration, LIKES_EMPTY)
                tempMusicList?.add(music)
            }
        } else {
            tempMusicList = null
        }
        cursor.close()
        return tempMusicList
    }

    //사용하는앱이 외부저장소를 읽을 권한이 있는지 체크
    private fun isPermitted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        //메뉴서치아이템
        val searchMenu = menu?.findItem(R.id.menu_search)
        val searchView = searchMenu?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val dbHelper = DBHelper(applicationContext, DB_NAME, VERSION)
                if (query.isNullOrBlank()) {
                    musicList?.clear()
                    dbHelper.selectMusicAll()?.let { musicList?.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    musicList?.clear()
                    dbHelper.searchMusic(query)?.let { musicList?.addAll(it) }
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dbHelper = DBHelper(applicationContext, DB_NAME, VERSION)
        when (item.itemId) {
            R.id.menu_likes -> {
                musicList?.clear()
                dbHelper.selectMusicLike()?.let { musicList?.addAll(it) }
                adapter.notifyDataSetChanged()
            }
            R.id.menu_main -> {
                musicList?.clear()
                dbHelper.selectMusicAll()?.let { musicList?.addAll(it) }
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}