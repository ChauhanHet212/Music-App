package com.example.musicplayer.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MusicActivity
import com.example.musicplayer.R
import com.example.musicplayer.databinding.MediaItemBinding
import com.example.musicplayer.model.MediaFile
import java.time.Duration
import java.util.concurrent.TimeUnit

class MediaAdapter(val context: Context, var musicList: ArrayList<MediaFile>): RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.media_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(musicList.get(position).path)
        val data = mmr.embeddedPicture
        var bitmap: Bitmap? = null
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        }

        if (bitmap != null) {
            holder.binding.mediaImg.setImageBitmap(bitmap)
        } else {
            holder.binding.mediaImg.setImageResource(R.drawable.placeholder)
        }
        holder.binding.mediaTitle.text = musicList.get(position).displayName
        holder.binding.mediaArtist.text = musicList.get(position).artist
        val size = musicList.get(position).size
        holder.binding.mediaSize.text = Formatter.formatFileSize(context, size!!.toLong())
        val time = musicList.get(position).duration!!.toDouble()
        holder.binding.mediaDuration.text = timeConvert(time.toLong())

        holder.itemView.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(context, MusicActivity::class.java)
            intent.putExtra("data", musicList)
            intent.putExtra("pos", holder.absoluteAdapterPosition)
            context.startActivity(intent)
        })
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun timeConvert(value: Long): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val duration = Duration.ofMillis(value)
            val h = duration.toHours()
            val m = duration.toMinutesPart()
            val s = duration.toSecondsPart()

            return if (h > 0) {
                String.format("%02d:%02d:%02d", h, m, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        } else {
            val h = TimeUnit.MILLISECONDS.toHours(value)
            val m = TimeUnit.MILLISECONDS.toMinutes(value) % 60
            val s = TimeUnit.MILLISECONDS.toSeconds(value) % 60

            return if (h > 0){
                String.format("%02d:%02d:%02d", h, m, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        }
    }

    fun filteredList(filteredList: ArrayList<MediaFile>) {
        musicList = filteredList
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: MediaItemBinding = MediaItemBinding.bind(itemView)
    }
}