package io.kommunicate.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import io.kommunicate.commons.AppContextService;

/**
 * Manages the creation and retrieval of a secure key for database encryption,
 * using the Android Keystore system. This key is independent of the applicationKey
 * and is securely stored on the device, making it available even when the SDK is not
 * fully initialized.
 */
public class DatabaseKeyProvider {

    private static final String TAG = "DatabaseKeyProvider";
    private static final String SECURE_PREFS_FILE_NAME = "km_secure_db_prefs";
    private static final String DATABASE_KEY_ALIAS = "km_db_encryption_key";
    private static volatile String cachedKey = null;

    /**
     * Generates or retrieves the database encryption key.
     * This method is thread-safe and can be called at any time.
     *
     * @param context The application context.
     * @return The database encryption key.
     */
    public static synchronized String getDatabaseKey(Context context) {
        if (cachedKey != null) {
            return cachedKey;
        }

        SharedPreferences securePrefs = createEncryptedSharedPreferences(context);
        if (securePrefs == null) {
            // Fallback for critical failure, though unlikely.
            Log.e(TAG, "Failed to create secure preferences. Using a temporary, insecure key.");
            return "temporary-insecure-fallback-key";
        }

        String key = securePrefs.getString(DATABASE_KEY_ALIAS, null);
        if (key == null) {
            Log.i(TAG, "No database key found. Generating a new one.");
            key = generateRandomKey();
            securePrefs.edit().putString(DATABASE_KEY_ALIAS, key).apply();
        }
        cachedKey = key;
        return key;
    }

    private static SharedPreferences createEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(AppContextService.getContext(context))
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    AppContextService.getContext(context),
                    SECURE_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences", e);
            // In a production app, you might want to handle this failure more gracefully,
            // perhaps by notifying a crash reporting service.
            return null;
        }
    }

    private static String generateRandomKey() {
        // Generate a 256-bit (32-byte) random key for SQLCipher.
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP);
    }
}