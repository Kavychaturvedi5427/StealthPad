package com.kavya.stealthpad.ui.notes;

import android.content.Intent;
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
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
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
            boolean isVault = getIntent().getBooleanExtra("IS_VAULT", false);

            if(isEditMode){
                current.setTitle(title);
                current.setContent(content);
                current.setCategory(category);
                current.setTimestamp(System.currentTimeMillis());
                viewModel.updateNote(current);
            }
            else{
                viewModel.validateNote(title, content, category, email, isVault);
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

        // Initialize Custom Bottom Navigation
        binding.stealthNavBar.setSelected(-1); // No selection on Create Note screen
        binding.stealthNavBar.setOnNavigationItemSelectedListener(itemId -> {
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else if (itemId == R.id.nav_add) {
                // Already on Create Note screen, maybe just clear if not edit mode?
                // For now, let's just do nothing or toast
            } else if (itemId == R.id.nav_vault) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("NAVIGATE_TO", R.id.nav_vault);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                // For other items, we might need to go back to Dashboard and then show the fragment
                // But since they are fragments in DashboardActivity, the simplest way is to go back to Dashboard
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("NAVIGATE_TO", itemId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void observeState(){
        viewModel.getNotesState().observe(this, state ->{
            if(state instanceof NotesState.LoadingState){
                progressBar.setVisibility(View.VISIBLE);
            }
            else if(state instanceof NotesState.SuccessState){
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note saved", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
                progressBar.setVisibility(View.GONE);
            }
            else if(state instanceof NotesState.ErrorState){
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note can't be added.", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
            }
            else if(state instanceof NotesState.DeleteSuccess){
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note deleted", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
                finish();
            } else if (state instanceof NotesState.DeleteFailure) {
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note can't be deleted", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
            }
        });
    }

}