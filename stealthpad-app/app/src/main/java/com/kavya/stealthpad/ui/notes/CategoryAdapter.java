package com.kavya.stealthpad.ui.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kavya.stealthpad.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private final List<CategoryItem> categories;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(List<CategoryItem> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = categories.get(position);
        holder.title.setText(item.name);
        holder.icon.setImageResource(item.iconRes);
        holder.count.setText("Tap to view");
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(item.name));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.category_icon);
            title = itemView.findViewById(R.id.category_name);
            count = itemView.findViewById(R.id.category_count);
        }
    }

    public static class CategoryItem {
        String name;
        int iconRes;

        public CategoryItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }
}
