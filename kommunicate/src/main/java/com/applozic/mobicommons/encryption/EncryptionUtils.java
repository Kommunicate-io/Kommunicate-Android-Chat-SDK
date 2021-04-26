package com.applozic.mobicommons.encryption;

import android.text.TextUtils;
import android.util.Base64;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by sunil on 26/8/16.
 */
public class EncryptionUtils {

    private static final String TAG = "EncryptionUtils";
    private static final String ALGORITHM = "AES/ECB/NoPadding";
    private static final String ALGORITHM_AES = "AES";

    // Performs Encryption
    public static String encrypt(String ketString, String plainText) throws Exception {
        // generate key
        if(TextUtils.isEmpty(plainText)){
            return null;
        }
        Key key =  generateKey(ketString);
        Cipher chiper = Cipher.getInstance(ALGORITHM);
        chiper.init(Cipher.ENCRYPT_MODE, key);
        byte[] w = plainText.getBytes();
        int bufferLength = 3072;
        int i = 0;
        byte[] buf = new byte[bufferLength];
        StringBuffer stringBuffer = new StringBuffer();
        while (i < w.length) {
            int size = Math.min(bufferLength, w.length - i);
            Arrays.fill(buf, (byte) 0);
            System.arraycopy(w, i, buf, 0, size);
            buf = chiper.doFinal(buf);
            stringBuffer.append(Base64.encodeToString(buf,Base64.DEFAULT));
            i += size;
        }
        return stringBuffer.toString();
    }

    // Performs decryption
    public static String decrypt(String ketString, String encryptedText) throws Exception {
        // generate key
        Key key =  generateKey(ketString);
        Cipher chiper= Cipher.getInstance(ALGORITHM);
        chiper.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(encryptedText,Base64.DEFAULT);
        byte[] decValue = chiper.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return TextUtils.isEmpty(decryptedValue)?null:decryptedValue.trim();
    }

    //generateKey() is used to generate a secret key for AES algorithm
    private static Key generateKey(String ketString) throws Exception {
        Key key = new SecretKeySpec(ketString.getBytes(), ALGORITHM_AES);
        return key;
    }

}