package com.kavya.stealthpad.ui.notes;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kavya.stealthpad.R;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.List;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{

    private List<NotesModel> notes = new ArrayList<>();
    private final int layoutId;

    public NotesAdapter(int id){
        this.layoutId = id;
    }

    public void setNotes(List<NotesModel> notes){
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

        NotesModel note = notes.get(position);
        holder.title.setText(note.getTitle());
        holder.preview.setText(note.getContent());
        holder.date.setText(DateTimeUtils.formatTimestamp(note.getTimestamp()));
        holder.category.setText(note.getCategory());

        // updation feature...
        holder.itemView.setOnClickListener(v->{
            // moving to the notes activity to allow the user so that they can update the note...
            Intent intent = new Intent(v.getContext(), Notes.class);
            intent.putExtra("NOTE_ID", note.getId());
            v.getContext().startActivity(intent);
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
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.note_title);
            this.preview = itemView.findViewById(R.id.note_content);
            this.date = itemView.findViewById(R.id.note_date);
            this.category = itemView.findViewById(R.id.category_chip);
        }
    }

}
