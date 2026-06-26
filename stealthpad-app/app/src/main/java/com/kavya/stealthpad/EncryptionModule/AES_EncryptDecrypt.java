package com.kavya.stealthpad.EncryptionModule;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AES_EncryptDecrypt {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    public String encrypt(String plainText) throws Exception {

        SecretKey secretKey = KeyStoreManager.getSecretKey();

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();

        byte[] encryptedBytes =
                cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer =
                ByteBuffer.allocate(iv.length + encryptedBytes.length);

        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);

        return Base64.encodeToString(
                byteBuffer.array(),
                Base64.DEFAULT
        );
    }

    public String decrypt(String encryptedText) throws Exception {

        SecretKey secretKey = KeyStoreManager.getSecretKey();

        byte[] combined =
                Base64.decode(encryptedText, Base64.DEFAULT);

        ByteBuffer byteBuffer = ByteBuffer.wrap(combined);

        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);

        byte[] encryptedBytes =
                new byte[byteBuffer.remaining()];

        byteBuffer.get(encryptedBytes);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        GCMParameterSpec spec =
                new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                spec
        );

        byte[] decryptedBytes =
                cipher.doFinal(encryptedBytes);

        return new String(
                decryptedBytes,
                StandardCharsets.UTF_8
        );
    }
}