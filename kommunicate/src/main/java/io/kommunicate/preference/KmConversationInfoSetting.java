package io.kommunicate.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Map;

public class KmConversationInfoSetting {
    private static KmConversationInfoSetting kmConversationInfoSetting;
    private static SharedPreferences sharedPreferences;
    private static final String ENABLE_KM_CONVERSATION_INFO_VIEW = "ENABLE_KM_CONVERSATION_INFO_VIEW";
    private static final String INFO_CONTENT = "INFO_CONTENT";
    private static final String LEADING_IMAGE = "LEADING_IMAGE";
    private static final String TRAILING_IMAGE = "TRAILING_IMAGE";
    private static final String CONVERSATION_INFO_BACKGROUND_COLOR = "CONVERSATION_INFO_BACKGROUND_COLOR";
    private static final String CONVERSATION_INFO_CONTENT_COLOR = "CONVERSATION_INFO_CONTENT_COLOR";
    private static final String AGENT_EXPERIENCE = "AGENT_EXPERIENCE";
    private static final String AGENT_RATING = "AGENT_RATING";
    private static final String NEW_TOOLBAR_ENABLED = "NEW_TOOLBAR_ENABLED";
    private KmConversationInfoSetting(Context context) {
        sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static KmConversationInfoSetting getInstance(Context context) {
        if (kmConversationInfoSetting == null) {
            kmConversationInfoSetting = new KmConversationInfoSetting(ApplozicService.getContext(context));
        } else {
            sharedPreferences = context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
        }
        return kmConversationInfoSetting;
    }

    public KmConversationInfoSetting setInfoContent(String content) {
        if(sharedPreferences != null) {
            sharedPreferences.edit().putString(INFO_CONTENT, content).apply();
        }
        return this;
    }
    public String getInfoContent() {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(INFO_CONTENT, "");
        }
        return null;
    }

    public KmConversationInfoSetting enableKmConversationInfo(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(ENABLE_KM_CONVERSATION_INFO_VIEW, enable).apply();
        }
        return this;
    }
    public boolean isKmConversationInfoEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(ENABLE_KM_CONVERSATION_INFO_VIEW, false);
        }
        return false;
    }
    public KmConversationInfoSetting setLeadingImageIcon(String iconName) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(LEADING_IMAGE, iconName).apply();
        }
        return this;
    }
    public String getLeadingImageIcon() {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(LEADING_IMAGE, "");
        }
        return null;
    }
    public KmConversationInfoSetting setTrailingImageIcon(String iconName) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(TRAILING_IMAGE, iconName).apply();
        }
        return this;
    }
    public String getTrailingImageIcon() {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(TRAILING_IMAGE, "");
        }
        return null;
    }
    public KmConversationInfoSetting setBackgroundColor(String backgroundColor) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(CONVERSATION_INFO_BACKGROUND_COLOR, backgroundColor).apply();
        }
        return this;
    }
    public String getBackgroundColor() {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(CONVERSATION_INFO_BACKGROUND_COLOR, "");
        }
        return null;
    }
    public KmConversationInfoSetting setContentColor(String contentColor) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(CONVERSATION_INFO_CONTENT_COLOR, contentColor).apply();
        }
        return this;
    }
    public String getContentColo() {
        if(sharedPreferences != null) {
            return sharedPreferences.getString(CONVERSATION_INFO_CONTENT_COLOR, "");
        }
        return null;
    }

    public KmConversationInfoSetting enableCustomToolbarSubtitleDesign(boolean enable){
        if(sharedPreferences != null){
            sharedPreferences.edit().putBoolean(NEW_TOOLBAR_ENABLED,enable).commit();
        }
        return this;
    }

    public boolean isCustomToolbarSubtitleDesign(){
        if(sharedPreferences != null ){
            return sharedPreferences.getBoolean(NEW_TOOLBAR_ENABLED, false);

        }
        return false;
    }

    public KmConversationInfoSetting setToolbarAgentExperience(String experience){
        if(sharedPreferences != null ){
            sharedPreferences.edit().putString(AGENT_EXPERIENCE,experience).commit();
        }
        return this;
    }

    public String getToolbarAgentExperience() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(AGENT_EXPERIENCE, "");
        }
        return null;
    }

    public KmConversationInfoSetting setToolbarSubtitleRating(Float rating){
        if(sharedPreferences != null){
            sharedPreferences.edit().putFloat(AGENT_RATING,rating).commit();
        }
        return this;
    }

    public Float getToolbarSubtitleRating(){
        if (sharedPreferences != null){
            return sharedPreferences.getFloat(AGENT_RATING,-1.0F);
        }
        return null;
    }
}