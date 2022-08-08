package com.mahdiasd.filepicker

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
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
    private val maxSelection: Int
) : BaseRecyclerAdapter<FileModel>(context, list) {

    private var stack = ArrayDeque<List<FileModel?>?>()
    var liveData: MutableLiveData<Int> = MutableLiveData<Int>()

    init {
        stack.add(list)
    }

    override fun getRootLayoutId(): Int {
        return if (pickerMode == PickerMode.Document || pickerMode == PickerMode.Audio)
            R.layout.item_file_picker_manager
        else
            R.layout.item_file_picker
    }

    override fun onBind(viewHolder: BaseViewHolder, position: Int) {
        val model = viewHolder.getData(position) as FileModel

        if (pickerMode == PickerMode.Document || pickerMode == PickerMode.Audio) {
            val itemBinding = viewHolder.binding as ItemFilePickerManagerBinding
            itemBinding.let {
                it.item = model
                it.presenter = this
                it.position = position
                it.pickerMode = pickerMode
                it.subFolderCount = model.file.listFiles()?.size ?: 0
                it.stackListSize = stack.size
            }

        } else {
            val itemBinding = viewHolder.binding as ItemFilePickerBinding
            itemBinding.let {
                it.item = model
                glideSdCart(itemBinding.image, model.path)
                it.presenter = this
            }
        }
    }

    fun checkBox(view: View, file: FileModel) {
        if (selectedFiles.size >= maxSelection) {
            selectedFiles.last().selected = false
            selectedFiles.removeLast()
        }

        file.selected = (view as CheckBox).isChecked

        if (view.isChecked)
            selectedFiles.add(file)
        else
            selectedFiles.remove(file)


        liveData.postValue(selectedFiles.size)
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
            val temp: MutableList<String> = ArrayList()
            if (file.file.listFiles()!!.isEmpty()) return
            file.file.listFiles()?.forEach {
                temp.add(it.path)
            }
            setLists(temp as List<FileModel?>?)
            stack.add(list)
            notifyDataSetChanged()
        }
    }

    fun glideSdCart(view: ImageView, imageUrl: String?) {
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

}