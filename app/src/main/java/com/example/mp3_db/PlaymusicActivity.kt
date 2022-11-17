package com.example.mp3_db

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mp3_db.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
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
        binding.tvMusicTitle.text = music?.title
        binding.tvMusician.text = music?.artist
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.playDuration.text = "00:00"
        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) {
            binding.ivAlbumCover.setImageBitmap(bitmap)
        } else {
            binding.ivAlbumCover.setImageResource(R.drawable.ic_music)
        }
        //음악등록
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
        //시크바 재생위치 결정
        binding.seekBarNew.max = mediaPlayer!!.duration
        binding.seekBarNew.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        // 목록 버튼
        binding.btnPlaylist.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
//            mediaPlayer?.release()
//            mediaPlayer = null
            finish() // PlayActivity 종료
        }
        // 정지 버튼
        binding.btnStop.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBarNew.progress = 0
            binding.playDuration.text = "00:00"
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
        //이벤트설정 재생버튼
        binding.btnPlayPause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer?.start()
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

                val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                messengerJob = backgroundScope.launch {
                    while (mediaPlayer?.isPlaying == true) {

                        runOnUiThread {
                            var currentPosition = mediaPlayer?.currentPosition!!
                            binding.seekBarNew.progress = currentPosition
                            val currentDurateion =
                                SimpleDateFormat("mm:ss").format(mediaPlayer!!.currentPosition)
                            binding.playDuration.text = currentDurateion
                        }
                        try {
                            // 1초마다 수행되도록 딜레이
                            delay(1000)
                        } catch (e: Exception) {
                            Log.d("로그", "스레드 오류 발생")
                        }
                    }//end of while
                    //노래가 전부 끝나면 처음으로 돌아감(1000을 빼주는 부분은 1초 늦게 반응하는것 사실상 오류이지만)
                    runOnUiThread {
                        if (mediaPlayer!!.currentPosition >= (binding.seekBarNew.max - 1000)) {
                            binding.seekBarNew.progress = 0
                            binding.playDuration.text = "00:00"
                        }
                        binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                    }
                }//end of messengerJob
            }
        }//end of playButton
        binding.btnRewind.setOnClickListener {

        }// rewind
    }
}

