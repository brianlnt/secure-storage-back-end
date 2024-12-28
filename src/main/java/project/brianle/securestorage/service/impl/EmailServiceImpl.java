package project.brianle.securestorage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import project.brianle.securestorage.exceptions.CustomException;
import project.brianle.securestorage.service.EmailService;

import static project.brianle.securestorage.utils.EmailUtils.getNewAccountMessage;
import static project.brianle.securestorage.utils.EmailUtils.getResetPasswordMessage;

@Service
@RequiredArgsConstructor //Generates a constructor for all final fields, ensuring dependencies are initialized.
@Slf4j
public class EmailServiceImpl implements EmailService {
    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String RESET_PASSWORD_REQUEST = "Reset Password Request";
    private final JavaMailSender sender;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async //allows you to run methods in separate threads so the main thread is not blocked.
    public void sendNewAccountEmail(String name, String toEmail, String key) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getNewAccountMessage(name, host, key));
            sender.send(message);
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new CustomException("Unable to send email");
        }
    }

    @Override
    @Async //allows you to run methods in separate threads so the main thread is not blocked.
    public void sendPasswordResetEmail(String name, String toEmail, String key) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(RESET_PASSWORD_REQUEST);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getResetPasswordMessage(name, host, key));
            sender.send(message);
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new CustomException("Unable to send email");
        }
    }
}
