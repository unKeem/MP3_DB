package com.example.mp3_db

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parceler

@Parcelize
class Music(
    var id: String,
    var title: String?,
    var artist: String?,
    var albumID: String?,
    var duration: Int?,
    var likes: Int?
) : Parcelable {
    //serializable 안쓰고 parcelable사용 하는 이유는 속도를 높이기 위해
    companion object : Parceler<Music> {
        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumID)
            parcel.writeInt(duration!!)
            parcel.writeInt(likes!!)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    //앨범 Uri 가져온다
    private fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumID")
    }

    //음악 Uri 가져온다
    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //음악 비트맵을 가져와서 원하는 사이즈로 비트맵 만들기
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = getAlbumUri() //앨범경로
        val options = BitmapFactory.Options()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var bitmap: Bitmap?
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "read")
            bitmap = BitmapFactory.decodeFileDescriptor(
                parcelFileDescriptor?.fileDescriptor,
                null,
                options
            )
            //비트맵을 가져왔는데 우리가 원하는 사이즈가 아닐경우를 위해서 처리
            if (bitmap != null) {
                val tempBitmap =
                    Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                bitmap.recycle()
                bitmap = tempBitmap
            }
            return bitmap
        } catch (e: java.lang.Exception) {
            Log.d("mp3_db", "getAlbumImage 함수에 에러 발생 _1")
        } finally {
            try {
                parcelFileDescriptor?.close()
            } catch (e: java.lang.Exception) {
                Log.d("mp3_db", "getAlbumImage 함수에 에러 발생 _2")
            }
        }
        return null
    }
}