package com.example.mp3_db

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mp3_db.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat

class PlaymusicActivity : AppCompatActivity() {
    companion object {
        val ALBUM_SIZE = 80
    }

    private lateinit var binding: ActivityPlaymusicBinding
    private var playList: MutableList<Parcelable>? = null
    private var position: Int = 0
    private var music: Music? = null
    private var mediaPlayer: MediaPlayer? = null
    private var messengerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //인텐트로 가져옴
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position", 0)
        music = playList?.get(position) as Music
        //화면에 바인딩
        binding.albumTitle.text = music?.title
        binding.albumArtist.text = music?.artist
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.playDuration.text = "00:00"
        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) {
            binding.albumImage.setImageBitmap(bitmap)
        } else {
            binding.albumImage.setImageResource(R.drawable.ic_music)
        }
        //음악등록
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
        //시크바 재생위치 결정
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        //이벤트 설정 목록버튼 눌렀을때
        binding.listButton.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer?.release()
            mediaPlayer = null
            finish()
        }

    }
}


