package com.mahdiasd.filepicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.util.Log.d
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mahdiasd.filepicker.databinding.ItemFilePickerBinding
import com.mahdiasd.filepicker.databinding.ItemFilePickerManagerBinding
import java.io.File

class FilePickerAdapter(
    context: Context?,
    list: List<FileModel>?,
    private var selectedFiles: MutableList<FileModel>,
    private val pickerMode: PickerMode,
    private val config: FilePicker
) : BaseRecyclerAdapter<FileModel>(context, list) {

    private var stack = ArrayDeque<List<FileModel?>?>()
    var liveData: MutableLiveData<Int> = MutableLiveData<Int>()

    init {
        stack.add(list)
    }

    override fun getRootLayoutId(): Int {
        return if (pickerMode == PickerMode.Audio || pickerMode == PickerMode.FILE)
            R.layout.item_file_picker_manager
        else
            R.layout.item_file_picker
    }

    override fun onBind(viewHolder: BaseViewHolder, position: Int) {
        val model = viewHolder.getData(position) as FileModel

        when (pickerMode) {
            PickerMode.Audio, PickerMode.FILE -> {
                val itemBinding = viewHolder.binding as ItemFilePickerManagerBinding
                itemBinding.let {
                    it.item = model
                    it.presenter = this
                    it.position = position
                    it.pickerMode = pickerMode
                    it.subFolderCount = model.file.listFiles()?.size ?: 0
                    it.stackListSize = stack.size
                    it.activeColor = config.activeColor
                    it.checkbox.buttonTintList = ColorStateList.valueOf(config.activeColor)
                }
            }
            else -> {
                val itemBinding = viewHolder.binding as ItemFilePickerBinding
                itemBinding.let {
                    it.item = model
                    glideSdCart(itemBinding.image, model.path)
                    it.presenter = this
                    it.type = config.selectedMode
                    it.checkbox.buttonTintList = ColorStateList.valueOf(config.activeColor)
                }
            }
        }
    }

    fun checkBox(view: View?, file: FileModel) {
        file.selected = !file.selected
        if (selectedFiles.size >= config.maxSelection) {
            selectedFiles.last().selected = false
            selectedFiles.removeLast()
        }

        if (file.selected)
            selectedFiles.add(file)
        else
            selectedFiles.remove(file)

        liveData.postValue(selectedFiles.size)
    }

    fun onClick(view: View, fileModel: FileModel) {
        if (config.showFileWhenClick)
            openFile(fileModel.path)
        else
            checkBox(null, fileModel)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun back(view: View) {
        stack.removeLast()
        if (stack.isNotEmpty()) {
            setLists(stack.last())
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun nextFolder(view: View, file: FileModel) {
        if (file.file.isDirectory && file.file.listFiles() != null) {
            val temp: MutableList<FileModel> = ArrayList()
            if (file.file.listFiles()!!.isEmpty()) return

            file.file.listFiles()?.forEach {
                temp.add(FileModel(it.path))
            }
            setLists(temp)
            stack.add(list)
            notifyDataSetChanged()
        }
    }

    private fun glideSdCart(view: ImageView, imageUrl: String?) {
        if (imageUrl == null) return
        val file = File(imageUrl)
        if (!file.exists()) {
            return
        }
        val imageUri = Uri.fromFile(file) ?: return
        Glide.with(view.context)
            .load(imageUri)
            .placeholder(R.color.gray)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view)
    }

    private fun openFile(fileAddress: String?) {
        if (fileAddress == null) return
        try {
            val file = File(fileAddress)
            val map = MimeTypeMap.getSingleton()
            val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
            val type = map.getMimeTypeFromExtension(ext)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "com.mahdiasd.filepicker.provider",
                    file
                ),
                type
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            d("TAG", "openFile: ${e.message}")
            Toast.makeText(
                context,
                context.resources.getString(R.string.failedOpenFile),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}