package com.kavya.stealthpad.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.ui.notes.Notes;
import com.kavya.stealthpad.ui.notes.NotesAdapter;
import com.kavya.stealthpad.utils.SessionManager;

import android.content.Intent;
import android.widget.Toast;

public class VaultFragment extends Fragment {

    private NotesViewModel notesViewModel;
    private NotesAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vault, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);
        sessionManager = new SessionManager(requireContext());

        recyclerView = view.findViewById(R.id.recycler_vault_notes);
        emptyState = view.findViewById(R.id.empty_vault_state);

        adapter = new NotesAdapter(R.layout.item_note_staggered);
        adapter.setNotesListener(new NotesAdapter.NotesListener() {
            @Override
            public void onNoteClick(NotesModel note) {
                Intent intent = new Intent(requireContext(), Notes.class);
                intent.putExtra("NOTE_ID", note.getId());
                intent.putExtra("IS_VAULT", true);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onNoteLongClick(NotesModel note) {
                if (getActivity() instanceof DashboardActivity) {
                    // Reusing the logic from DashboardActivity
                    // (I'll need to make showNoteOptions public in DashboardActivity)
                    ((DashboardActivity) getActivity()).showNoteOptions(note);
                }
            }
        });
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        loadVaultNotes();
    }

    private void loadVaultNotes() {
        String email = sessionManager.getEmail();
        if (email == null) return;

        notesViewModel.getVaultNotes(email).observe(getViewLifecycleOwner(), notes -> {
            if (notes == null || notes.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                adapter.setNotes(notes);
            }
        });
    }
}
