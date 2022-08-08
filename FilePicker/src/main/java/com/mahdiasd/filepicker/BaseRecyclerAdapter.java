package com.mahdiasd.filepicker;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by : youngkaaa on 2016/10/3.
 * Contact me : 645326280@qq.com
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter {
    private List<T> mLists;
    private final Context mContext;

    public BaseRecyclerAdapter(Context context, List<T> list) {
        this.mContext = context;
        this.mLists = list;
    }

    public abstract int getRootLayoutId();

    public abstract void onBind(BaseViewHolder viewHolder, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBind((BaseViewHolder) holder, position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getRootLayoutId(), parent, false);
        return new BaseViewHolder(binding);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getItemCount() {
        if (mLists == null) return 0;
        return mLists.size();
    }

    public List<T> getList() {
        return mLists;
    }

    public void setLists(List<T> mLists) {
        this.mLists = mLists;
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public BaseViewHolder(ViewDataBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public ViewDataBinding getBinding() {
            return binding;
        }

        public Object getData(int position) {
            if (position >= mLists.size())
                return null;
            return mLists.get(position);
        }
    }
}
