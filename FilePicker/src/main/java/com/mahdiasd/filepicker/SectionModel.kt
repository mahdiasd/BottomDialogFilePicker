package com.mahdiasd.filepicker

import android.graphics.drawable.Drawable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

data class SectionModel(
    var text: String,
    var icon: Drawable?,
    var mode: PickerMode,
) : BaseObservable() {
    @Bindable
    var selected = false
        set(value) {
            notifyPropertyChanged(BR.selected)
            field = value
        }

}