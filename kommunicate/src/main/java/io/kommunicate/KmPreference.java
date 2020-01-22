package io.kommunicate;

import android.content.Context;
import android.content.SharedPreferences;

import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by ashish on 21/04/18.
 */

public class KmPreference {

    private static SharedPreferences preferences;
    private static KmPreference kmPreference;
    private static final String KM_USER_PREFERENCES = "KOMMUNICATE_USER_PREFS";
    private static final String KM_CONVERSATION_BUILDER = "KM_CONVERSATION_BUILDER";
    private static final String IS_FCM_REGISTRATION_CALL_DONE = "IS_FCM_REGISTRATION_CALL_DONE";

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

    public KmPreference setHelpDocsKey(String helpDocsKey) {
        preferences.edit().putString(HELPDOCS_ACCESS_KEY, helpDocsKey).apply();
        return this;
    }

    public String getHelpDocsKey() {
        return preferences.getString(HELPDOCS_ACCESS_KEY, null);
    }

    public KmPreference setKmConversationBuilder(KmConversationBuilder conversationBuilder) {
        preferences.edit().putString(KM_CONVERSATION_BUILDER, GsonUtils.getJsonFromObject(conversationBuilder, KmConversationBuilder.class)).apply();
        return this;
    }

    public KmConversationBuilder getKmConversationBuilder() {
        return (KmConversationBuilder) GsonUtils.getObjectFromJson(preferences.getString(KM_CONVERSATION_BUILDER, null), KmConversationBuilder.class);
    }

    public KmPreference setFcmRegistrationCallDone(boolean callDone) {
        if (preferences != null) {
            preferences.edit().putBoolean(IS_FCM_REGISTRATION_CALL_DONE, callDone).commit();
        }
        return this;
    }

    public boolean isFcmRegistrationCallDone() {
        return preferences != null && preferences.getBoolean(IS_FCM_REGISTRATION_CALL_DONE, false);
    }
}
