package com.kavya.stealthpad.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendPasswordResetOtp(String email, String otp) {

        Context context = new Context();
        context.setVariable("otp", otp);

        String htmlContent =
                templateEngine.process("forgot-password", context);

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("onboarding@resend.dev");
            helper.setTo(email);
            helper.setSubject("StealthPad Password Reset");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to send password reset email", e
            );
        }
    }
}