package com.kavya.stealthpad.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {

    public interface BiometricCallback {
        void onAuthenticationSuccess();
        default void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {}
        default void onAuthenticationFailed() {}
    }

    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context.getApplicationContext());
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void showBiometricPrompt(
            Fragment fragment,
            String title,
            String subtitle,
            String negativeButtonText,
            BiometricCallback callback
    ) {
        Executor executor = ContextCompat.getMainExecutor(fragment.requireContext().getApplicationContext());
        BiometricPrompt biometricPrompt = createBiometricPrompt(fragment, executor, callback);
        BiometricPrompt.PromptInfo promptInfo = createPromptInfo(title, subtitle, negativeButtonText);
        biometricPrompt.authenticate(promptInfo);
    }

    public static void showBiometricPrompt(
            FragmentActivity activity,
            String title,
            String subtitle,
            String negativeButtonText,
            BiometricCallback callback
    ) {
        Executor executor = ContextCompat.getMainExecutor(activity.getApplicationContext());
        BiometricPrompt biometricPrompt = createBiometricPrompt(activity, executor, callback);
        BiometricPrompt.PromptInfo promptInfo = createPromptInfo(title, subtitle, negativeButtonText);
        biometricPrompt.authenticate(promptInfo);
    }

    private static BiometricPrompt createBiometricPrompt(
            Object host,
            Executor executor,
            BiometricCallback callback
    ) {
        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (callback != null) callback.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (callback != null) callback.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) callback.onAuthenticationFailed();
            }
        };

        if (host instanceof Fragment) {
            return new BiometricPrompt((Fragment) host, executor, authCallback);
        } else {
            return new BiometricPrompt((FragmentActivity) host, executor, authCallback);
        }
    }

    private static BiometricPrompt.PromptInfo createPromptInfo(String title, String subtitle, String negativeButtonText) {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build();
    }
}
