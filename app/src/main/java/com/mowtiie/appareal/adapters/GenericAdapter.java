package com.mowtiie.appareal.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowtiie.appareal.databinding.ItemRowBinding;

import java.util.ArrayList;
import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.Holder> {

    public interface Binder<T> {
        void bind(ItemRowBinding b, T item, int position);
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;

    public GenericAdapter(Binder<T> binder) {
        this.binder = binder;
    }

    public void setItems(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        binder.bind(holder.b, items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRowBinding b;

        Holder(ItemRowBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
