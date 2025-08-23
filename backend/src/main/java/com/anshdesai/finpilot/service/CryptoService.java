package com.anshdesai.finpilot.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;   // 16-byte auth tag
    private static final int IV_BYTES = 12;        // 12-byte nonce recommended for GCM

    private final SecretKey key;
    private final SecureRandom rnd = new SecureRandom();

    public CryptoService(@Value("${app.crypto.key-base64}") String keyB64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyB64);
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    /** Encrypts plaintext; returns base64( IV || CIPHERTEXT ) */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            rnd.nextBytes(iv);

            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] ct = c.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // concatenate IV + ciphertext
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    /** Decrypts base64( IV || CIPHERTEXT ) back to plaintext */
    public String decrypt(String b64) {
        try {
            byte[] in = Base64.getDecoder().decode(b64);
            if (in.length < IV_BYTES + 16) { // minimal size (iv + tag)
                throw new IllegalArgumentException("ciphertext too short");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] ct = new byte[in.length - IV_BYTES];
            System.arraycopy(in, 0, iv, 0, IV_BYTES);
            System.arraycopy(in, IV_BYTES, ct, 0, ct.length);

            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] pt = c.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }
}