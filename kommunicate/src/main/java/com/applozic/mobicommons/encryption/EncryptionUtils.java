package com.applozic.mobicommons.encryption;

import android.text.TextUtils;
import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sunil on 26/8/16.
 */
public class EncryptionUtils {

    private static final String TAG = "EncryptionUtils";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String ALGORITHM_AES = "AES";

    // Performs Encryption
    public static String encrypt(String ketString, String plainText) throws Exception {
        // generate key
        if(TextUtils.isEmpty(plainText)){
            return null;
        }
        Key key =  generateKey(ketString);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
        byte[] encryptedByteValue = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
    }

    // Performs decryption
    public static String decrypt(String ketString, String encryptedText) throws Exception {
        // generate key
        Key key =  generateKey(ketString);
        Cipher cipher= Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
        byte[] decryptedValue64 = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
        String decryptedValue = new String(decryptedByteValue, "UTF-8");
        return TextUtils.isEmpty(decryptedValue) ? null: decryptedValue.trim();
    }

    //generateKey() is used to generate a secret key for AES algorithm
    private static Key generateKey(String ketString) throws Exception {
        return new SecretKeySpec(ketString.getBytes(), ALGORITHM);
    }

}