package io.kommunicate.data.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import io.kommunicate.KommunicateService;

public class KmBotPreference {
    private static final String KM_BOT_PREFERENCE = "km_bot_preference";
    private static final String EMPTY_STRING = "";
    private static final int MAX_SHARED_PREF_SIZE = 50;
    private static KmBotPreference kmBotPreference;
    private SharedPreferences sharedPreferences;

    private KmBotPreference(Context context) {
        sharedPreferences = KommunicateService.getContext(context).getSharedPreferences(KM_BOT_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static KmBotPreference getInstance(Context context) {
        if (kmBotPreference == null) {
            kmBotPreference = new KmBotPreference(KommunicateService.getContext(context));
        }
        return kmBotPreference;
    }

    //also clears shared pref if size too big
    public void addBotType(String botId, String botType) {
        if (TextUtils.isEmpty(botId)) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getAll().size() > MAX_SHARED_PREF_SIZE) {
            editor.clear();
        }
        editor.putString(botId, botType);
        editor.commit();
    }

    public String getBotType(String botId) {
        return sharedPreferences.getString(botId, EMPTY_STRING);
    }
}
