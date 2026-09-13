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
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.SessionManager;

import android.content.Intent;
import android.widget.Toast;

public class AllNotes extends BottomSheetDialogFragment {

    private NotesViewModel notesViewModel;
    private RecyclerView allNotesRecycler;
    private SessionManager sessionManager;
    private TextView notecount;
    private MaterialButton close;
    private androidx.appcompat.widget.SearchView searchView;
    private String email;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

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
        email = sessionManager.getEmail();

        //binding view groups...
        allNotesRecycler = view.findViewById(R.id.recycler_all_notes);
        notecount = view.findViewById(R.id.text_note_count);
        close = view.findViewById(R.id.btn_close);
        searchView = view.findViewById(R.id.search_view);

        // setting up the adapter...
        NotesAdapter notesAdapter = new NotesAdapter(R.layout.item_note_staggered);
        notesAdapter.setNotesListener(new NotesAdapter.NotesListener() {
            @Override
            public void onNoteClick(NotesModel note) {
                Intent intent = new Intent(requireContext(), Notes.class);
                intent.putExtra("NOTE_ID", note.getId());
                intent.putExtra("IS_VAULT", note.isVault());
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                dismiss();
            }

            @Override
            public void onNoteLongClick(NotesModel note) {
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).showNoteOptions(note);
                }
            }
        });
        allNotesRecycler.setAdapter(notesAdapter);
        // for brick layout...
        allNotesRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Initial load
        observeNotes(notesAdapter, null);

        // Search Implementation
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                observeNotes(notesAdapter, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                observeNotes(notesAdapter, newText);
                return true;
            }
        });

        close.setOnClickListener(v->{
            dismiss();
        });

    }

    private void observeNotes(NotesAdapter adapter, String query) {
        // Remove previous observers to avoid multiple subscriptions
        notesViewModel.getAllNotes(email, sessionManager.getSortOrder()).removeObservers(getViewLifecycleOwner());
        if (query != null && !query.trim().isEmpty()) {
            notesViewModel.searchNotes(email, query).observe(getViewLifecycleOwner(), notes -> {
                updateUI(adapter, notes);
            });
        } else {
            notesViewModel.getAllNotes(email, sessionManager.getSortOrder()).observe(getViewLifecycleOwner(), notes -> {
                updateUI(adapter, notes);
            });
        }
    }

    private void updateUI(NotesAdapter adapter, java.util.List<com.kavya.stealthpad.data.Local.model.NoteWithAttachments> notes) {
        adapter.setNotes(notes);
        if (notes != null) {
            notecount.setText(notes.size() + " notes");
        } else {
            notecount.setText("0 notes");
        }
    }
}
