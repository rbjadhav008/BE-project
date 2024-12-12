package com.epam.user_service.service;

public interface PasswordService {
    void sendOtpMessage(String email, String text);
    String generateOtp();
    String creatingOtp(String email);
    String updatePassword(String email, String password, String receivedOtp);
}