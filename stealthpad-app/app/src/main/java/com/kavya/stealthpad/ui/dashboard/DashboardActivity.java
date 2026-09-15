package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.imageview.ShapeableImageView;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.databinding.DashboardBinding;
import com.kavya.stealthpad.synchronization.SyncScheduler;
import com.kavya.stealthpad.ui.Auth.AuthDialogFragment;
import com.kavya.stealthpad.ui.Auth.ProfileDialog;
import com.kavya.stealthpad.ui.notes.AllNotes;
import com.kavya.stealthpad.ui.notes.Notes;
import com.kavya.stealthpad.ui.notes.NotesAdapter;
import com.kavya.stealthpad.ui.vault.VaultAccessBottomSheet;
import com.kavya.stealthpad.ui.vault.VaultFragment;
import com.kavya.stealthpad.ui.vault.VaultSetupFragment;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private DashboardBinding binding;
    private NotesViewModel notesViewModel;
    private AuthViewModel authViewModel;
    private NotesAdapter adapterRecentNotes;
    private RecyclerView recyclerViewRecent;
    private ShapeableImageView authimg;
    private FrameLayout authbtn;
    private SessionManager sessionManager;
    private TextView ViewAll, greetingText, empty_txt;
    private LottieAnimationView emptyStateAnimation, authbtnLottie;
    
    private boolean isVaultAuthenticated = false;
    private int currentNavId = R.id.nav_home;

    private boolean adLoaded = false;
    private androidx.lifecycle.LiveData<java.util.List<com.kavya.stealthpad.data.Local.model.NoteWithAttachments>> dashboardNotesLiveData;

    public void setVaultAuthenticated(boolean authenticated) {
        this.isVaultAuthenticated = authenticated;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        binding = DashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        sessionManager = new SessionManager(this);

        // Initialize AdMob
        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {
            // AdMob initialized successfully
        });

        /*
        * ================================================
        * SYNC ON APP REOPENING
        * ================================================
        */
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                String email = sessionManager.getEmail();
                if (email != null && !email.isEmpty()) {
                    SyncScheduler.syncNow(getApplicationContext(), email);
                    SyncScheduler.schedulerPeriodicSync(getApplicationContext(), email);
                }
            }
        }, 500);

        recyclerViewRecent = binding.recyclerViewNotes;
        authbtn = binding.authbtn;
        ViewAll = binding.btnViewAll;
        greetingText = binding.greetingText;
        empty_txt = binding.emptyTxt;
        emptyStateAnimation = binding.empty;
        authbtnLottie = binding.profileAnimation;
        authimg = binding.authImg;

        // setting the name of the logged in user on the dashboard...
        String fullName = sessionManager.getName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+");
            if (nameParts.length > 0) {
                String firstname = nameParts[0];
                greetingText.setText(getGreeting() + firstname);
            }
        }

        // creating adapters for recycler view....
        adapterRecentNotes = new NotesAdapter(R.layout.item_note_folder);
        adapterRecentNotes.setNotesListener(new NotesAdapter.NotesListener() {
            @Override
            public void onNoteClick(NotesModel note) {
                Intent intent = new Intent(DashboardActivity.this, Notes.class);
                intent.putExtra("NOTE_ID", note.getId());
                intent.putExtra("IS_VAULT", note.isVault());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onNoteLongClick(NotesModel note) {
                showNoteOptions(note);
            }
        });

        // setting adapters to the recycler view...
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecent.setAdapter(adapterRecentNotes);

        /*
         * ------------------------------------------------ Auth Section -----------------------------------------
         */
        authbtn.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showProfileDialog();
            } else {
                showLoginDialog();
            }
        });

        ViewAll.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please login to access notes", Toast.LENGTH_SHORT).show();
            } else {
                AllNotes allnotes = new AllNotes();
                allnotes.show(getSupportFragmentManager(), "ALL_NOTES");
            }
        });

        // Initialize Custom Bottom Navigation
        binding.stealthNavBar.setOnNavigationItemSelectedListener(this::navigateTo);

        // Handle navigation from other activities

        observeState();
        
        // Transparent status bar for modern look
        getWindow().setStatusBarColor(getResources().getColor(R.color.dash_bg, getTheme()));

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.mainFragmentContainer.getVisibility() == View.VISIBLE) {
                    navigateTo(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("NAVIGATE_TO")) {
            int itemId = intent.getIntExtra("NAVIGATE_TO", R.id.nav_home);
            navigateTo(itemId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        authViewModel.checkAuth(sessionManager);
    }

    private void observeState() {
        authViewModel.getAuthState().observe(this, state -> {
            ViewGroup.LayoutParams params = authbtn.getLayoutParams();
            if (state instanceof AuthState.LoggedIn) {
                // Normal size for logged in state
                params.width = (int) (48 * getResources().getDisplayMetrics().density);
                params.height = (int) (48 * getResources().getDisplayMetrics().density);
                authbtn.setLayoutParams(params);

                authimg.setVisibility(View.GONE);
                authbtnLottie.setVisibility(View.VISIBLE);

                binding.adContainer.setVisibility(View.VISIBLE);

                if (!adLoaded) {

                    AdRequest adRequest = new AdRequest.Builder().build();

                    binding.adView.setAdListener(new AdListener() {

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            Log.d("ADMOB", "Real ad loaded successfully");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            super.onAdFailedToLoad(adError);

                            Log.e(
                                    "ADMOB",
                                    "Ad failed: code=" + adError.getCode()
                                            + ", message=" + adError.getMessage()
                            );
                        }
                    });

                    binding.adView.loadAd(adRequest);
                    adLoaded = true;
                }

                updateGreeting();
                loadnotes();
            } else if (state instanceof AuthState.LoggedOut) {
                // Increased size for logged out logo
                params.width = (int) (64 * getResources().getDisplayMetrics().density);
                params.height = (int) (64 * getResources().getDisplayMetrics().density);
                authbtn.setLayoutParams(params);

                binding.adContainer.setVisibility(View.GONE);

                greetingText.setText(getGreeting() + "User");
                adapterRecentNotes.setNotes(new ArrayList<>());
                adapterRecentNotes.notifyDataSetChanged();
                authimg.setVisibility(View.VISIBLE);
                authimg.setImageResource(R.drawable.logo);
                authimg.setPadding(0, 0, 0, 0); // Remove padding to make logo occupy full space
                authbtnLottie.setVisibility(View.GONE);

                emptyStateAnimation.setVisibility(View.VISIBLE);
                emptyStateAnimation.setAnimation(R.raw.login);
                empty_txt.setVisibility(View.VISIBLE);
                empty_txt.setText("Please login to access notes");
            }
        });
    }

    private void loadnotes() {
        String email = sessionManager.getEmail();
        if (email == null || email.isEmpty()) return;
        
        if (dashboardNotesLiveData != null) {
            dashboardNotesLiveData.removeObservers(this);
        }

        String sortOrder = sessionManager.getSortOrder();
        adapterRecentNotes.setSortOrder(sortOrder);
        dashboardNotesLiveData = notesViewModel.getDashboardNotes(email, sortOrder);
        dashboardNotesLiveData.observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                empty_txt.setVisibility(View.VISIBLE);
                empty_txt.setText(R.string.no_notes_found);
                recyclerViewRecent.setVisibility(View.GONE);
                emptyStateAnimation.setVisibility(View.VISIBLE);
                emptyStateAnimation.setAnimation(R.raw.empty);
            } else {
                empty_txt.setVisibility(View.GONE);
                recyclerViewRecent.setVisibility(View.VISIBLE);
                adapterRecentNotes.setNotes(notes);
                adapterRecentNotes.notifyDataSetChanged();
                emptyStateAnimation.setVisibility(View.GONE);
            }
        });
    }

    /**
     * CENTRAL NAVIGATION METHOD
     */
    public void navigateTo(int itemId) {
        if (itemId == R.id.nav_home) {
            currentNavId = R.id.nav_home;
            
            // Auto-lock logic: Reset authentication if set to "Immediately"
            if (sessionManager.getAutoLockMinutes() == 0) {
                isVaultAuthenticated = false;
                sessionManager.setLastVaultUnlockTime(0);
            }
            
            binding.homeContent.setVisibility(View.VISIBLE);
            binding.mainFragmentContainer.setVisibility(View.GONE);
            binding.stealthNavBar.setSelected(R.id.nav_home);

        } else if (itemId == R.id.nav_ai) {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please login to use AI features", Toast.LENGTH_SHORT).show();
                binding.stealthNavBar.setSelected(currentNavId);
            } else {
                // Now showing AI as a Bottom Sheet
                StealthAIFragment.newInstance(null).show(getSupportFragmentManager(), "STEALTH_AI");
                // Revert selection or keep it? If it's a bottom sheet, maybe we don't want to change the "tab"
                binding.stealthNavBar.setSelected(currentNavId);
            }

        } else if (itemId == R.id.nav_vault) {
            handleVaultNavigation();

        } else if (itemId == R.id.nav_more) {
            // Reset vault if auto-lock is immediate
            if (sessionManager.getAutoLockMinutes() == 0) {
                isVaultAuthenticated = false;
                sessionManager.setLastVaultUnlockTime(0);
            }
            
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            // Revert navbar to previous valid section since we just opened an activity
            binding.stealthNavBar.setSelected(currentNavId);

        } else if (itemId == R.id.nav_add) {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please login to create note", Toast.LENGTH_SHORT).show();
                binding.stealthNavBar.setSelected(currentNavId);
            } else {
                Intent intent = new Intent(this, Notes.class);
                intent.putExtra("IS_VAULT", isVaultAuthenticated);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    }

    private void handleVaultNavigation() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to access vault", Toast.LENGTH_SHORT).show();
            binding.stealthNavBar.setSelected(currentNavId);
            return;
        }

        if (!sessionManager.isVaultSetup()) {
            currentNavId = R.id.nav_vault;
            showFragment(new VaultSetupFragment(), "VAULT_SETUP");
            binding.stealthNavBar.setSelected(R.id.nav_vault);
            return;
        }

        // Check Auto-Lock logic
        int autoLockMinutes = sessionManager.getAutoLockMinutes();
        if (autoLockMinutes > 0) {
            long lastUnlock = sessionManager.getLastVaultUnlockTime();
            long now = System.currentTimeMillis();
            if (now - lastUnlock < (long) autoLockMinutes * 60 * 1000) {
                isVaultAuthenticated = true;
            } else {
                isVaultAuthenticated = false;
            }
        }

        if (isVaultAuthenticated) {
            currentNavId = R.id.nav_vault;
            showFragment(new VaultFragment(), "VAULT");
            binding.stealthNavBar.setSelected(R.id.nav_vault);
        } else {
            showVaultAccessDialog(null);
        }
    }

    private void showVaultAccessDialog(@Nullable NotesModel pendingNote) {
        VaultAccessBottomSheet authDialog = new VaultAccessBottomSheet();
        authDialog.setVaultAuthListener(new VaultAccessBottomSheet.VaultAuthListener() {
            @Override
            public void onVaultAuthenticated() {
                isVaultAuthenticated = true;
                sessionManager.setLastVaultUnlockTime(System.currentTimeMillis());

                if (pendingNote != null) {
                    moveNoteToVault(pendingNote);
                } else {
                    currentNavId = R.id.nav_vault;
                    showFragment(new VaultFragment(), "VAULT");
                    binding.stealthNavBar.setSelected(R.id.nav_vault);
                }
            }

            @Override
            public void onVaultAuthCancelled() {
                if (pendingNote == null) {
                    binding.stealthNavBar.setSelected(currentNavId);
                }
            }

            @Override
            public void onVaultReset() {
                // If vault reset (Forgot PIN), refresh current view or navigate home
                isVaultAuthenticated = false;
                navigateTo(R.id.nav_home);
            }
        });
        authDialog.show(getSupportFragmentManager(), "VAULT_ACCESS");
    }

    private void showFragment(Fragment fragment, String tag) {
        binding.homeContent.setVisibility(View.GONE);
        binding.mainFragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, fragment, tag)
                .commit();
    }

    private void showLoginDialog() {
        AuthDialogFragment authDialogFragment = new AuthDialogFragment();
        authDialogFragment.show(getSupportFragmentManager(), "Auth_Dia");
    }

    private void showProfileDialog() {
        ProfileDialog profileDialog = new ProfileDialog();
        profileDialog.show(getSupportFragmentManager(), "Profile_Dia");
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning, ";
        else if (hour < 17) return "Good Afternoon, ";
        else return "Good Evening, ";
    }

    private void updateGreeting() {
        String fullName = sessionManager.getName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+");
            if (nameParts.length > 0) {
                String firstname = nameParts[0];
                greetingText.setText(getGreeting() + firstname);
            }
        } else {
            greetingText.setText(getGreeting() + "User");
        }
    }

    public void showNoteOptions(NotesModel note) {
        String vaultAction = note.isVault() ? "Remove from Vault" : "Move to Vault";
        String[] options = {vaultAction, "Edit", "Delete"};

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Note Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            handleVaultMove(note);
                            break;
                        case 1:
                            Intent intent = new Intent(this, Notes.class);
                            intent.putExtra("NOTE_ID", note.getId());
                            intent.putExtra("IS_VAULT", note.isVault());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            break;
                        case 2:
                            confirmDelete(note);
                            break;
                    }
                })
                .show();
    }

    private void handleVaultMove(NotesModel note) {
        if (note.isVault()) {
            // Remove from vault
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Remove from Vault?")
                    .setMessage("This note will become visible in your normal notes and categories.")
                    .setPositiveButton("Remove from Vault", (dialog, which) -> {
                        note.setVault(false);
                        notesViewModel.updateNote(note);
                        triggerSync();
                        Toast.makeText(this, "Moved to normal notes", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            // Move to vault
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Move to Vault?")
                    .setMessage("This note will be removed from your public notes and stored in your Private Vault.")
                    .setPositiveButton("Move to Vault", (dialog, which) -> {
                        if (isVaultAuthenticated) {
                            moveNoteToVault(note);
                        } else {
                            showVaultAccessDialog(note);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void moveNoteToVault(NotesModel note) {
        note.setVault(true);
        notesViewModel.updateNote(note);
        triggerSync();
        Toast.makeText(this, "Moved to Private Vault", Toast.LENGTH_SHORT).show();
    }

    private void triggerSync() {
        if (sessionManager.isLoggedIn()) {
            String email = sessionManager.getEmail();
            if (email != null && !email.isEmpty()) {
                SyncScheduler.syncNow(getApplicationContext(), email);
            }
        }
    }

    private void confirmDelete(NotesModel note) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    notesViewModel.deleteById(note.getId());
                    triggerSync();
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (binding != null && binding.adView != null) {
            binding.adView.destroy();
        }
        super.onDestroy();
    }
}
