package com.example.mp3_db

import android.content.Context
import android.content.Intent
import android.os.Parcelable
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
        when (music?.likes) {
            0 -> binding.ivItemLike.setImageResource(R.drawable.ic_star_outline)
            1 -> binding.ivItemLike.setImageResource(R.drawable.ic_star)
        }
        //이벤트처리
        binding.root.setOnClickListener{
            val playList: ArrayList<Parcelable> = musicList as ArrayList<Parcelable>
            val intent = Intent(binding.root.context, PlaymusicActivity::class.java)
            intent.putExtra("playList", playList)
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return musicList?.size ?: 0
    }

    class CustomViewHoldr(val binding: ItemRecyclerBinding) : RecyclerView.ViewHolder(binding.root)
}