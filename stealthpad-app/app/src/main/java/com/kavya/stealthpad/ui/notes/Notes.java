package com.kavya.stealthpad.ui.notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesState;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.data.Local.model.NoteAttachment;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.databinding.ActivityCreateNoteBinding;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.DateTimeUtils;
import com.kavya.stealthpad.synchronization.SyncScheduler;
import com.kavya.stealthpad.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint  // tells hilt that this class will participate in the DI
public class Notes extends AppCompatActivity {
    private ActivityCreateNoteBinding binding;
    private EditText notestitle, notesContent;
    private TextView datetime;
    private NotesViewModel viewModel;
    private MaterialCardView back, delete, addImage, aiNote;
    private MaterialButton save;
    private NotesModel current;
    private boolean isEditMode = false;
    private boolean isSaved = false;
    private CircularProgressIndicator progressBar;

    private AttachmentAdapter attachmentAdapter;
    private List<NoteAttachment> pendingAttachments = new ArrayList<>();
    private List<NoteAttachment> existingAttachments = new ArrayList<>();

    @Inject
    AttachmentStorageManager storageManager;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (!uris.isEmpty()) {
                    int noteId = getIntent().getIntExtra("NOTE_ID", -1);
                    for (Uri uri : uris) {
                        try {
                            File file = storageManager.importImage(uri);
                            NoteAttachment attachment = new NoteAttachment(
                                    noteId != -1 ? noteId : 0,
                                    file.getName(),
                                    file.getAbsolutePath(),
                                    storageManager.getMimeType(uri),
                                    storageManager.getFileSize(uri)
                            );
                            
                            if (isEditMode && noteId != -1) {
                                viewModel.addAttachment(attachment);
                            } else {
                                pendingAttachments.add(attachment);
                                updateAttachmentVisibility();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to import image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!isEditMode) {
                        attachmentAdapter.setAttachments(pendingAttachments);
                    }
                }
            });

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
        addImage = binding.btnAddImage;
        aiNote = binding.btnAiNote;

        setupAttachments();

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
                    
                    loadExistingAttachments(noteId);
                }
            });
        }

        observeState();

        addImage.setOnClickListener(v -> {
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        aiNote.setOnClickListener(v -> {
            String content = notesContent.getText().toString().trim();
            com.kavya.stealthpad.ui.dashboard.StealthAIFragment fragment = 
                com.kavya.stealthpad.ui.dashboard.StealthAIFragment.newInstance(content);
            fragment.setOnResultAppliedListener(result -> {
                String existingContent = notesContent.getText().toString();
                if (existingContent.isEmpty()) {
                    notesContent.setText(result);
                } else {
                    notesContent.setText(existingContent + "\n\n" + result);
                }
            });
            fragment.show(getSupportFragmentManager(), "STEALTH_AI");
        });

        save.setOnClickListener(v->{
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            String title = notestitle.getText().toString();
            String content = notesContent.getText().toString();
            int selectedChipID = binding.categoryChipGroup.getCheckedChipId();

            SessionManager sessionManager = new SessionManager(this);
            // default category from settings...
            String category = sessionManager.getDefaultCategory();

            if(selectedChipID != View.NO_ID){
                Chip selectedchip = findViewById(selectedChipID);
                category = selectedchip.getText().toString();

            }
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
                viewModel.validateNote(title, content, category, email, isVault, pendingAttachments);
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

    private void setupAttachments() {
        attachmentAdapter = new AttachmentAdapter();
        binding.recyclerAttachments.setAdapter(attachmentAdapter);
        attachmentAdapter.setListener(new AttachmentAdapter.AttachmentListener() {
            @Override
            public void onRemoveAttachment(NoteAttachment attachment) {
                if (isEditMode) {
                    viewModel.deleteAttachment(attachment);
                } else {
                    pendingAttachments.remove(attachment);
                    attachmentAdapter.setAttachments(pendingAttachments);
                    storageManager.deleteAttachment(attachment.getLocalPath());
                    updateAttachmentVisibility();
                }
            }

            @Override
            public void onAttachmentClick(NoteAttachment attachment) {
                if (attachment.getLocalPath() != null) {
                    Intent intent = new Intent(Notes.this, ImageViewerActivity.class);
                    intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, attachment.getLocalPath());
                    startActivity(intent);
                }
            }
        });
    }

    private void loadExistingAttachments(int noteId) {
        viewModel.getAttachmentsForNote(noteId).observe(this, attachments -> {
            if (attachments != null) {
                existingAttachments = attachments;
                attachmentAdapter.setAttachments(existingAttachments);
                updateAttachmentVisibility();
            }
        });
    }

    private void updateAttachmentVisibility() {
        boolean hasAttachments = isEditMode ? !existingAttachments.isEmpty() : !pendingAttachments.isEmpty();
        binding.recyclerAttachments.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isEditMode && !isSaved && !pendingAttachments.isEmpty() && isFinishing()) {
            for (NoteAttachment attachment : pendingAttachments) {
                storageManager.deleteAttachment(attachment.getLocalPath());
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void observeState(){
        viewModel.getNotesState().observe(this, state ->{
            if(state instanceof NotesState.LoadingState){
                setSaveButtonLoading(true);
                progressBar.setVisibility(View.VISIBLE);
            }
            else if(state instanceof NotesState.SuccessState){
                isSaved = true;
                setSaveButtonLoading(false);
                setSaveButtonSaved();
                
                // Trigger immediate sync
                SessionManager sessionManager = new SessionManager(this);
                String email = sessionManager.getEmail();
                if (email != null && !email.isEmpty()) {
                    SyncScheduler.syncNow(getApplicationContext(), email);
                }

                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note saved", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
                progressBar.setVisibility(View.GONE);
            }
            else if(state instanceof NotesState.ErrorState){
                setSaveButtonLoading(false);
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Note can't be added.", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.stealthNavBar);
                snackbar.show();
            }
            else if(state instanceof NotesState.DeleteSuccess){
                progressBar.setVisibility(View.GONE);

                // Trigger immediate sync
                SessionManager sessionManager = new SessionManager(this);
                String email = sessionManager.getEmail();
                if (email != null && !email.isEmpty()) {
                    SyncScheduler.syncNow(getApplicationContext(), email);
                }

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

    private void setSaveButtonLoading(boolean isLoading) {
        if (isLoading) {
            save.setText("Saving...");
            CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this, null);
            spec.indicatorSize = (int) (18 * getResources().getDisplayMetrics().density);
            spec.trackThickness = (int) (2 * getResources().getDisplayMetrics().density);
            spec.indicatorColors = new int[]{ContextCompat.getColor(this, R.color.white)};
            IndeterminateDrawable<CircularProgressIndicatorSpec> progressDrawable = 
                IndeterminateDrawable.createCircularDrawable(this, spec);
            save.setIcon(progressDrawable);
        } else {
            save.setText("Save Note");
            save.setIconResource(R.drawable.ic_check);
        }
    }

    private void setSaveButtonSaved() {
        save.setText("Saved");
        save.setIconResource(R.drawable.ic_check);
        save.postDelayed(() -> {
            save.setText("Save Note");
            save.setIconResource(R.drawable.ic_check);
        }, 2000);
    }

}