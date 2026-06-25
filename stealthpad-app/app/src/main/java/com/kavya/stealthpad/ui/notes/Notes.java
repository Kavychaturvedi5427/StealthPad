package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesState;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.databinding.ActivityCreateNoteBinding;
import com.kavya.stealthpad.utils.DateTimeUtils;
import com.kavya.stealthpad.utils.SessionManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint  // tells hilt that this class will participate in the DI
public class Notes extends AppCompatActivity {
    private ActivityCreateNoteBinding binding;
    private EditText notestitle, notesContent;
    private TextView datetime;
    private NotesViewModel viewModel;
    private MaterialCardView save, back, delete;
    private NotesModel current;
    private boolean isEditMode = false;
    private CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        notestitle = binding.inputNoteTitle;
        notesContent = binding.inputNoteContent;
        datetime = binding.textDateTime;
        save = binding.btnSaveNote;
        back = binding.btnBack;
        progressBar = binding.loadingIndicator;
        delete = binding.btnDeleteNote;

        // note edit part... fetch the note id otherwise its -1...
        int noteId = getIntent().getIntExtra("NOTE_ID", -1);

        if(noteId != -1){
            delete.setVisibility(View.VISIBLE);
            isEditMode = true;
            viewModel.getNoteById(noteId).observe(this, note->{
                if(note != null){
                    current = note;
                    // updating the ui based on the note that is fetched...
                    notestitle.setText(current.getTitle());
                    notesContent.setText(current.getContent());
                    datetime.setText(DateTimeUtils.formatTimestamp(current.getTimestamp()));
                }
            });
        }

        observeState();

        save.setOnClickListener(v->{
            String title = notestitle.getText().toString();
            String content = notesContent.getText().toString();
            int selectedChipID = binding.categoryChipGroup.getCheckedChipId();

            // default category...
            String category = "Personal";

            if(selectedChipID != View.NO_ID){
                Chip selectedchip = findViewById(selectedChipID);
                category = selectedchip.getText().toString();

            }
            SessionManager sessionManager = new SessionManager(this);
            String email = sessionManager.getEmail();

            if(isEditMode){
                current.setTitle(title);
                current.setContent(content);
                current.setCategory(category);
                current.setTimestamp(System.currentTimeMillis());
                viewModel.updateNote(current);
            }
            else{
                viewModel.validateNote(title, content, category, email);
            }
        });

        // if user wants to delete the note....
        delete.setOnClickListener(del ->{
            if(current != null)  {
                viewModel.deleteById(current.getId());
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(v->{
            finish();
        });

    }

    private void observeState(){
        viewModel.getNotesState().observe(this, state ->{
            if(state instanceof NotesState.LoadingState){
                progressBar.setVisibility(View.VISIBLE);
            }
            else if(state instanceof NotesState.SuccessState){                Snackbar.make(binding.getRoot(), "Note saved", Snackbar.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note saved", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(findViewById(R.id.navbar));
                snackbar.show();
                progressBar.setVisibility(View.GONE);
            }
            else if(state instanceof NotesState.ErrorState){
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note can't be added.", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(findViewById(R.id.navbar));
                snackbar.show();
            }
            else if(state instanceof NotesState.DeleteSuccess){
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note deleted", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(findViewById(R.id.navbar));
                snackbar.show();
                finish();
            } else if (state instanceof NotesState.DeleteFailure) {
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note can't be deleted", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(findViewById(R.id.navbar));
                snackbar.show();
            }
        });
    }

}