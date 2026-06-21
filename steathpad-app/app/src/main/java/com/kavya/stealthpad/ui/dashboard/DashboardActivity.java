package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.databinding.DashboardBinding;
import com.kavya.stealthpad.ui.Auth.AuthDialogFragment;
import com.kavya.stealthpad.ui.notes.Notes;
import com.kavya.stealthpad.ui.notes.NotesAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private FloatingActionButton add;
    private NotesViewModel viewModel;
    private AuthViewModel authViewModel;
    private NotesAdapter adapterRecentNotes, adapterAllNotes;
    private RecyclerView recyclerViewRecent, recyclerViewAllNotes;
    private ShapeableImageView authbtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DashboardBinding binding = DashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        add = binding.btnAddNewNote;
        recyclerViewRecent = binding.recyclerViewNotes;
        authbtn = binding.auth;

        // creating adapters for recycler view....
        adapterRecentNotes = new NotesAdapter(R.layout.item_note_folder);
//        adapterAllNotes = new NotesAdapter(R.layout.item_note_preview);

        // setting adapters to the recycler view..
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecent.setAdapter(adapterRecentNotes);

//        recyclerViewAllNotes.setLayoutManager(new LinearLayoutManager(this));
//        recyclerViewAllNotes.setAdapter(adapterAllNotes);

        add.setOnClickListener(v -> {
            startActivity(new Intent(this, Notes.class));
        });

        // fetching the recent notes for highlights...
        viewModel.getRecentNotes().observe(this, notes -> {
            adapterRecentNotes.setNotes(notes);
            adapterRecentNotes.notifyDataSetChanged();
        });

        /*
        viewModel.getAllNotes().observe(this, notes->{
            adapterAllNotes.setNotes(notes);
            adapterAllNotes.notifyDataSetChanged();
        });
        */

        /**
         * ------------------------------------------------ Auth Section -----------------------------------------
         **/
        authbtn.setOnClickListener(v->{
            AuthDialogFragment authDialogFragment = new AuthDialogFragment();
            authDialogFragment.show(getSupportFragmentManager(), "Auth_dilaog");
        });

        // Transparent status bar for modern look
        getWindow().setStatusBarColor(getResources().getColor(R.color.dash_bg, getTheme()));
    }
}
