package com.example.musicplayer

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.format.Formatter
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import com.example.musicplayer.databinding.ActivityMusicBinding
import com.example.musicplayer.model.MediaFile
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MusicActivity : AppCompatActivity() {

    lateinit var binding: ActivityMusicBinding
    lateinit var mediaList: ArrayList<MediaFile>
    var pos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener{ finish() }

        mediaList = intent.getParcelableArrayListExtra<MediaFile>("data") as ArrayList<MediaFile>
        pos = intent.getIntExtra("pos", 0)

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(mediaList.get(pos).path)
        val data = mmr.embeddedPicture
        var bitmap: Bitmap? = null
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        }

        if (bitmap != null) {
            binding.musicImg.setImageBitmap(bitmap)
        } else {
            binding.musicImg.setImageResource(R.drawable.placeholder)
        }
        binding.musicTitle.text = mediaList.get(pos).displayName
        binding.musicArtist.text = mediaList.get(pos).artist
        val size = mediaList.get(pos).size
        binding.musicSize.text = Formatter.formatFileSize(this, size!!.toLong())
        binding.musicSeekbar.max = mediaList.get(pos).duration!!.toInt()
        binding.musicDuration.text = timeConvert(mediaList.get(pos).duration!!.toLong())

        if (!isServiceRunning(MService::class.java)) {
            MService.setpath(mediaList.get(pos).path!!)
            ContextCompat.startForegroundService(this, Intent(this, MService::class.java))
        } else {
            if (!MService.path.equals(mediaList.get(pos).path)) {
                MService.service.onDestroy()
                MService.setpath(mediaList.get(pos).path!!)
                startService(Intent(this, MService::class.java))
            } else {
                binding.musicSeekbar.progress = MService.mp.currentPosition
            }
        }

        runOnUiThread(object : Runnable {
            override fun run() {
                if (MService.mp != null) {
                    binding.musicSeekbar.progress = MService.mp.currentPosition
                    binding.musicTime.text = timeConvert(MService.mp.currentPosition.toLong())

                    if (binding.musicSeekbar.progress == mediaList.get(pos).duration!!.toInt()) {
                        nextSong()
                    }

                    if (MService.mp.isPlaying) {
                        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                    } else {
                        binding.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }

                Handler().postDelayed(this, 100)
            }
        })

        binding.playPauseBtn.setOnClickListener(View.OnClickListener {
            if (!MService.mp.isPlaying) {
                MService.mp.start()
            } else {
                MService.mp.pause()
            }
        })

        binding.musicSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, byUser: Boolean) {
                if (MService.mp != null && byUser) {
                    MService.mp.seekTo(pos)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.nextBtn.setOnClickListener{ nextSong() }

        binding.previousBtn.setOnClickListener{ previousSong() }

        binding.previous10Btn.setOnClickListener{ previous10sec() }

        binding.next10Btn.setOnClickListener{ next10sec() }
    }

    fun previous10sec(){
        if (MService.mp != null){
            if (MService.mp.currentPosition > 10000){
                MService.mp.seekTo(MService.mp.currentPosition - 10000)
            } else {
                MService.mp.seekTo(0)
            }
        }
    }

    fun next10sec(){
        if (MService.mp != null){
            if (MService.mp.currentPosition < MService.mp.duration - 10000){
                MService.mp.seekTo(MService.mp.currentPosition + 10000)
            } else {
                MService.mp.seekTo(MService.mp.duration)
            }
        }
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

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                return true
            }
        }

        return false
    }

    fun nextSong() {
        if (pos != mediaList.size - 1) {
            pos++
        } else {
            pos = 0
        }

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(mediaList.get(pos).path)
        val data = mmr.embeddedPicture
        var bitmap: Bitmap? = null
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        }

        if (bitmap != null) {
            binding.musicImg.setImageBitmap(bitmap)
        } else {
            binding.musicImg.setImageResource(R.drawable.placeholder)
        }
        binding.musicTitle.text = mediaList.get(pos).displayName
        binding.musicArtist.text = mediaList.get(pos).artist
        val size = mediaList.get(pos).size
        binding.musicSize.text = Formatter.formatFileSize(this@MusicActivity, size!!.toLong())
        binding.musicSeekbar.max = mediaList.get(pos).duration!!.toInt()
        binding.musicDuration.text = timeConvert(mediaList.get(pos).duration!!.toLong())


        MService.service.onDestroy()
        MService.setpath(mediaList.get(pos).path!!)
        startService(Intent(this@MusicActivity, MService::class.java))
    }

    fun previousSong() {
        if (pos != 0) {
            pos--
        } else {
            pos = mediaList.size - 1
        }

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(mediaList.get(pos).path)
        val data = mmr.embeddedPicture
        var bitmap: Bitmap? = null
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        }

        if (bitmap != null) {
            binding.musicImg.setImageBitmap(bitmap)
        } else {
            binding.musicImg.setImageResource(R.drawable.placeholder)
        }
        binding.musicTitle.text = mediaList.get(pos).displayName
        binding.musicArtist.text = mediaList.get(pos).artist
        val size = mediaList.get(pos).size
        binding.musicSize.text = Formatter.formatFileSize(this@MusicActivity, size!!.toLong())
        binding.musicSeekbar.max = mediaList.get(pos).duration!!.toInt()
        binding.musicDuration.text = timeConvert(mediaList.get(pos).duration!!.toLong())


        MService.service.onDestroy()
        MService.setpath(mediaList.get(pos).path!!)
        startService(Intent(this@MusicActivity, MService::class.java))
    }
}