package com.kavya.stealthpad.ViewModel.NotesViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.data.repository.Notes.NotesRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotesViewModel extends ViewModel {
    private MutableLiveData<AddNotesState> notesState = new MutableLiveData<>();

    public LiveData<AddNotesState> getNotesState() {
        return notesState;
    }
    private NotesRepository repo;

    @Inject
    public NotesViewModel(NotesRepository repository) {
        this.repo = repository;
    }

    public void validateNote(String title, String content, String category) {
        notesState.setValue(new AddNotesState.LoadingState());
        if (title == null || title.trim().isEmpty()) {
            notesState.setValue(new AddNotesState.ErrorState("Title can't be empty."));
            return;
        }
        repo.saveNote(title, content, category, new NotesRepository.SavenotesCallback() {
            @Override
            public void onSuccess() {
                notesState.postValue(new AddNotesState.SuccessState());
            }

            @Override
            public void onError(Exception e) {
                notesState.postValue(new AddNotesState.ErrorState(e.getMessage()));
            }
        });
    }

    public LiveData<List<NotesModel>> getAllNotes(){
        return repo.getAllNotes();
    }

    public LiveData<List<NotesModel>> getRecentNotes(){
        return repo.getRecentNotes();
    }

}

