package com.mahdiasd.filepicker

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.File

data class FileModel(
    var path: String
) : BaseObservable() {

    var file = File(path)

    @Bindable
    var selected = false
        set(value) {
            notifyPropertyChanged(BR.selected)
            field = value
        }
}
