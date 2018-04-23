package io.kommunicate;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ashish on 21/04/18.
 */

public class KmPreference {

    private static SharedPreferences preferences;
    private static KmPreference kmPreference;
    private static String KM_USER_PREFERENCES = "KOMMUNICATE_USER_PREFS";

    private String HELPDOCS_ACCESS_KEY = "HELPDOCS_ACCESS_KEY";

    private KmPreference(Context context) {
        preferences = context.getSharedPreferences(KM_USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static KmPreference getInstance(Context context) {
        if (kmPreference == null) {
            kmPreference = new KmPreference(context);
        } else {
            preferences = context.getSharedPreferences(KM_USER_PREFERENCES, Context.MODE_PRIVATE);
        }
        return kmPreference;
    }

    public void setHelpDocsKey(String helpDocsKey) {
        preferences.edit().putString(HELPDOCS_ACCESS_KEY, helpDocsKey).apply();
    }

    public String getHelpDocsKey() {
        return preferences.getString(HELPDOCS_ACCESS_KEY, null);
    }
}
