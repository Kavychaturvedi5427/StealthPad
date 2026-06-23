package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.utils.SessionManager;

public class AllNotes extends BottomSheetDialogFragment {

    private NotesViewModel notesViewModel;
    private RecyclerView allNotesRecycler;
    private SessionManager sessionManager;
    private TextView notecount;
    private MaterialButton close;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bottom_sheet_all_notes, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);
        // getting logged in user...
        sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getEmail();

        //binding view groups...
        allNotesRecycler = view.findViewById(R.id.recycler_all_notes);
        notecount = view.findViewById(R.id.text_note_count);
        close = view.findViewById(R.id.btn_close);

        // setting up the adapter...
        NotesAdapter notesAdapter = new NotesAdapter(R.layout.item_note_staggered);
        allNotesRecycler.setAdapter(notesAdapter);
        // for brick layout...
        allNotesRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // fetching all the notes...
        notesViewModel.getAllNotes(email).observe(getViewLifecycleOwner(), notes->{
            notesAdapter.setNotes(notes);
            // updating the notes count;
            if (notes != null) {
                notecount.setText(notes.size() + " notes");
            } else {
                notecount.setText("0 notes");
            }
        });

        close.setOnClickListener(v->{
            dismiss();
        });

    }
}
