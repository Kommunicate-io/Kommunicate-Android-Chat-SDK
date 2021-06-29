package io.kommunicate;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.HashMap;
import java.util.Map;

import io.kommunicate.async.KmAssigneeUpdateTask;
import io.kommunicate.async.KmConversationInfoTask;
import io.kommunicate.async.KmUpdateConversationTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmGetConversationInfoCallback;

public class KmSettings {

    public static final String TAG = "KmSettings";
    public static final String KM_CHAT_CONTEXT = "KM_CHAT_CONTEXT";
    public static final String KM_LANGUAGE_UPDATE_KEY = "kmUserLanguageCode";
    public static final String KM_CONVERSATION_METADATA = "conversationMetadata";

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
        updateChatContext(context, languageCodeMap);
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
        ApplozicClient.getInstance(context).setMessageMetaData(existingMetadata);
    }

    public static void updateConversation(Context context, GroupInfoUpdate groupInfoUpdate, KmUpdateConversationTask.KmConversationUpdateListener listener) {
        new KmUpdateConversationTask(context, groupInfoUpdate, listener).execute();
    }

    public static void updateConversationAssignee(Context context, Integer conversationId, String clientConversationId, final String assigneeId, final KmCallback callback) {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(final Channel channel, Context context) {
                Utils.printLog(context, TAG, "Updating conversation assignee for : " + channel.getKey() + "\nAssignee : " + assigneeId);
                new KmAssigneeUpdateTask(channel.getKey(), assigneeId, new KmCallback() {
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
                            callback.onFailure(new KmException("Unable to update"));
                        }
                    }
                }).execute();
            }

            @Override
            public void onFailure(Exception e, Context context) {
                callback.onFailure(e);
            }
        };

        new KmConversationInfoTask(context, conversationId, clientConversationId, conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void updateTeamId(Context context, Integer conversationId, String clientConversationId, String teamId, KmCallback callback) {
        updateConversationInfo(context, conversationId, clientConversationId, teamId, null, callback);
    }

    public static void updateConversationInfo(Context context, Integer conversationId, String clientConversationId, Map<String, String> conversationMetadata, KmCallback callback) {
        updateConversationInfo(context, conversationId, clientConversationId, null, conversationMetadata, callback);
    }

    public static void updateConversationInfo(Context context, Integer conversationId, String clientConversationId, final String teamId, final Map<String, String> conversationMetadata, final KmCallback callback) {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(final Channel channel, Context context) {
                if (channel != null) {
                    Map<String, String> metadataForUpdate = channel.getMetadata();

                    if (!TextUtils.isEmpty(teamId)) {
                        metadataForUpdate.put(KmConversationHelper.KM_TEAM_ID, teamId);
                    }
                    if (conversationMetadata != null) {
                        metadataForUpdate = getChannelMetadataWithConversationInfo(metadataForUpdate, conversationMetadata);
                    }

                    GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(metadataForUpdate, channel.getKey());

                    updateConversation(context, groupInfoUpdate, new KmUpdateConversationTask.KmConversationUpdateListener() {
                        @Override
                        public void onSuccess(Context context) {
                            Utils.printLog(context, TAG, "Successfully updated conversation metadata for : " + channel.getKey());
                            if (callback != null) {
                                callback.onSuccess(channel.getClientGroupId());
                            }
                        }

                        @Override
                        public void onFailure(Context context) {
                            Utils.printLog(context, TAG, "Failed to update conversation metadata for : " + channel.getKey());
                            if (callback != null) {
                                callback.onFailure(new KmException("Unable to update"));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                callback.onFailure(e);
            }
        };

        new KmConversationInfoTask(context, conversationId, clientConversationId, conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
}
