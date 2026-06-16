package com.kavya.stealthpad.ViewModel.NotesViewModel;

public class AddNotesState {
    public static class SuccessState extends AddNotesState{}
    public static class LoadingState extends AddNotesState{};
    public static class ErrorState extends AddNotesState{
        private String error;
        public ErrorState(String er){
            this.error = er;
        }

        public String getError() {
            return error;
        }
    }
}
