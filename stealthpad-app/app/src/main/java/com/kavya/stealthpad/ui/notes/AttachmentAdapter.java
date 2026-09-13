package com.kavya.stealthpad.ui.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.data.Local.model.NoteAttachment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {

    public interface AttachmentListener {
        void onRemoveAttachment(NoteAttachment attachment);
        void onAttachmentClick(NoteAttachment attachment);
    }

    private List<NoteAttachment> attachments = new ArrayList<>();
    private AttachmentListener listener;

    public void setAttachments(List<NoteAttachment> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    public void setListener(AttachmentListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        NoteAttachment attachment = attachments.get(position);
        
        String path = attachment.getLocalPath();
        if (path != null && new File(path).exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(new File(path))
                    .centerCrop()
                    .into(holder.imgAttachment);
        } else {
            // Show a placeholder or handle missing file
            holder.imgAttachment.setImageResource(R.drawable.vault); 
            holder.imgAttachment.setAlpha(0.5f);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveAttachment(attachment);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAttachmentClick(attachment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    static class AttachmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAttachment;
        ImageButton btnRemove;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAttachment = itemView.findViewById(R.id.img_attachment);
            btnRemove = itemView.findViewById(R.id.btn_remove_attachment);
        }
    }
}
