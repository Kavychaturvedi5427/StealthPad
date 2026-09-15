package com.kavya.stealthpad.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    public void sendPasswordResetOtp(String email, String otp) {

        Context context = new Context();
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("forgot-password", context);

        try {

            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("StealthPad <noreply@stealthpad.kavyadev.in>")
                    .to(email)
                    .subject("StealthPad Password Reset")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);

        } catch (ResendException e) {

            throw new RuntimeException(
                    "Unable to send password reset email", e);
        }
    }
}