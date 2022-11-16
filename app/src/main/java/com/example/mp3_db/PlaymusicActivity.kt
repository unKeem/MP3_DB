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
        // 목록 버튼
        binding.listButton.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
//            mediaPlayer?.release()
//            mediaPlayer = null
            finish() // PlayActivity 종료
        }
        // 정지 버튼
        binding.stopButton.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.progress = 0
            binding.playDuration.text = "00:00"
            binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        //이벤트설정 재생버튼
        binding.playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            } else {
                mediaPlayer?.start()
                binding.playButton.setImageResource(R.drawable.ic_pause)

                val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                messengerJob = backgroundScope.launch {
                    while (mediaPlayer?.isPlaying == true) {

                        runOnUiThread {
                            var currentPosition = mediaPlayer?.currentPosition!!
                            binding.seekBar.progress = currentPosition
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
                        if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                            binding.seekBar.progress = 0
                            binding.playDuration.text = "00:00"
                        }
                        binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }//end of messengerJob
            }
        }//end of playButton
    }
}

