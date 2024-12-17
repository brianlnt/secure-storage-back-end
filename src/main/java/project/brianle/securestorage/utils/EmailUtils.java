package project.brianle.securestorage.utils;

public class EmailUtils {
    public static String getNewAccountMessage(String name, String host, String token){
        return "Hello " + name + ",\n\n" +
                "You have successfully created a new account on Secure Storage.\n" +
                "To activate your account, please click the link below:\n" +
                getVerificationUrl(host, token) + "\n\n" +
                "If you did not create this account, please disregard this email.\n\n" +
                "Best regards,\n\n" + "The Secure Storage Team";
    }

    private static String getVerificationUrl(String host, String token) {
        return host + "/verify/account?token=" + token;
    }

    public static String getResetPasswordMessage(String name, String host, String token){
        return "Hello " + name + ",\n\n" +
                "We received a request to reset your password for your Secure Storage account\n" +
                "To reset your password, click on the link below within the next 24 hours:\n" +
                getResetPasswordUrl(host, token) + "\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Best regards,\n\n" + "The Secure Storage Team";
    }

    private static String getResetPasswordUrl(String host, String token) {
        return host + "/verify/password?token=" + token;
    }
}