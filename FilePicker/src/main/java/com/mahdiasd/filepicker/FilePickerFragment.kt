package com.mahdiasd.filepicker

import android.Manifest
import android.content.ContentResolver
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mahdiasd.filepicker.databinding.FilePickerFragmentBinding
import java.io.File


class FilePickerFragment : BottomSheetDialogFragment() {
    private var maxSelection: Int = 10

    private var myAdapter: FilePickerAdapter? = null
    private lateinit var binding: FilePickerFragmentBinding

    private var totalFiles: MutableList<String> = ArrayList()

    private var imageList: MutableList<FileModel> = ArrayList()
    private var videoList: MutableList<FileModel> = ArrayList()
    private var audioList: MutableList<FileModel> = ArrayList()
    private var docList: MutableList<FileModel> = ArrayList()
    private var fileManagerList: MutableList<FileModel> = ArrayList()


    private var selectedFiles: MutableList<FileModel> = ArrayList()

    private var filePickerListener: FilePickerListener? = null

    private lateinit var config: FilePicker

    companion object {
        fun Builder() = FilePickerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.file_picker_fragment, container, false)
        binding.presenter = this

        checkPermission(true)

        binding.config = config
        initSectionList()

        return binding.root
    }

    fun setConfig(filePicker: FilePicker) {
        this.config = filePicker
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
            Log.e("+++++++++++", "observe")
            config.selectedMode = it
            initRecyclerView()
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
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_file),
                            PickerMode.FILE
                        )
                    )
                }
                PickerMode.Audio -> {
                    temp.add(
                        SectionModel(
                            config.audioText,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_audio),
                            PickerMode.Audio
                        )
                    )
                }
                PickerMode.Video -> {
                    temp.add(
                        SectionModel(
                            config.videoText,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_video),
                            PickerMode.Video
                        )
                    )
                }
                PickerMode.Image -> {
                    temp.add(
                        SectionModel(
                            config.imageText,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_image),
                            PickerMode.Image
                        )
                    )
                }
                PickerMode.Document -> {
                    temp.add(
                        SectionModel(
                            config.documentText,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_document),
                            PickerMode.Document
                        )
                    )
                }
            }
        }
        return temp

    }

    private fun checkPermission(request: Boolean) {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
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

    override fun onResume() {
        super.onResume()
        checkPermission(false)
    }

    private fun getFiles() {
        val temp = config.mode

        if (temp.contains(PickerMode.Video))
            getVideo()

        if (temp.contains(PickerMode.Image))
            getImage()

        if (temp.contains(PickerMode.Document))
            getDoc()

        if (temp.contains(PickerMode.Audio))
            getAudio()

//        val path = Environment.getExternalStorageDirectory().absolutePath
//        val f = File(path)
//        val files: Array<File> = f.listFiles() as Array<File>
//        files.forEach {
//            fileManagerList.add(FileModel(it.path))
//        }

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
        imageList.reverse()
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
        audioList.reverse()
        cursor.close()
    }

    private fun getDoc() {
        val cr: ContentResolver = requireContext().contentResolver
        val uri = MediaStore.Files.getContentUri("external")

        val projection =
            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME)
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE)
        val selectionArgs: Array<String>? = null
        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgsPdf = arrayOf(mimeType)

        val cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, null)?: return

        while (cursor.moveToNext()) {
            val dataColumnIndex: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            docList.add(FileModel(cursor.getString(dataColumnIndex)))
        }
//        docList = docList.filter { it.path.endsWith(".pdf") } as MutableList<FileModel>
        cursor.close()
    }

    private fun getFileFromDirectory(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isDirectory) getFileFromDirectory(it)
                else {
                    totalFiles.add(it.path)
                }
            }
        } else {
            file.listFiles()?.forEach {
                totalFiles.add(it.path)
            }
        }
    }

    private fun initRecyclerView() {
        Log.e("+++++++++++", "initRecyclerView")
        binding.loading = false

        val temp = getSelectedList()

        myAdapter =
            FilePickerAdapter(context, temp, selectedFiles, config.selectedMode, maxSelection)

        binding.list.let {
            it.layoutManager = handleLayoutManager()
            it.adapter = myAdapter
        }

        binding.listSize = temp.size
        handleBehaviorFragment()

        myAdapter!!.liveData.observe(this) {
            binding.countFrame.visibility = if (it == 0) View.GONE else View.VISIBLE
            binding.count.text = it.toString()
        }
        Log.e("+++++++++++", "initRecyclerView+")
    }

    private fun handleLayoutManager(): RecyclerView.LayoutManager {
        return when (config.selectedMode) {
            PickerMode.Document, PickerMode.Audio ->
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
        Log.e("+++++++++++", "getSelectedList")
        return when (config.selectedMode) {
            PickerMode.Audio -> {
                Log.e("+++++++++++", "getSelectedList: ${audioList.size}")
                audioList
            }
            PickerMode.Video -> {
                Log.e("+++++++++++", "getSelectedList: ${videoList.size}")
                videoList
            }
            PickerMode.Image -> {
                Log.e("+++++++++++", "getSelectedList: ${imageList.size}")
                imageList
            }
            PickerMode.Document -> {
                Log.e("+++++++++++", "getSelectedList: ${docList.size}")
                docList
            }
            else -> {
                ArrayList()
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

    fun btn(view: View) {
        filePickerListener?.selectedFiles(selectedFiles)
        selectedFiles.clear()
    }

}