package com.kavya.stealthpad.ui.notes;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.data.Local.model.NoteWithAttachments;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{

    public interface NotesListener {
        void onNoteClick(NotesModel note);
        void onNoteLongClick(NotesModel note);
    }

    private List<NoteWithAttachments> notes = new ArrayList<>();
    private final int layoutId;
    private NotesListener listener;
    private String sortOrder = "RECENTLY_UPDATED";

    public NotesAdapter(int id){
        this.layoutId = id;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        notifyDataSetChanged();
    }

    public void setNotesListener(NotesListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<NoteWithAttachments> notes){
        this.notes = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this will inflate the notes preview layout and return it to the parent....
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {

        NoteWithAttachments noteWithAttachments = notes.get(position);
        NotesModel note = noteWithAttachments.note;
        
        holder.title.setText(note.getTitle());
        holder.preview.setText(note.getContent());
        
        // Determine which timestamp and label to use based on sort order
        if ("RECENTLY_UPDATED".equals(sortOrder)) {
            // Use updatedAt (with createdAt fallback) and human-friendly relative formatting
            holder.date.setText(DateTimeUtils.formatRelativeTime(note.getLastUpdated(), "Updated"));
        } else if ("OLDEST_CREATED".equals(sortOrder) || "NEWEST_CREATED".equals(sortOrder)) {
            // For creation-based sorts, show creation date
            holder.date.setText(DateTimeUtils.formatRelativeTime(note.getTimestamp(), "Created"));
        } else {
            // Default fallback
            holder.date.setText(DateTimeUtils.formatRelativeTime(note.getLastUpdated(), "Updated"));
        }

        holder.category.setText(note.getCategory());
        if (holder.folderTag != null) {
            holder.folderTag.setText(note.getCategory() != null ? note.getCategory().toUpperCase() : "GENERAL");
        }

        if (noteWithAttachments.attachments != null && !noteWithAttachments.attachments.isEmpty()) {
            String path = noteWithAttachments.attachments.get(0).getLocalPath();
            if (path != null && new File(path).exists()) {
                holder.imagePreview.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(new File(path))
                        .centerCrop()
                        .into(holder.imagePreview);
            } else {
                holder.imagePreview.setVisibility(View.GONE);
            }
        } else {
            holder.imagePreview.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            } else {
                // Fallback to existing behavior if no listener set
                Intent intent = new Intent(v.getContext(), Notes.class);
                intent.putExtra("NOTE_ID", note.getId());
                intent.putExtra("IS_VAULT", note.isVault());
                v.getContext().startActivity(intent);
                if (v.getContext() instanceof android.app.Activity) {
                    ((android.app.Activity) v.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNoteLongClick(note);
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // fetching the id of the viewgroups from the item holder...
    static class NotesViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView preview;
        TextView date;
        TextView category;
        TextView folderTag;
        ImageView imagePreview;
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.note_title);
            this.preview = itemView.findViewById(R.id.note_content);
            this.date = itemView.findViewById(R.id.note_date);
            this.category = itemView.findViewById(R.id.category_chip);
            this.folderTag = itemView.findViewById(R.id.folder_tag);
            this.imagePreview = itemView.findViewById(R.id.note_image_preview);
        }
    }

}
