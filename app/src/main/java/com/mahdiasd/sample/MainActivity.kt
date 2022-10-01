package com.mahdiasd.sample

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mahdiasd.filepicker.FileModel
import com.mahdiasd.filepicker.FilePicker
import com.mahdiasd.filepicker.FilePickerListener
import com.mahdiasd.filepicker.PickerMode
import com.mahdiasd.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding(R.layout.activity_main)
        test(null)
    }

    private fun initBinding(layout: Int) {
        binding = DataBindingUtil.setContentView(this, layout)
        binding.let {
            it.lifecycleOwner = this
        }
    }

    fun btn(view: View) {
        val modes =
            mutableListOf(PickerMode.FILE, PickerMode.Audio, PickerMode.Image, PickerMode.Video)

        if (!binding.Image.isChecked)
            modes.remove(PickerMode.Image)
        if (!binding.video.isChecked)
            modes.remove(PickerMode.Video)
        if (!binding.storage.isChecked)
            modes.remove(PickerMode.FILE)
        if (!binding.audio.isChecked)
            modes.remove(PickerMode.Audio)

        if (modes.isEmpty()) {
            Toast.makeText(this, "choose mode", Toast.LENGTH_SHORT).show()
            return
        }

        val max =
            if (binding.maxSelection.text.isEmpty()) 1 else binding.maxSelection.text.toString()
                .toInt()

        val activeColor =
            if (binding.color.text.isEmpty()) "#2196F3" else binding.color.text.toString()


        val deActiveColor =
            if (binding.deActiveColor.text.isEmpty()) "#A4A4A4" else binding.deActiveColor.text.toString()


        val cardBackgroundColor =
            if (binding.cardViewColor.text.isEmpty()) "#ffffff" else binding.cardViewColor.text.toString()


        FilePicker(this, supportFragmentManager)
            .setMode(*modes.toTypedArray())
            .setDefaultMode(PickerMode.Image)
            .setMaxSelection(max).setCardBackgroundColor(Color.parseColor(cardBackgroundColor))
            .setCustomText(
                binding.videoTitle.text.toString(),
                binding.audioTitle.text.toString(),
                binding.storage.text.toString(),
                binding.imageTitle.text.toString(),
                binding.openStorageTitle.text.toString()
            )
            .setShowFileWhenClick(binding.showWhenClick.isChecked)
            .setDeActiveColor(Color.parseColor(deActiveColor))
            .setActiveColor(Color.parseColor(activeColor))
            .setListener(object : FilePickerListener {
                override fun selectedFiles(list: List<FileModel>?) {
                }
            })
            .show()

    }


    fun test(view: View?) {
    }
}