package com.kavya.stealthpad.ViewModel.NotesViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kavya.stealthpad.data.Local.model.NoteAttachment;
import com.kavya.stealthpad.data.Local.model.NoteWithAttachments;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.data.repository.Notes.NotesRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotesViewModel extends ViewModel {
    private MutableLiveData<NotesState> notesState = new MutableLiveData<>();

    public LiveData<NotesState> getNotesState() {
        return notesState;
    }
    private NotesRepository repo;

    @Inject
    public NotesViewModel(NotesRepository repository) {
        this.repo = repository;
    }

    public void validateNote(String title, String content, String category, String email, boolean isVault, List<NoteAttachment> attachments) {
        notesState.setValue(new NotesState.LoadingState());
        if (title == null || title.trim().isEmpty()) {
            notesState.setValue(new NotesState.ErrorState("Title can't be empty."));
            return;
        }
        NotesModel model = new NotesModel(title, content, System.currentTimeMillis(), category, email);
        model.setVault(isVault);
        repo.saveNote(model, attachments, new NotesRepository.SavenotesCallback() {
            @Override
            public void onSuccess() {
                notesState.postValue(new NotesState.SuccessState());
            }

            @Override
            public void onError(Exception e) {
                notesState.postValue(new NotesState.ErrorState(e.getMessage()));
            }
        });
    }

    public LiveData<List<NoteAttachment>> getAttachmentsForNote(int noteId) {
        return repo.getAttachmentsForNote(noteId);
    }

    public void addAttachment(NoteAttachment attachment) {
        repo.addAttachment(attachment);
    }

    public void deleteAttachment(NoteAttachment attachment) {
        repo.deleteAttachment(attachment);
    }

    public LiveData<List<NoteWithAttachments>> getAllNotes(String email, String sortOrder){
        return repo.getAllNotes(email, sortOrder);
    }

    public LiveData<List<NoteWithAttachments>> getNotesByCategory(String email, String category, String sortOrder){
        return repo.getNotesByCategory(email, category, sortOrder);
    }

    public LiveData<List<NoteWithAttachments>> getRecentNotes(String email){
        return repo.getRecentNotes(email);
    }

    public LiveData<List<NoteWithAttachments>> getVaultNotes(String email){
        return repo.getVaultNotes(email);
    }

    public LiveData<List<NoteWithAttachments>> searchNotes(String email, String query) {
        return repo.searchNotes(email, query);
    }

    public LiveData<NotesModel> getNoteById(int id){
        return repo.getNoteById(id);
    }

    public void updateNote(NotesModel current) {
        notesState.setValue(new NotesState.LoadingState());

        repo.updateNote(current, new NotesRepository.SavenotesCallback() {
            @Override
            public void onSuccess() {
                notesState.postValue(new NotesState.SuccessState());
            }

            @Override
            public void onError(Exception e) {
                notesState.postValue(new NotesState.ErrorState(e.getMessage()));
            }
        });
    }

    public void deleteById(int id){
        notesState.setValue(new NotesState.LoadingState());
        repo.deleteById(id, new NotesRepository.SavenotesCallback() {
            @Override
            public void onSuccess() {
                notesState.postValue(new NotesState.DeleteSuccess());
            }

            @Override
            public void onError(Exception e) {
                notesState.postValue(new NotesState.DeleteFailure(e.getMessage()));
            }
        });
    }

}

