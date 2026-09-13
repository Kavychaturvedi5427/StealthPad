package com.kavya.stealthpad.ViewModel.AiViewModel;

public abstract class AiState<T> {
    public static class Idle<T> extends AiState<T> {}
    public static class Loading<T> extends AiState<T> {
        private final String message;
        public Loading(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class Success<T> extends AiState<T> {
        private final T data;
        public Success(T data) { this.data = data; }
        public T getData() { return data; }
    }
    public static class Error<T> extends AiState<T> {
        private final String error;
        public Error(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
