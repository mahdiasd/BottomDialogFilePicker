package com.mahdiasd.filepicker

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.fragment.app.FragmentManager

data class FilePicker(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    var listener: FilePickerListener? = null,

    var mode: List<PickerMode> = listOf(PickerMode.Image, PickerMode.Video, PickerMode.Audio),
    private var defaultMode: PickerMode? = null,

    var videoText: String = "Video",
    var audioText: String = "Audio",
    var documentText: String = "Document",
    var fileManagerText: String = "File",
    var imageText: String = "Image",

    var showFileWhenClick: Boolean = false,
    var maxSelection: Int = 10,

    var activeColor: Int = ContextCompat.getColor(context, R.color.colorPrimary),
    var deActiveColor: Int = ContextCompat.getColor(context, R.color.gray),
    var cardBackgroundColor: Int = ContextCompat.getColor(context, R.color.white),
) :
    BaseObservable() {
    private val fragmentTag = "mahdiasd_file_picker"
    private var filePickerFragment = FilePickerFragment.Builder()

    @Bindable
    var selectedMode: PickerMode = defaultMode()
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

    fun setMaxSelection(value: Int): FilePicker {
        this.maxSelection = value
        return this
    }


    fun showFileWhenClick(value: Boolean): FilePicker {
        this.showFileWhenClick = value
        return this
    }

    fun setActiveColor(value: Int): FilePicker {
        if (value > 0)
            this.activeColor = ContextCompat.getColor(context, value)
        else
            this.activeColor = value
        return this
    }

    fun setDeActiveColor(value: Int): FilePicker {
        if (value > 0)
            this.deActiveColor = ContextCompat.getColor(context, value)
        else
            this.deActiveColor = value
        return this
    }

    fun setCardBackgroundColor(value: Int): FilePicker {
        if (value > 0)
            this.cardBackgroundColor = ContextCompat.getColor(context, value)
        else
            this.cardBackgroundColor = value
        return this
    }

    fun defaultMode(defaultMode: PickerMode): FilePicker {
        this.defaultMode = defaultMode
        return this
    }

    fun defaultMode(): PickerMode {
        return if (defaultMode == null || defaultMode == PickerMode.FILE) {
            if (mode.first() == PickerMode.FILE && mode.size != 1) {
                mode[1]
            } else
                mode.first()
        } else
            defaultMode!!
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
        selectedMode = defaultMode()
        filePickerFragment.show(fragmentManager, fragmentTag)
        filePickerFragment.setConfig(this)
    }
}
