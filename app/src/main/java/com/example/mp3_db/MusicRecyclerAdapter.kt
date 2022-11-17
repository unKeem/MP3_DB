package com.example.mp3_db

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3_db.databinding.ItemRecyclerBinding
import java.text.SimpleDateFormat

class MusicRecyclerAdapter(val context: Context, val musicList: MutableList<Music>?) :
    RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHoldr>() {
    //정적멤버상수
    companion object {
        var ALBUM_SIZE = 80
        const val LIKES_EMPTY = 0
        const val LIKES_FULL = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHoldr {
        val binding =
            ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHoldr(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHoldr, position: Int) {
        val binding = (holder as CustomViewHoldr).binding
        val music = musicList?.get(position)

        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        val bitmap = music?.getAlbumImage(context, ALBUM_SIZE)
        if (bitmap != null) {
            binding.ivAlbumArt.setImageBitmap(bitmap)
        } else {
            binding.ivAlbumArt.setImageResource(R.drawable.ic_music)
        }

        //이벤트처리
        binding.root.setOnClickListener {
            val playList: ArrayList<Parcelable> = musicList as ArrayList<Parcelable>
            val intent = Intent(binding.root.context, PlaymusicActivity::class.java)
            intent.putExtra("playList", playList)
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }

        // DB 저장 값대로 좋아요 표시
        when (music?.likes) {
            LIKES_EMPTY -> binding.ivItemLike.setImageResource(R.drawable.ic_star_outline)
            LIKES_FULL -> binding.ivItemLike.setImageResource(R.drawable.ic_star)
        }

        //이벤트처리 좋아요 눌렀을때 데이터 베이스 좋아요 등록
        binding.ivItemLike.setOnClickListener {
            when (music?.likes) {
                LIKES_EMPTY -> {
                    binding.ivItemLike.setImageResource(R.drawable.ic_star)
                    music.likes = LIKES_FULL
                }
                LIKES_FULL -> {
                    binding.ivItemLike.setImageResource(R.drawable.ic_star_outline)
                    music.likes = LIKES_EMPTY
                }
            }

            if (music != null) {
                val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
                val flag = dbHelper.updateLike(music)
                if (flag == false) {
                    Log.d("mp3_db", "어댑터에 onBindViewHolder 에러 ${music.toString()}")
                } else {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList?.size ?: 0
    }

    class CustomViewHoldr(val binding: ItemRecyclerBinding) : RecyclerView.ViewHolder(binding.root)
}