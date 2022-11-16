package com.example.mp3_db

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3_db.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        val REQ_READ = 99
    }

    lateinit var binding: ActivityMainBinding

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
        val musicList: MutableList<Music>? = mutableListOf<Music>()
        //1.음원정보주소(공유메모리에서 음원 정보)
        val musicURL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //2.음원에서 가져올 정보의 매열
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        //3. 콘텐트리졸버 쿼리를 작성해 musiclist로 가져온다
        val cursor = contentResolver.query(musicURL, projection, null, null, null)
        while (cursor!!.moveToNext()) {
            val id = cursor.getString(0)
            val title = cursor.getString(1).replace("'","")
            val artist = cursor.getString(2).replace("'","")
            val albumId = cursor.getString(3)
            val duration = cursor.getInt(4)

            val music = Music(id, title, artist, albumId, duration, 0)
            musicList?.add(music)
        }

        //4.리싸이클러뷰에 제공할 어댑터
        val adapter = MusicRecyclerAdapter(this, musicList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)


    }

    private fun isPermitted(): Boolean{
        return ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED
    }

    //외부저장소를 읽을 권한이 있는지 체크
//    private fun isPermitted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            permissions[0]
//        ) != PackageManager.PERMISSION_GRANTED
//    }
}