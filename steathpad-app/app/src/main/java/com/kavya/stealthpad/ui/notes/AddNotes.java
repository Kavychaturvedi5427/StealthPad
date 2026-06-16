package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.kavya.stealthpad.ViewModel.NotesViewModel.AddNotesState;
import com.kavya.stealthpad.ViewModel.NotesViewModel.AddNotesViewModel;
import com.kavya.stealthpad.databinding.ActivityCreateNoteBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint  // tells hilt that this class will participate in the DI
public class AddNotes extends AppCompatActivity {
    private ActivityCreateNoteBinding binding;
    private EditText notestitle, notesContent;
    private TextView datetime;
    private AddNotesViewModel viewModel;
    private MaterialCardView save, back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(AddNotesViewModel.class);
        notestitle = binding.inputNoteTitle;
        notesContent = binding.inputNoteContent;
        datetime = binding.textDateTime;
        save = binding.btnSaveNote;
        back = binding.btnBack;

        observeState();

        save.setOnClickListener(v->{
            String title = notestitle.getText().toString();
            String content = notesContent.getText().toString();
            viewModel.validateNote(title, content);
        });

        back.setOnClickListener(v->{
            finish();
        });

    }

    private void observeState(){
        viewModel.getNotesState().observe(this, state ->{
            if(state instanceof AddNotesState.LoadingState){
                binding.progressBar.setVisibility(View.VISIBLE);
            }
            else if(state instanceof AddNotesState.SuccessState){
                binding.progressBar.setVisibility(View.GONE);
            }
            else if(state instanceof AddNotesState.ErrorState){
                binding.progressBar.setVisibility(View.GONE);
                Snackbar.make(binding.getRoot(), "Note can't be added.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

}