package com.mahdiasd.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mahdiasd.filepicker.FileModel
import com.mahdiasd.filepicker.FilePicker
import com.mahdiasd.filepicker.FilePickerListener
import com.mahdiasd.filepicker.PickerMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test(null)
    }

    fun test(view: View?) {
        FilePicker(this, supportFragmentManager)
            .setMode(PickerMode.FILE, PickerMode.Audio, PickerMode.Image, PickerMode.Video)
            .setDefaultMode(PickerMode.Image)
            .setMaxSelection(10)
            .setListener(object : FilePickerListener {
                override fun selectedFiles(list: List<FileModel>?) {
                    Log.e("TAG", "selectedFiles: ${list.toString()}")
                }
            })
            .show()
    }
}