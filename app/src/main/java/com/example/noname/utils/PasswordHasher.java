package com.example.noname.utils;

import android.util.Base64; // Dùng để encode/decode byte array sang String
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final String TAG = "PasswordHasher";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 10000; // Số lần lặp
    private static final int KEY_LENGTH = 256; // Chiều dài khóa (bit)

    // Phương thức để băm mật khẩu
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16]; // Tạo salt ngẫu nhiên
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            // Kết hợp salt và hash để lưu trữ
            // Format: salt_base64_encoded + ":" + hash_base64_encoded
            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage(), e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Phương thức để xác thực mật khẩu
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                Log.e(TAG, "Stored hash is not in the correct format.");
                return false;
            }

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] hash = Base64.decode(parts[1], Base64.NO_WRAP);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            // So sánh hash mới tạo với hash đã lưu
            return arrayEquals(hash, testHash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            Log.e(TAG, "Error verifying password: " + e.getMessage(), e);
            return false;
        }
    }

    // Helper method để so sánh hai mảng byte một cách an toàn (tránh timing attacks)
    private static boolean arrayEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}