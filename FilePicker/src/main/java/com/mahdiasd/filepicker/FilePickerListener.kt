package com.mahdiasd.filepicker

import android.net.Uri


interface FilePickerListener {
    fun selectedFiles(files: List<FileModel>?, uris: List<Uri>?)
}