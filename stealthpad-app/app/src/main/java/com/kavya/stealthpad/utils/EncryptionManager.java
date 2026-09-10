package com.kavya.stealthpad.utils;

import android.util.Base64;
import com.kavya.stealthpad.EncryptionModule.AES_EncryptDecrypt;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EncryptionManager {

    private final AES_EncryptDecrypt helper;

    @Inject
    public EncryptionManager() {
        this.helper = new AES_EncryptDecrypt();
    }

    public String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) return strToEncrypt;
        try {
            return helper.encrypt(strToEncrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) return strToDecrypt;
        try {
            return helper.decrypt(strToDecrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
