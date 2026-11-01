package com.mrxlogin.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** PBKDF2-HMAC-SHA256 password hashing. Never stores plaintext passwords. */
public final class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHasher() {}

    public static String hash(char[] password, int iterations) throws Exception {
        byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, iterations);
        return "pbkdf2-sha256$" + iterations + "$" + Base64.getUrlEncoder().withoutPadding().encodeToString(salt)
                + "$" + Base64.getUrlEncoder().withoutPadding().encodeToString(derived);
    }

    public static boolean verify(char[] password, String encoded) throws Exception {
        String[] p = encoded.split("\\$");
        if (p.length != 4 || !p[0].equals("pbkdf2-sha256")) return false;
        int iterations = Integer.parseInt(p[1]);
        if (iterations < 100000 || iterations > 2000000) return false;
        byte[] salt = Base64.getUrlDecoder().decode(p[2]);
        byte[] expected = Base64.getUrlDecoder().decode(p[3]);
        return MessageDigest.isEqual(expected, derive(password, salt, iterations));
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
}
