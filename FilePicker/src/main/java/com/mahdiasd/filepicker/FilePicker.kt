package com.mahdiasd.filepicker

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.fragment.app.FragmentManager

data class FilePicker(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    var mode: List<PickerMode> = listOf(PickerMode.Audio, PickerMode.Video, PickerMode.Image),
    var defaultMode: PickerMode = mode.first(),
    var listener: FilePickerListener? = null,
    var videoText: String = "Video",
    var audioText: String = "Audio",
    var documentText: String = "Document",
    var fileManagerText: String = "File Manager",
    var imageText: String = "Image",
    var activeColor: Int = ContextCompat.getColor(context, R.color.colorPrimary),
    var deActiveColor: Int = ContextCompat.getColor(context, R.color.gray),
) :
    BaseObservable() {
    private val fragmentTag = "mahdiasd_file_picker"
    private var filePickerFragment = FilePickerFragment.Builder()

    @Bindable
    var selectedMode: PickerMode = defaultMode
        set(value) {
            notifyPropertyChanged(BR.selectedMode)
            field = value
        }

    fun setListener(listener: FilePickerListener): FilePicker {
        this.listener = listener
        return this
    }

    fun setMode(vararg modes: PickerMode): FilePicker {
        this.mode = modes.toList()
        return this
    }


    fun defaultMode(defaultMode: PickerMode): FilePicker {
        this.defaultMode = defaultMode
        return this
    }

    fun setCustomText(
        videoText: String = "Video",
        audioText: String = "Audio",
        fileManagerText: String = "File Manager",
        imageText: String = "Image"
    ): FilePicker {
        this.videoText = videoText
        this.audioText = audioText
        this.fileManagerText = fileManagerText
        this.imageText = imageText
        return this
    }

    fun show() {
        if (fragmentManager.findFragmentByTag(fragmentTag) != null) return
        filePickerFragment.show(fragmentManager, fragmentTag)
        filePickerFragment.setConfig(this)
    }
}
