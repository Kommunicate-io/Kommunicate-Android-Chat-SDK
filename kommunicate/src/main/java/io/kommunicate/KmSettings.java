package io.kommunicate;

import static io.kommunicate.utils.KmConstants.KM_USER_LOCALE;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.feed.GroupInfoUpdate;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.channel.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.TaskListener;
import io.kommunicate.preference.KmDefaultSettingPreference;
import io.kommunicate.usecase.AssigneeUpdateUseCase;
import io.kommunicate.usecase.ConversationInfoUseCase;
import io.kommunicate.usecase.UpdateConversationUseCase;

public class KmSettings {

    public static final String TAG = "KmSettings";
    public static final String KM_CHAT_CONTEXT = "KM_CHAT_CONTEXT";
    public static final String KM_LANGUAGE_UPDATE_KEY = "kmUserLanguageCode";
    public static final String KM_CONVERSATION_METADATA = "conversationMetadata";
    private static final String UNABLE_TO_UPDATE = "Unable to update";

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

        String existingMetaDataString = SettingsSharedPreference.getInstance(context).getMessageMetaData();
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
        SettingsSharedPreference.getInstance(context).setMessageMetaData(existingMetadata);
    }

    public static void updateUserLanguage(Context context, String languageCode) {
        Map<String, String> languageCodeMap = new HashMap<>();
        languageCodeMap.put(KM_USER_LOCALE, languageCode);
        updateChatContext(context, languageCodeMap);
    }

    public static void updateMessageMetadata(Context context, Map<String, String> metadata) {
        String existingMetadataString = SettingsSharedPreference.getInstance(context).getMessageMetaData();
        Map<String, String> existingMetadata;
        if (TextUtils.isEmpty(existingMetadataString)) {
            existingMetadata = new HashMap<>();
        } else {
            existingMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetadataString, Map.class);
        }

        existingMetadata.putAll(metadata);
        SettingsSharedPreference.getInstance(context).setMessageMetaData(existingMetadata);
    }

    public static void updateConversation(Context context, GroupInfoUpdate groupInfoUpdate, TaskListener<Context> listener) {
        UpdateConversationUseCase.executeWithExecutor(context, groupInfoUpdate, listener);
    }

    public static void updateConversationAssignee(Context context, Integer conversationId, String clientConversationId, final String assigneeId, final KmCallback callback) {
        TaskListener<Channel> conversationInfoCallback = new TaskListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                Utils.printLog(context, TAG, "Updating conversation assignee for : " + channel.getKey() + "\nAssignee : " + assigneeId);
                KmCallback kmCallback = new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        Utils.printLog(null, TAG, "Successfully updated conversation assignee for : " + channel.getKey());
                        if (callback != null) {
                            callback.onSuccess(assigneeId);
                        }
                    }

                    @Override
                    public void onFailure(Object error) {
                        Utils.printLog(null, TAG, "Failed to update conversation assignee for : " + channel.getKey());
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                };

                AssigneeUpdateUseCase.executeWithExecutor(channel.getKey(), assigneeId, kmCallback);
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                callback.onFailure(error);
            }
        };

        ConversationInfoUseCase.executeWithExecutor(context, conversationId, clientConversationId, conversationInfoCallback);
    }

    public static void updateTeamId(Context context, Integer conversationId, String clientConversationId, String teamId, KmCallback callback) {
        updateConversationInfo(context, conversationId, clientConversationId, teamId, null, callback);
    }

    public static void updateConversationInfo(Context context, Integer conversationId, String clientConversationId, Map<String, String> conversationMetadata, KmCallback callback) {
        updateConversationInfo(context, conversationId, clientConversationId, null, conversationMetadata, callback);
    }

    public static void updateConversationInfo(Context context, Integer conversationId, String clientConversationId, final String teamId, final Map<String, String> conversationMetadata, final KmCallback callback) {
        TaskListener<Channel> conversationInfoCallback = new TaskListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    Map<String, String> metadataForUpdate = channel.getMetadata();

                    if (!TextUtils.isEmpty(teamId)) {
                        metadataForUpdate.put(KmConversationHelper.KM_TEAM_ID, teamId);
                    }
                    if (conversationMetadata != null) {
                        metadataForUpdate = getChannelMetadataWithConversationInfo(metadataForUpdate, conversationMetadata);
                    }

                    GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(metadataForUpdate, channel.getKey());

                    updateConversation(context, groupInfoUpdate, new TaskListener<Context>() {


                        @Override
                        public void onSuccess(Context context) {
                            Utils.printLog(context, TAG, "Successfully updated conversation metadata for : " + channel.getKey());
                            if (callback != null) {
                                callback.onSuccess(channel.getClientGroupId());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Exception error) {
                            if (callback != null) {
                                callback.onFailure(new KmException(UNABLE_TO_UPDATE));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                callback.onFailure(error);
            }
        };

        ConversationInfoUseCase.executeWithExecutor(context, conversationId, clientConversationId, conversationInfoCallback);
    }

    private static Map<String, String> getChannelMetadataWithConversationInfo(Map<String, String> channelMetadata, Map<String, String> conversationInfo) {
        if (channelMetadata == null) {
            channelMetadata = new HashMap<>();
        }

        Map<String, String> conversationMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(channelMetadata.get(KM_CONVERSATION_METADATA), Map.class);

        if (conversationMetadata == null) {
            conversationMetadata = new HashMap<>();
        }

        conversationMetadata.putAll(conversationInfo);

        channelMetadata.put(KM_CONVERSATION_METADATA, GsonUtils.getJsonFromObject(conversationMetadata, Map.class));

        return channelMetadata;
    }

    public static void setDefaultBotIds(List<String> botIds) {
        Set<String> hashSet = new HashSet<String>(botIds);
        hashSet.addAll(botIds);
        KmDefaultSettingPreference.getInstance().setDefaultBotIds(hashSet);
    }

    public static void setDefaultAgentIds(List<String> agentIds) {
        Set<String> hashSet = new HashSet<String>(agentIds);
        hashSet.addAll(agentIds);
        KmDefaultSettingPreference.getInstance().setDefaultAgentIds(hashSet);
    }

    public static void setDefaultAssignee(String assigneeId) {
        KmDefaultSettingPreference.getInstance().setDefaultAssignee(assigneeId);
    }

    public static void setDefaultTeamId(String teamId) {
        KmDefaultSettingPreference.getInstance().setDefaultTeamId(teamId);
    }

    public static void setSkipRouting(boolean isSkipRouting) {
        KmDefaultSettingPreference.getInstance().setSkipRouting(isSkipRouting);
    }

    public static void clearDefaultSettings() {
        KmDefaultSettingPreference.getInstance().clearSettings();
    }
}
