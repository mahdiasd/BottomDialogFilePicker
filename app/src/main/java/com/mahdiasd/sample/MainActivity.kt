package com.mahdiasd.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mahdiasd.filepicker.FilePicker
import com.mahdiasd.filepicker.PickerMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test(null)
    }

    fun test(view: View?) {
        FilePicker(
            this, supportFragmentManager,
            listOf(PickerMode.Image, PickerMode.Video, PickerMode.Audio, PickerMode.Document),
        )
            .show()

    }
}