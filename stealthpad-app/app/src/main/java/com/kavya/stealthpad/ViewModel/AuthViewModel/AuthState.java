package com.kavya.stealthpad.ViewModel.AuthViewModel;

import com.kavya.stealthpad.data.DataModel.AuthResponseDto;

public abstract class AuthState {

    public static class Idle extends AuthState{}
    public static class LoggedIn extends AuthState {}
    public static class LoggedOut extends AuthState{}
    public static class Loading extends AuthState {}
    public static class Success extends AuthState {
        private AuthResponseDto authResponseDto;

        public Success(AuthResponseDto response) { this.authResponseDto = response; }

        public AuthResponseDto getAuthResponseDto(){ return authResponseDto; }
    }
    public static class ForgotPassSuccess extends AuthState {
        private final String message;
        public ForgotPassSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class ResetPassSuccess extends AuthState {
        private final String message;
        public ResetPassSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class DeleteAccountSuccess extends AuthState {
        private final String message;
        public DeleteAccountSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class Error extends AuthState {
        private final String error;
        public Error(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
