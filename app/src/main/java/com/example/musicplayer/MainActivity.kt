package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.musicplayer.adapter.MediaAdapter
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.model.MediaFile
import com.google.android.material.snackbar.Snackbar
import java.util.Collections

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var mediaList: List<MediaFile>
    lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPer()

        binding.refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            checkPer()
        })

        binding.moreBtn.setOnClickListener(View.OnClickListener { view ->
            PopupMenu(this, binding.moreBtn).apply {
                inflate(R.menu.menu)
                setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
                    if (MService.mp.isPlaying){
                        stopService(Intent(this@MainActivity, MService::class.java))
                        dismiss()
                    }
                    true
                })
                show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 404){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showMedia()
            } else {
                Toast.makeText(this, "Please grant the permission to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPer(){
        if (Build.VERSION.SDK_INT >= 33){
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED){
                showMedia()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 404)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    showMedia()
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 404)
                }
            }
        }
    }

    fun showMedia(){
        mediaList = getMedias()
        Collections.sort(mediaList, Comparator { t, t2 ->
            t.displayName!!.compareTo(t2.displayName!!)
        })
        binding.recycler.layoutManager = LinearLayoutManager(this)
        adapter = MediaAdapter(this, mediaList as ArrayList<MediaFile>)
        binding.recycler.adapter = adapter

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newtext: String?): Boolean {
                if (!newtext.equals("")) {
                    val filteredList: ArrayList<MediaFile> = arrayListOf()
                    for (media in mediaList) {
                        if (media.displayName!!.toLowerCase().contains(newtext!!.toLowerCase())
                            || media.artist!!.toLowerCase().contains(newtext.toLowerCase())) {
                            filteredList.add(media)
                        }
                    }
                    adapter.filteredList(filteredList)
                } else {
                    adapter.filteredList(mediaList as ArrayList<MediaFile>)
                }
                return true
            }
        })
    }

    @SuppressLint("Range")
    fun getMedias(): List<MediaFile> {
        val arrayList: ArrayList<MediaFile> = arrayListOf()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(uri, null, null, null, null)

        if (cursor != null && cursor.moveToNext()){
            do {
                val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                val size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                arrayList.add(MediaFile(id, displayName, size, duration, artist, path))
            } while (cursor.moveToNext())
        }

        return arrayList
    }
}