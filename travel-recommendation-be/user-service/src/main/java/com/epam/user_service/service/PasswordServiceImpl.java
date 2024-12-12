package com.epam.user_service.service;
import com.epam.user_service.exception.OtpMismatchException;
import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.UserRepository;
import com.epam.user_service.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JavaMailSender emailSender;
     static final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();


    @Autowired
    public PasswordServiceImpl(UserRepository userRepository, BCryptPasswordEncoder encoder, JavaMailSender emailSender) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.emailSender = emailSender;
    }

    public void sendOtpMessage(String email, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setText(text);
            emailSender.send(message);
            System.out.println("OTP Email sent successfully to {}");
        } catch (Exception e) {
            System.out.println("Failed to send OTP email to " );

        }
    }

    public String generateOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(899999);
        return String.valueOf(otp);
    }

    public String creatingOtp(String email){
        try {

            Optional<User> user = userRepository.findByUsername(email);

            if (user.isEmpty()) {
                throw new UserNotFound("User not found");
            }
            String createdOtp = generateOtp();
            otpStore.put(email, createdOtp);

            sendOtpMessage(email, "Your OTP: " + createdOtp);


            return "OTP sent successfully";


        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to access the database to find the user");
        }
    }


    public String updatePassword(String email, String password, String receivedOtp) {
        try {
            Optional<User> OptionalUser = userRepository.findByUsername(email);

            if (OptionalUser.isEmpty()) {
                throw new UserNotFound("User not found");
            }
            User user = OptionalUser.get();
             String storedOtp = otpStore.get(email);
            if (storedOtp != null && storedOtp.equals(receivedOtp)) {
                user.setPassword(encoder.encode(password));
                userRepository.save(user);
                otpStore.remove(email);
                return "Password updated successfully";
            }
            else{
                throw new OtpMismatchException("OTP is not matching");
            }
        }
        catch (DataAccessException e) {
            throw new RuntimeException("Unable to update user password due to database issue", e);
        }
    }
}