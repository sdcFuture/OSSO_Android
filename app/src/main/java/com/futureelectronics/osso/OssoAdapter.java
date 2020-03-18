package com.futureelectronics.osso;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.futureelectronics.osso.data.Osso;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.futureelectronics.osso.databinding.ListItemKnownOssoBinding;

import java.util.List;
import java.util.Objects;

/**
 * Created by Kyle Harman on 12/5/2018.
 */
public class OssoAdapter extends RecyclerView.Adapter<OssoAdapter.OssoViewHolder> {

    List<? extends Osso> mOssoList;

    @Nullable
    private final OssoClickCallback mClickCallback;

    public OssoAdapter(@Nullable OssoClickCallback callback) {
        mClickCallback = callback;
        setHasStableIds(true);
    }

    public void setOssoList(final List<? extends Osso> ossoList) {
        if (mOssoList == null) {
            mOssoList = ossoList;
            notifyItemRangeInserted(0, ossoList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mOssoList.size();
                }

                @Override
                public int getNewListSize() {
                    return ossoList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mOssoList.get(oldItemPosition).id ==
                            ossoList.get(newItemPosition).id;
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Osso newProduct = ossoList.get(newItemPosition);
                    Osso oldProduct = mOssoList.get(oldItemPosition);
                    return newProduct.id == oldProduct.id
                            && Objects.equals(newProduct.address, oldProduct.address)
                            && Objects.equals(newProduct.petName, oldProduct.petName)
                            && newProduct.battery == oldProduct.battery;
                }
            });
            mOssoList = ossoList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public OssoViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ListItemKnownOssoBinding binding = ListItemKnownOssoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        binding.setCallback(mClickCallback);
        return new OssoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OssoViewHolder holder, int position)
    {
        holder.binding.setOsso(mOssoList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mOssoList == null ? 0 : mOssoList.size();
    }

    @Override
    public long getItemId(int position) {
        return mOssoList.get(position).id;
    }

    public static class OssoViewHolder extends RecyclerView.ViewHolder {
        final ListItemKnownOssoBinding binding;

        public OssoViewHolder(ListItemKnownOssoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
