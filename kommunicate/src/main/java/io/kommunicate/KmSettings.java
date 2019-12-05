package io.kommunicate;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;

public class KmSettings {

    public static final String KM_CHAT_CONTEXT = "KM_CHAT_CONTEXT";
    public static final String KM_LANGUAGE_UPDATE_KEY = "kmUserLanguageCode";

    /**
     * will update the metadata object with the KM_CHAT_CONTEXT field
     *
     * @param context         the context
     * @param messageMetadata the map data to update the KM_CHAT_CONTEXT field with
     */
    public static void updateChatContext(Context context, Map<String, String> messageMetadata) {
        //converting the messageMetadata parameter passed to function (keyed by KM_CHAT_CONTEXT), to json string
        String messageMetaDataString = GsonUtils.getJsonFromObject(messageMetadata, Map.class);
        if (TextUtils.isEmpty(messageMetaDataString)) {
            return;
        }

        //getting the message metadata already in the applozic preferences
        String existingMetaDataString = ApplozicClient.getInstance(context).getMessageMetaData();
        Map<String, String> existingMetadata;

        if (TextUtils.isEmpty(existingMetaDataString)) { //case 1: no existing metadata
            existingMetadata = new HashMap<>();
        } else { //case 2: metadata already exists
            existingMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetaDataString, Map.class);

            if (existingMetadata.containsKey(KM_CHAT_CONTEXT)) { //case 2a: km_chat-context already exists
                Map<String, String> existingKmChatContext = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetadata.get(KM_CHAT_CONTEXT), Map.class);

                for (Map.Entry<String, String> data : messageMetadata.entrySet()) {
                    existingKmChatContext.put(data.getKey(), data.getValue());
                }

                //update messageMetadataString
                messageMetaDataString = GsonUtils.getJsonFromObject(existingKmChatContext, Map.class);
            }
        }

        existingMetadata.put(KM_CHAT_CONTEXT, messageMetaDataString);
        ApplozicClient.getInstance(context).setMessageMetaData(existingMetadata);
    }

    public static void updateUserLanguage(Context context, String languageCode) {
        Map<String, String> languageCodeMap = new HashMap<>();
        languageCodeMap.put(KM_LANGUAGE_UPDATE_KEY, languageCode);
        updateMessageMetadata(context, languageCodeMap);
    }

    public static void updateMessageMetadata(Context context, Map<String, String> metadata) {
        String existingMetadataString = ApplozicClient.getInstance(context).getMessageMetaData();
        Map<String, String> existingMetadata;
        if (TextUtils.isEmpty(existingMetadataString)) {
            existingMetadata = new HashMap<>();
        } else {
            existingMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetadataString, Map.class);
        }

        existingMetadata.putAll(metadata);
    }
}
