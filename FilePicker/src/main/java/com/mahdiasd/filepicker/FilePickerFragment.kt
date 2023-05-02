package com.mahdiasd.filepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    private var imageList: MutableList<FileModel> = ArrayList()
    private var videoList: MutableList<FileModel> = ArrayList()
    private var audioList: MutableList<FileModel> = ArrayList()

    private var selectedFiles: MutableList<FileModel> = ArrayList()

    private lateinit var config: FilePicker
    private var storageIsOpen = false
    private var cameraImagePath = ""

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

        if (config.mode.size == 1 && config.selectedMode == PickerMode.File)
            openFileManager()
        else
            initSectionList()


        return binding.root
    }

    private fun checkPermission() {
        if (isGrant(Manifest.permission.READ_EXTERNAL_STORAGE)) getFiles()

        val list = arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (config.mode.contains(PickerMode.Camera)) {
            list.add(Manifest.permission.CAMERA)
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        requestMultiplePermissions.launch(list.toTypedArray())
    }

    private fun isGrant(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
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
            it.layoutManager = (LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
            it.adapter = sectionAdapter
        }

        sectionAdapter.changeMode.observe(this) {
            config.selectedMode = it
            initRecyclerView()
            if (it == PickerMode.Camera) {
                openCamera()
            }
        }

    }

    private fun getUriFromFile(file: File?): Uri? {
        if (file == null) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        else
            Uri.fromFile(file)
    }


    fun openCamera() {
        if (!isGrant(Manifest.permission.CAMERA) || !isGrant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            checkPermission()
        else {
            cameraImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + System.currentTimeMillis() + ".png"
            val file = File(cameraImagePath)
            file.createNewFile()
            val outputFileUri = getUriFromFile(file)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            cameraLauncher.launch(cameraIntent)
        }
    }

    private fun openFileManager() {
        storageIsOpen = true
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, config.maxSelection > 1)
        storageLauncher.launch(intent)

        handlePathOz =
            HandlePathOz(requireContext(), object : HandlePathOzListener.MultipleUri {
                override fun onRequestHandlePathOz(listPathOz: List<PathOz>, tr: Throwable?) {
                    listPathOz.forEach {
                        val file = File(it.path)
                        if (file.exists()) {
                            val fileModel = FileModel(it.path)
                            if (checkMaxSize(fileModel) && selectedFiles.size < config.maxSelection) {
                                fileModel.selected = true
                                selectedFiles.add(fileModel)
                            }
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.mahdiasd_file_picker_cant_find_this_file), Toast.LENGTH_SHORT).show()
                        }
                    }
                    initRecyclerView()
                }
            })
    }

    private fun checkMaxSize(file: FileModel): Boolean {
        val fileSize = (file.file.length() / 1024).toInt()
        var totalSize = fileSize

        selectedFiles.forEach {
            totalSize += (it.file.length() / 1024).toInt()
        }

        return when {
            config.eachFileSize != null && fileSize > config.eachFileSize!! -> {
                Toast.makeText(
                    context, "${config.maxEachFileSizeText} ${config.eachFileSize}kb",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            config.totalFileSize != null && totalSize > config.totalFileSize!! -> {
                Toast.makeText(
                    context, "${config.maxTotalFileSizeText} ${config.totalFileSize}kb",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            else -> true
        }
    }

    private var cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            initRecyclerView()
            result.data?.extras?.get("data")?.let {
//                val imageBitmap = it as Bitmap?
//                val file = saveBitmapToStorage(imageBitmap)
                getImage()
                if (File(cameraImagePath).exists()) {
                    val fileModel = FileModel(cameraImagePath)
                    if (checkMaxSize(fileModel) && selectedFiles.size < config.maxSelection) {
                        fileModel.selected = true
                        selectedFiles.add(fileModel)
                        initRecyclerView()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.mahdiasd_file_picker_cant_find_this_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @OptIn(FlowPreview::class)
    private var storageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                PickerMode.File -> {
                    temp.add(
                        SectionModel(
                            config.fileManagerText,
                            config.fileManagerIcon,
                            PickerMode.File
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
                else -> {}
            }
        }
        temp.find { it.mode == config.selectedMode }?.selected = true
        return temp
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                getFiles()
            } else {
                Toast.makeText(requireContext(), R.string.mahdiasd_file_picker_permission_denied_toast, Toast.LENGTH_SHORT).show()
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
        imageList.clear()
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

        if (config.mode.contains(PickerMode.Camera)) {
            imageList.takeIf { it.find { it.path == "Camera" } == null }?.let { imageList.add(0, FileModel("Camera")) }
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
        if (binding.edt.text.toString().isNotEmpty() && config.selectedMode != PickerMode.File)
            temp = temp.filter {
                it.file.name.contains(binding.edt.text.toString(), false)
            } as MutableList<FileModel>

        if (selectedFiles.isEmpty())
            temp.onEach { it.selected = false }
        else
            selectedFiles.forEach {
                temp.find { t -> t.path == it.path }?.let { te -> te.selected = true }
            }

        myAdapter = FilePickerAdapter(context, temp, selectedFiles, config.selectedMode, config, this)

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
            PickerMode.Audio, PickerMode.File ->
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
                    bottomSheet.setBackgroundResource(android.R.color.transparent)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    bottomSheet.setBackgroundResource(android.R.color.transparent)
                    binding.footer.y =
                        ((bottomSheet.parent as View).height - bottomSheet.top - binding.footer.height).toFloat()
                    binding.done.y =
                        (((bottomSheet.parent as View).height - bottomSheet.top - binding.done.height).toFloat() - binding.footer.height / 1.2).toFloat()
                    binding.countFrame.y =
                        (((bottomSheet.parent as View).height - bottomSheet.top - binding.done.height).toFloat() - binding.footer.height / 1.9).toFloat()

                    binding.countFrame.visibility = if (selectedFiles.size == 0) View.GONE else View.VISIBLE
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
        val uris: MutableList<Uri> = ArrayList()
        selectedFiles.forEach {
            getUriFromFile(it.file)?.let { it1 -> uris.add(it1) }
        }
        config.listener?.selectedFiles(selectedFiles, uris)
        selectedFiles.clear()
        dismiss()
    }

    fun RecyclerView.disableItemAnimator() {
        //disable blink when notifyDataSetChanged
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

}