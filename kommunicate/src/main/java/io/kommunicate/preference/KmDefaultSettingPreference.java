package io.kommunicate.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Set;

import io.kommunicate.KmConversationBuilder;

public class KmDefaultSettingPreference {

    public static final String DEFAULT_BOT_IDS = "DEFAULT_BOT_IDS";
    public static final String DEFAULT_AGENT_IDS = "DEFAULT_AGENT_IDS";
    public static final String DEFAULT_ASSIGNEE = "DEFAULT_ASSIGNEE";
    public static final String DEFAUT_TEAM = "DEFAUT_TEAM";
    public static final String SKIP_ROUTING = "SKIP_ROUTING";
    private static SharedPreferences preferences;
    private static KmDefaultSettingPreference kmPreference;
    private static final String KM_DEFAULT_SETTING_PREFERENCE = "KOMMUNICATE_SETTING_PREFS";

    private KmDefaultSettingPreference() {
        preferences = ApplozicService.getAppContext().getSharedPreferences(KM_DEFAULT_SETTING_PREFERENCE, Context.MODE_PRIVATE);
    }


    public static KmDefaultSettingPreference getInstance() {
        if (kmPreference == null) {
            kmPreference = new KmDefaultSettingPreference();
        }
        return kmPreference;
    }

    public void clearSettings() {
        preferences.edit().clear().apply();
    }

    public void setDefaultBotIds(Set<String> defaultBotIds) {
        preferences.edit().putStringSet(DEFAULT_BOT_IDS, defaultBotIds).apply();
    }

    public Set<String> getDefaultBotIds() {
        return preferences.getStringSet(DEFAULT_BOT_IDS, null);
    }
    public void setDefaultAgentIds(Set<String> defaultAgentIdsIds) {
        preferences.edit().putStringSet(DEFAULT_AGENT_IDS, defaultAgentIdsIds).apply();
    }

    public Set<String> getDefaultAgentIds() {
        return preferences.getStringSet(DEFAULT_AGENT_IDS, null);
    }

    public void setDefaultAssignee(String assignee) {
        preferences.edit().putString(DEFAULT_ASSIGNEE, assignee).apply();
    }

    public String getDefaultAssignee() {
        return preferences.getString(DEFAULT_ASSIGNEE, null);
    }

    public void setDefaultTeamId(String teamId) {
        preferences.edit().putString(DEFAUT_TEAM, teamId).apply();
    }

    public String getDefaultTeamId() {
        return preferences.getString(DEFAUT_TEAM, null);
    }

    public void setSkipRouting(boolean isSkipRouting) {
        preferences.edit().putBoolean(SKIP_ROUTING,  isSkipRouting).apply();
    }

    public boolean isSkipRouting() { return preferences.getBoolean(SKIP_ROUTING, false);}
}
