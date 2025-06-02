package Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Lỗi nghiêm trọng: Thuật toán SHA-256 không được hỗ trợ.");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        if (plainPassword == null || hashedPasswordFromDB == null) {
            return false;
        }
        String hashedPlainPassword = hashPassword(plainPassword);
        if (hashedPlainPassword == null) {
            return false;
        }
        return hashedPasswordFromDB.equals(hashedPlainPassword);
    }
}