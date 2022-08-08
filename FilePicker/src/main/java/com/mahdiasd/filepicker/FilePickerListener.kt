package com.mahdiasd.filepicker


interface FilePickerListener {
    fun selectedFiles(list: List<FileModel>?)
}