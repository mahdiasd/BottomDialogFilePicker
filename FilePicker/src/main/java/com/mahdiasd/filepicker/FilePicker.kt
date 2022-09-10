package com.mahdiasd.filepicker

import android.content.Context
import android.graphics.drawable.Drawable
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
    var openStorageText: String = "Open Storage",
    var maxTotalFileSizeText: String = "Max size for total files is ",
    var maxEachFileSizeText: String = "Max size for each files is ",

    var videoIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_video),
    var audioIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_audio),
    var documentIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_document),
    var fileManagerIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_file),
    var imageIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_image),

    var showFileWhenClick: Boolean = false,
    var maxSelection: Int = 10,

    var totalFileSize: Int? = null,
    var eachFileSize: Int? = null,

    var activeColor: Int = ContextCompat.getColor(context, R.color.colorPrimary),
    var deActiveColor: Int = ContextCompat.getColor(context, R.color.gray),
    var cardBackgroundColor: Int = ContextCompat.getColor(context, R.color.white),
) :
    BaseObservable() {
    private val fragmentTag = "mahdiasd_file_picker"
    private var filePickerFragment = FilePickerFragment.newInstance()

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

    fun setIcons(
        videoIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_video),
        audioIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_audio),
        documentIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_document),
        fileManagerIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_file),
        imageIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_image),
    ): FilePicker {
        this.videoIcon
        this.audioIcon
        this.documentIcon
        this.fileManagerIcon
        this.imageIcon
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

    fun setTotalFileSize(value: Int): FilePicker {
        this.totalFileSize = value
        return this
    }

    fun setEachFileSize(value: Int): FilePicker {
        this.eachFileSize = value
        return this
    }

    fun setShowFileWhenClick(value: Boolean): FilePicker {
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

    fun setDefaultMode(defaultMode: PickerMode): FilePicker {
        this.defaultMode = defaultMode
        return this
    }

    fun defaultMode(): PickerMode {
        return if (defaultMode == null) {
            if (mode.isEmpty())
                PickerMode.Image
            else mode.first()
        } else {
            if (!mode.contains(defaultMode)) mode.first()
            else defaultMode!!
        }
    }

    fun setCustomText(
        videoText: String = "Video",
        audioText: String = "Audio",
        fileManagerText: String = "File Manager",
        imageText: String = "Image",
        openStorageText: String = "Open Storage"
    ): FilePicker {
        this.videoText = videoText
        this.audioText = audioText
        this.fileManagerText = fileManagerText
        this.imageText = imageText
        this.openStorageText = openStorageText
        return this
    }

    fun show() {
        if (fragmentManager.findFragmentByTag(fragmentTag) != null) return
        selectedMode = defaultMode()
        filePickerFragment.show(fragmentManager, fragmentTag)
        filePickerFragment.setConfig(this)
    }
}
