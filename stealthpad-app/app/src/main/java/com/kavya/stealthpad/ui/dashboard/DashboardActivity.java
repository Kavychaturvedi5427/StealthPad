package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.ViewModel.NotesViewModel.NotesViewModel;
import com.kavya.stealthpad.databinding.DashboardBinding;
import com.kavya.stealthpad.ui.Auth.AuthDialogFragment;
import com.kavya.stealthpad.ui.Auth.ProfileDialog;
import com.kavya.stealthpad.ui.notes.AllNotes;
import com.kavya.stealthpad.ui.notes.Notes;
import com.kavya.stealthpad.ui.notes.NotesAdapter;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private FloatingActionButton add;
    private NotesViewModel notesViewModel;
    private AuthViewModel authViewModel;
    private NotesAdapter adapterRecentNotes;
    private RecyclerView recyclerViewRecent;
    private ShapeableImageView authimg;
    private FrameLayout authbtn;
    private SessionManager sessionManager;
    private TextView ViewAll, greetingText, empty_txt;
    private LottieAnimationView emptyStateAnimation, authbtnLottie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DashboardBinding binding = DashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        sessionManager = new SessionManager(this);

        add = binding.btnAddNewNote;
        recyclerViewRecent = binding.recyclerViewNotes;
        authbtn = binding.authbtn;
        ViewAll = binding.btnViewAll;
        greetingText = binding.greetingText;
        empty_txt = binding.emptyTxt;
        emptyStateAnimation = binding.empty;
        authbtnLottie = binding.profileAnimation;
        authimg = binding.authImg;

        // setting the name of the logged in user on the dashboard...
        String fullName = sessionManager.getName(); // this will give the array of the string name and from that we need to get the first name that is at 0th index...
        if (fullName != null) {
            String firstname = fullName.split(" ")[0];
            greetingText.setText(getGreeting() + firstname);
        }

        // creating adapters for recycler view....
        adapterRecentNotes = new NotesAdapter(R.layout.item_note_folder);
//        adapterAllNotes = new NotesAdapter(R.layout.item_note_preview);

        // setting adapters to the recycler view...
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecent.setAdapter(adapterRecentNotes);

        add.setOnClickListener(v -> {
            if(!sessionManager.isLoggedIn()){
                Toast.makeText(this, "Please login to create note", Toast.LENGTH_SHORT).show();
            }
            else{
                startActivity(new Intent(this, Notes.class));
            }
            
        });


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
            if(!sessionManager.isLoggedIn()){
                Toast.makeText(this,"Please login to access notes", Toast.LENGTH_SHORT).show();
            }
            else{
                AllNotes allnotes = new AllNotes();
                allnotes.show(getSupportFragmentManager(), "ALL_NOTES");
            }
        });

        // making sure that before loading notes the user is logged in...
        observeState();
        authViewModel.checkAuth(sessionManager);

        // Transparent status bar for modern look
        getWindow().setStatusBarColor(getResources().getColor(R.color.dash_bg, getTheme()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        authViewModel.checkAuth(sessionManager);
    }

    private void observeState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.LoggedIn) {
                authimg.setVisibility(View.GONE);
                authbtnLottie.setVisibility(View.VISIBLE);
                updateGreeting();
                loadnotes();
            } else if (state instanceof AuthState.LoggedOut) {
                greetingText.setText(
                        getGreeting() + "User"
                );
                adapterRecentNotes.setNotes(new ArrayList<>());
                adapterRecentNotes.notifyDataSetChanged();
                authimg.setImageResource(R.mipmap.ic_launcher_foreground);
                emptyStateAnimation.setVisibility(View.VISIBLE);
                emptyStateAnimation.setAnimation(R.raw.login);
                empty_txt.setVisibility(View.VISIBLE);
                empty_txt.setText("Please login to access notes");
            }
        });
    }

    private void loadnotes() {
        // fetching the email based on the user logged in...
        String email = sessionManager.getEmail();

        // this will setup the observer for the notes, whenever any note is added or deleted it will automatically update..
        notesViewModel.getRecentNotes(email).observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {

                empty_txt.setVisibility(View.VISIBLE);
                emptyStateAnimation.setVisibility(View.VISIBLE);

                recyclerViewRecent.setVisibility(View.GONE);

            } else {
                empty_txt.setVisibility(View.GONE);
                emptyStateAnimation.setVisibility(View.GONE);

                recyclerViewRecent.setVisibility(View.VISIBLE);

                adapterRecentNotes.setNotes(notes);
                adapterRecentNotes.notifyDataSetChanged();
            }
        });
    }

    private void showLoginDialog() {
        AuthDialogFragment authDialogFragment = new AuthDialogFragment();
        authDialogFragment.show(getSupportFragmentManager(), "Auth_Dia");
    }

    private void showProfileDialog() {
        ProfileDialog profileDialog = new ProfileDialog();
        profileDialog.show(getSupportFragmentManager(), "Profile_Dia");
    }


    // method for getting the greeting text based on the time of the day...
    private String getGreeting() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good Morning, ";
        } else if (hour < 17) {
            return "Good Afternoon, ";
        } else {
            return "Good Evening, ";
        }
    }

    private void updateGreeting() {

        String fullName = sessionManager.getName();

        if (fullName != null && !fullName.isEmpty()) {

            String firstname = fullName.split(" ")[0];

            greetingText.setText(
                    getGreeting() + firstname
            );

        } else {
            greetingText.setText(getGreeting() + "User");
        }
    }

}
