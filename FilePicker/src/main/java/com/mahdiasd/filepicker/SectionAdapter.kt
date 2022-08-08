package com.mahdiasd.filepicker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.mahdiasd.filepicker.databinding.ItemSectionBinding

class SectionAdapter(
    context: Context,
    list: List<SectionModel>,
    private var selectedMode: PickerMode,
    private val activeColor: Int,
    private val deActiveColor: Int,
) : BaseRecyclerAdapter<SectionModel>(context, list) {

    override fun getRootLayoutId(): Int {
        return R.layout.item_section
    }

    var changeMode: MutableLiveData<PickerMode> = MutableLiveData<PickerMode>()

    override fun onBind(viewHolder: BaseViewHolder, position: Int) {
        val model = viewHolder.getData(position) as SectionModel
        val itemBinding = viewHolder.binding as ItemSectionBinding
        itemBinding.item = model
        itemBinding.activeColor = activeColor
        itemBinding.deActiveColor = deActiveColor

        viewHolder.itemView.setOnClickListener {
            changeMode.postValue(model.mode)
            if (model.mode != PickerMode.FILE)
                refreshSelceted(model)
        }
    }

    private fun refreshSelceted(model: SectionModel) {
        list.forEach { it.selected = false }
        model.selected = true
    }
}