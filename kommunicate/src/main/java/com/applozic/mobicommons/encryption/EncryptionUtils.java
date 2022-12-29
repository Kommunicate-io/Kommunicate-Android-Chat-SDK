package com.applozic.mobicommons.encryption;

import android.text.TextUtils;
import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by sunil on 26/8/16.
 */
public class EncryptionUtils {

    private static final String TAG = "EncryptionUtils";
    private static final String ALGORITHM = "AES";
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";


    // Converts String to Hex
    public static String convertStringToHex(String s) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            char[] charArray = s.toCharArray();
            for (char c : charArray) {
                String charToHex = Integer.toHexString(c);
                stringBuilder.append(charToHex);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                    (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    //Performs encryption using AES256
    public static String encrypt(String secret, String strToEncrypt, String IV) throws Exception {
        if (strToEncrypt == null) {
            return null;
        }
        if(TextUtils.isEmpty(secret) || TextUtils.isEmpty(IV)) {
            return strToEncrypt;
        }
        Cipher cipher = generateKey(secret, Cipher.ENCRYPT_MODE, IV);
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    //Performs decryption using AES256
    public static  String decrypt(String secret, String strToDecrypt, String IV) throws Exception {
        if (strToDecrypt == null) {
            return null;
        }
        if(TextUtils.isEmpty(secret) || TextUtils.isEmpty(IV)) {
            return strToDecrypt;
        }
        Cipher cipher = generateKey(secret, Cipher.DECRYPT_MODE, IV);
        byte[] decrypted = cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT));
        return new String(decrypted, "UTF-8");
    }
    public static Cipher generateKey(String keyString, int mode, String IV) throws Exception {
        SecretKeySpec key = new SecretKeySpec(Base64.decode(keyString, Base64.DEFAULT), ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_CBC);
        IvParameterSpec ivspec =
                new IvParameterSpec(hexStringToByteArray(convertStringToHex(IV)));
        cipher.init(mode, key, ivspec);
        return cipher;
    }

}