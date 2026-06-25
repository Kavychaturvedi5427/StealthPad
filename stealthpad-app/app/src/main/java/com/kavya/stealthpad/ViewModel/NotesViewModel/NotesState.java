package com.kavya.stealthpad.ViewModel.NotesViewModel;


public class NotesState {
    public static class SuccessState extends NotesState {}
    public static class LoadingState extends NotesState {};
    public static class ErrorState extends NotesState {
        private String error;
        public ErrorState(String er){
            this.error = er;
        }

        public String getError() {
            return error;
        }
    }

    public static class DeleteSuccess extends NotesState{}
    public static class DeleteFailure extends NotesState{
        private String messsge;
        public DeleteFailure(String mess){
            this.messsge = mess;
        }
        public String getMesssge(){
            return messsge;
        }

    }

}
