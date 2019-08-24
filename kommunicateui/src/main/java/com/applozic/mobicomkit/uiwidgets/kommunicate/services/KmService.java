package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelUpdateTask;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelRemoveMemberTask;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KommunicateUI;
import com.applozic.mobicomkit.uiwidgets.kommunicate.database.KmAutoSuggestionDatabase;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmAutoSuggestionModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmApiResponse;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFeedback;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ashish on 03/04/18.
 * updated by shubham on 07/19. (feedback)
 */

public class KmService {

    private Context context;
    private KmClientService clientService;
    private KmAutoSuggestionDatabase autoSuggestionDatabase;

    public KmService(Context context) {
        this.context = ApplozicService.getContext(context);
        clientService = new KmClientService(context);
        autoSuggestionDatabase = KmAutoSuggestionDatabase.getInstance(this.context);
    }

    public String getAwayMessage(String appKey, Integer groupId) throws Exception {
        String response = clientService.getAwayMessage(appKey, groupId);

        if (response == null) {
            return null;
        }

        return response;
    }

    public KmApiResponse<List<KmAutoSuggestionModel>> getKmAutoSuggestions() {
        try {
            Type listType = new TypeToken<KmApiResponse<List<KmAutoSuggestionModel>>>() {
            }.getType();

            KmApiResponse<List<KmAutoSuggestionModel>> kmApiResponse = new Gson().fromJson(clientService.getKmAutoSuggestions(), listType);
            if (kmApiResponse != null) {
                List<KmAutoSuggestionModel> autoSuggestionList = kmApiResponse.getData();
                if (autoSuggestionList != null && !autoSuggestionList.isEmpty() && autoSuggestionDatabase != null) {
                    for (KmAutoSuggestionModel kmAutoSuggestion : autoSuggestionList) {
                        autoSuggestionDatabase.upsertAutoSuggestion(kmAutoSuggestion);
                    }
                }
            }
            return kmApiResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Contact getSupportGroupContact(Context context, Channel channel, BaseContactService contactService, int loggedInUserRoleType) {
        if (User.RoleType.USER_ROLE.getValue() == loggedInUserRoleType) {
            Map<String, String> metadataMap = channel.getMetadata();
            if (metadataMap != null) {
                String conversationAssignee = null;
                String conversationTitle = null;

                if (metadataMap.containsKey(KommunicateUI.CONVERSATION_ASSIGNEE)) {
                    conversationAssignee = metadataMap.get(KommunicateUI.CONVERSATION_ASSIGNEE);
                }

                if (metadataMap.containsKey(KommunicateUI.KM_CONVERSATION_TITLE)) {
                    conversationTitle = metadataMap.get(KommunicateUI.KM_CONVERSATION_TITLE);
                }

                if (!TextUtils.isEmpty(conversationAssignee)) {
                    return TextUtils.isEmpty(conversationAssignee) ? null : contactService.getContactById(conversationAssignee);
                }
                return TextUtils.isEmpty(conversationTitle) ? null : contactService.getContactById(conversationTitle);
            }
        } else {
            String userId = KmChannelService.getInstance(context).getUserInSupportGroup(channel.getKey());
            return TextUtils.isEmpty(userId) ? null : contactService.getContactById(userId);
        }
        return null;
    }

    public static Contact getAssigneeContact(Channel channel, BaseContactService contactService) {
        Map<String, String> metadataMap = channel.getMetadata();
        if (metadataMap != null) {
            String conversationAssignee = null;
            String conversationTitle = null;

            if (metadataMap.containsKey(KommunicateUI.CONVERSATION_ASSIGNEE)) {
                conversationAssignee = metadataMap.get(KommunicateUI.CONVERSATION_ASSIGNEE);
            }

            if (metadataMap.containsKey(KommunicateUI.KM_CONVERSATION_TITLE)) {
                conversationTitle = metadataMap.get(KommunicateUI.KM_CONVERSATION_TITLE);
            }

            if (!TextUtils.isEmpty(conversationAssignee)) {
                return TextUtils.isEmpty(conversationAssignee) ? null : contactService.getContactById(conversationAssignee);
            }
            return TextUtils.isEmpty(conversationTitle) ? null : contactService.getContactById(conversationTitle);
        }
        return null;
    }

    public static void removeMembersFromChannel(Context context, Integer channelKey, final Set<String> userIds, final ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener listener) {
        if (userIds == null || channelKey == null) {
            return;
        }

        int i = 0;
        for (String userId : userIds) {
            ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener recListener = new ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener() {
                @Override
                public void onSuccess(String response, int i, Context context) {
                    if (i == userIds.size() - 1) {
                        listener.onSuccess(response, i, context);
                    }
                }

                @Override
                public void onFailure(String response, Exception e, Context context) {
                    listener.onFailure(response, e, context);
                }
            };
            new ApplozicChannelRemoveMemberTask(context, channelKey, userId, i, recListener).execute();
            i++;
        }
    }

    public static void updateChannel(Context context, GroupInfoUpdate groupInfoUpdate, AlChannelUpdateTask.AlChannelUpdateListener listener) {
        new AlChannelUpdateTask(context, groupInfoUpdate, listener).execute();
    }

    /**
     * the wrapper method to get feedback for given conversation
     * @param conversationId the groupId of the conversation
     * @return the response object, response.getData() will return null in case of feedback not found
     */
    public synchronized String getConversationFeedback(String conversationId) {
        return clientService.getConversationFeedback(conversationId);
    }

    /**
     * the wrapper method tp set the feedback for the conversation at the server
     * @param kmFeedback the feedback object (has groupId, rating and comments data members)
     * @return string response of the post request
     */
    public synchronized String setConversationFeedback(KmFeedback kmFeedback) throws Exception {
        String response = clientService.setConversationFeedback(kmFeedback.getGroupId(), kmFeedback.getRating(), kmFeedback.getComments());
        return response;
    }

}
