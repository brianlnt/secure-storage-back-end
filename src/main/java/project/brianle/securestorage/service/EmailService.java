package project.brianle.securestorage.service;

public interface EmailService {
    void sendNewAccountEmail(String name, String to, String key);
    void sendPasswordResetEmail(String name, String to, String key);
}
