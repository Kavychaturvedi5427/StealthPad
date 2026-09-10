package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public class CategoryNotesBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CATEGORY = "category_name";
    private String categoryName;
    private NotesViewModel notesViewModel;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private TextView titleView, countView;
    private LinearLayout emptyState;
    private MaterialButton closeBtn;

    public static CategoryNotesBottomSheet newInstance(String category) {
        CategoryNotesBottomSheet fragment = new CategoryNotesBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_category_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);
        sessionManager = new SessionManager(requireContext());

        titleView = view.findViewById(R.id.text_category_title);
        countView = view.findViewById(R.id.text_category_note_count);
        recyclerView = view.findViewById(R.id.recycler_category_notes);
        emptyState = view.findViewById(R.id.empty_category_state);
        closeBtn = view.findViewById(R.id.btn_close_category);

        titleView.setText(categoryName);
        
        NotesAdapter adapter = new NotesAdapter(R.layout.item_note_staggered);
        adapter.setNotesListener(new NotesAdapter.NotesListener() {
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
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        String email = sessionManager.getEmail();
        notesViewModel.getNotesByCategory(email, categoryName).observe(getViewLifecycleOwner(), notes -> {
            if (notes == null || notes.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                countView.setText("0 notes");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                adapter.setNotes(notes);
                countView.setText(notes.size() + " notes");
            }
        });

        closeBtn.setOnClickListener(v -> dismiss());
    }
}
