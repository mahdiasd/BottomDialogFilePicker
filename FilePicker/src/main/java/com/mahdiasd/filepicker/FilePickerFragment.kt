package com.mahdiasd.filepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import br.com.onimur.handlepathoz.HandlePathOz
import br.com.onimur.handlepathoz.HandlePathOzListener
import br.com.onimur.handlepathoz.model.PathOz
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mahdiasd.filepicker.databinding.FilePickerFragmentBinding
import kotlinx.coroutines.FlowPreview
import java.io.File


class FilePickerFragment : BottomSheetDialogFragment() {
    private var handlePathOz: HandlePathOz? = null

    private var myAdapter: FilePickerAdapter? = null
    private lateinit var binding: FilePickerFragmentBinding

    private var totalFiles: MutableList<String> = ArrayList()

    private var imageList: MutableList<FileModel> = ArrayList()
    private var videoList: MutableList<FileModel> = ArrayList()
    private var audioList: MutableList<FileModel> = ArrayList()

    private var selectedFiles: MutableList<FileModel> = ArrayList()

    private lateinit var config: FilePicker
    private var storageIsOpen = false
    private val TAG = "TAG"

    companion object {
        fun newInstance() = FilePickerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.file_picker_fragment, container, false)
        binding.presenter = this
        binding.config = config

        if (config.mode.size == 1 && config.selectedMode == PickerMode.FILE)
            openFileManager()
        else
            initSectionList()

