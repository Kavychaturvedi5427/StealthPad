package com.kavya.stealthpad.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.data.Local.model.NoteWithAttachments;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnNotePickedListener {
        void onNotePicked(NotesModel note);
    }

    private OnNotePickedListener listener;
    private SessionManager sessionManager;

    public void setOnNotePickedListener(OnNotePickedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_note_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        NotesViewModel notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);
        sessionManager = new SessionManager(requireContext());
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_note_picker);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        NotePickerAdapter adapter = new NotePickerAdapter(note -> {
            if (listener != null) {
                listener.onNotePicked(note);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);
        
        view.findViewById(R.id.btn_cancel_picker).setOnClickListener(v -> dismiss());

        String email = sessionManager.getEmail();
        if (email != null) {
            notesViewModel.getAllNotes(email, "NEWEST_CREATED").observe(getViewLifecycleOwner(), notes -> {
                if (notes != null) {
                    adapter.setNotes(notes);
                }
            });
        }
    }

    private static class NotePickerAdapter extends RecyclerView.Adapter<NotePickerAdapter.ViewHolder> {
        
        private List<NoteWithAttachments> notes = new ArrayList<>();
        private final OnNotePickedListener listener;

        public NotePickerAdapter(OnNotePickedListener listener) {
            this.listener = listener;
        }

        public void setNotes(List<NoteWithAttachments> notes) {
            this.notes = notes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotesModel note = notes.get(position).note;
            holder.title.setText(note.getTitle());
            holder.preview.setText(note.getContent());
            holder.itemView.setOnClickListener(v -> listener.onNotePicked(note));
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, preview;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.note_title);
                preview = itemView.findViewById(R.id.note_preview);
            }
        }
    }
}
