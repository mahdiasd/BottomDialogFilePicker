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

    var mode: List<PickerMode> = listOf(PickerMode.Image, PickerMode.Image, PickerMode.Video, PickerMode.Audio),
    private var defaultMode: PickerMode? = null,

    var videoText: String = context.getString(R.string.mahdiasd_file_picker_video),
    var audioText: String = context.getString(R.string.mahdiasd_file_picker_audio),
    var fileManagerText: String = context.getString(R.string.mahdiasd_file_picker_file_manager),
    var imageText: String = context.getString(R.string.mahdiasd_file_picker_image),
    var cameraText: String = context.getString(R.string.mahdiasd_file_picker_camera),
    var openStorageText: String = context.getString(R.string.mahdiasd_file_picker_open_storage),
    var maxTotalFileSizeText: String = context.getString(R.string.mahdiasd_file_picker_max_total_size),
    var maxEachFileSizeText: String = context.getString(R.string.mahdiasd_file_picker_max_each_size),

    var videoIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_video),
    var audioIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_audio),
    var documentIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_document),
    var fileManagerIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_file),
    var imageIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_image),
    var searchIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_search),
    var doneIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_send),

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
        doneIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_send),
        searchIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_search),
    ): FilePicker {
        this.searchIcon = searchIcon
        this.videoIcon = videoIcon
        this.audioIcon = audioIcon
        this.documentIcon = documentIcon
        this.fileManagerIcon = fileManagerIcon
        this.imageIcon = imageIcon
        this.doneIcon = doneIcon

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

    fun setMaxTotalFileSize(value: Int): FilePicker {
        this.totalFileSize = value
        return this
    }

    fun setMaxEachFileSize(value: Int): FilePicker {
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
        videoText: String = context.getString(R.string.mahdiasd_file_picker_video),
        audioText: String = context.getString(R.string.mahdiasd_file_picker_audio),
        fileManagerText: String = context.getString(R.string.mahdiasd_file_picker_file_manager),
        imageText: String = context.getString(R.string.mahdiasd_file_picker_image),
        cameraText: String = context.getString(R.string.mahdiasd_file_picker_camera),
        openStorageText: String = context.getString(R.string.mahdiasd_file_picker_open_storage),
        maxTotalFileSizeText: String = context.getString(R.string.mahdiasd_file_picker_max_total_size),
        maxEachFileSizeText: String = context.getString(R.string.mahdiasd_file_picker_max_each_size),
    ): FilePicker {
        this.videoText = videoText
        this.audioText = audioText
        this.fileManagerText = fileManagerText
        this.imageText = imageText
        this.cameraText = cameraText
        this.openStorageText = openStorageText
        this.maxTotalFileSizeText = maxTotalFileSizeText
        this.maxEachFileSizeText = maxEachFileSizeText
        return this
    }

    fun show() {
        if (fragmentManager.findFragmentByTag(fragmentTag) != null) return
        selectedMode = defaultMode()
        filePickerFragment.show(fragmentManager, fragmentTag)
        filePickerFragment.setConfig(this)
    }
}