        return binding.root
    }

    fun setConfig(filePicker: FilePicker) {
        this.config = filePicker
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun initSectionList() {
        val sectionAdapter = SectionAdapter(
            requireContext(),
            getSectionList(),
            config.selectedMode,
            config.activeColor,
            config.deActiveColor
        )

        binding.sectionList.let {
            it.layoutManager =
                (LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
            it.adapter = sectionAdapter
        }

        sectionAdapter.changeMode.observe(this) {
            config.selectedMode = it
            initRecyclerView()

        }

    }

    private fun openFileManager() {
        storageIsOpen = true
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, config.maxSelection > 1)
        resultLauncher.launch(intent)

        handlePathOz =
            HandlePathOz(requireContext(), object : HandlePathOzListener.MultipleUri {
                override fun onRequestHandlePathOz(listPathOz: List<PathOz>, tr: Throwable?) {
                    listPathOz.forEach {
                        val file = File(it.path)
                        if (file.exists()) {
                            val fileModel = FileModel(it.path)
                            fileModel.selected = true
                            selectedFiles.add(fileModel)
                        }
                    }
                    initRecyclerView()
                }
            })
    }

    @OptIn(FlowPreview::class)
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            storageIsOpen = false
            if (result.resultCode == Activity.RESULT_OK) {
                storageIsOpen = false
                result.data?.clipData?.also {
                    val a: MutableList<Uri> = ArrayList()
                    for (i in 0 until it.itemCount) {
                        a.add(it.getItemAt(i).uri)
                    }
                    handlePathOz?.getListRealPath(a)
                }

                result.data?.data?.also {
                    handlePathOz?.getListRealPath(arrayListOf(it))
                }
            }
        }

    private fun getSectionList(): MutableList<SectionModel> {
        val temp: MutableList<SectionModel> = ArrayList()
        config.mode.forEach {
            when (it) {
                PickerMode.FILE -> {
                    temp.add(
                        SectionModel(
                            config.fileManagerText,
                            config.fileManagerIcon,
                            PickerMode.FILE
                        )
                    )
                }
                PickerMode.Audio -> {
                    temp.add(
                        SectionModel(
                            config.audioText,
                            config.audioIcon,
                            PickerMode.Audio
                        )
                    )
                }
                PickerMode.Video -> {
                    temp.add(
                        SectionModel(
                            config.videoText,
                            config.videoIcon,
                            PickerMode.Video
                        )
                    )
                }
                PickerMode.Image -> {
                    temp.add(
                        SectionModel(
                            config.imageText,
                            config.imageIcon,
                            PickerMode.Image
                        )
                    )
                }
            }
        }
        temp.find { it.mode == config.selectedMode }?.selected = true
        return temp
    }

    private fun checkPermission() {
        val hasPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS)

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value == true }) {
                getFiles()
            } else {
                Toast.makeText(requireContext(), "permission needed!!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }


    private fun getFiles() {
        val temp = config.mode

        if (temp.contains(PickerMode.Video))
            getVideo()

        if (temp.contains(PickerMode.Image))
            getImage()

        if (temp.contains(PickerMode.Audio))
            getAudio()

        initRecyclerView()
    }

    private fun getImage() {
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)

        val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"

        val cursor: Cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            orderBy
        ) ?: return

        while (cursor.moveToNext()) {
            val dataColumnIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            imageList.add(FileModel(cursor.getString(dataColumnIndex)))
        }
        cursor.close()
    }

    private fun getVideo() {
        val columns = arrayOf(
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns._ID
        )

        val orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC"

        val cursor: Cursor = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, orderBy
        ) ?: return

        while (cursor.moveToNext()) {
            val dataColumnIndex: Int =
                cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
            videoList.add(FileModel(cursor.getString(dataColumnIndex)))
        }
        cursor.close()
    }

    private fun getAudio() {
        val columns = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATA,
        )
        val orderBy = MediaStore.Audio.AudioColumns.DATE_ADDED + " DESC"

        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            orderBy
        ) ?: return

        while (cursor.moveToNext()) {
            val dataColumnIndex: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            audioList.add(FileModel(cursor.getString(dataColumnIndex)))
        }
        cursor.close()
    }

    private fun initRecyclerView() {
        binding.loading = false

        var temp = getSelectedList()
        if (binding.edt.text.toString().isNotEmpty() && config.selectedMode != PickerMode.FILE)
            temp = temp.filter {
                it.file.name.contains(
                    binding.edt.text.toString(),
                    false
                )
            } as MutableList<FileModel>


        myAdapter = FilePickerAdapter(context, temp, selectedFiles, config.selectedMode, config)

        binding.list.let {
            it.layoutManager = handleLayoutManager()
            it.adapter = myAdapter
            it.disableItemAnimator()
        }

        binding.listSize = temp.size

        handleBehaviorFragment()

        myAdapter!!.liveData.observe(this) {
            binding.countFrame.visibility = if (it == 0) View.GONE else View.VISIBLE
            binding.count.text = it.toString()
        }
    }

    private fun handleLayoutManager(): RecyclerView.LayoutManager {
        return when (config.selectedMode) {
            PickerMode.Audio, PickerMode.FILE ->
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            else -> {
                val gridCount =
                    if (isTablet() && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5
                    else if (isTablet() && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 4
                    else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4
                    else 3
                GridLayoutManager(context, gridCount)
            }
        }
    }

    private fun getSelectedList(): MutableList<FileModel> {
        return when (config.selectedMode) {
            PickerMode.Audio -> {
                audioList
            }
            PickerMode.Video -> {
                videoList
            }
            PickerMode.Image -> {
                imageList
            }
            else -> {
                selectedFiles
            }
        }
    }

    private fun isTablet(): Boolean {
        return requireContext().resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    private fun handleBehaviorFragment() {
        try {
            val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
            behavior.peekHeight = 1500

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.footer.y =
                        ((bottomSheet.parent as View).height - bottomSheet.top - binding.footer.height).toFloat()
                    binding.done.y =
                        (((bottomSheet.parent as View).height - bottomSheet.top - binding.done.height).toFloat() - binding.footer.height / 1.2).toFloat()
                    binding.countFrame.y =
                        (((bottomSheet.parent as View).height - bottomSheet.top - binding.done.height).toFloat() - binding.footer.height / 1.9).toFloat()

                    binding.countFrame.visibility =
                        if (selectedFiles.size == 0) View.GONE else View.VISIBLE
                    binding.count.text = selectedFiles.size.toString()
                }
            }.apply {
                binding.root.post { onSlide(binding.root.parent as View, 0f) }
            })
        } catch (e: Exception) {
        }

    }

    fun openStorage(view: View?) {
        openFileManager()
    }

    fun search(s: CharSequence, start: Int, before: Int, count: Int) {
        initRecyclerView()
    }


    fun btn(view: View?) {
        config.listener?.selectedFiles(selectedFiles)
        selectedFiles.clear()
        dismiss()
    }

    fun RecyclerView.disableItemAnimator() {
        //disable blink when notifyDataSetChanged
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

}