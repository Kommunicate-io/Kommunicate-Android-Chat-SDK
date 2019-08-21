package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.kommunicate.async.KmConversationRemoveMemberTask;
import io.kommunicate.async.KmUpdateConversationTask;
import io.kommunicate.callbacks.KmRemoveMemberCallback;
import io.kommunicate.database.KmAutoSuggestionDatabase;
import io.kommunicate.models.KmApiResponse;
import io.kommunicate.models.KmAutoSuggestionModel;
import io.kommunicate.utils.KmConstants;

/**
 * Created by ashish on 03/04/18.
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

                if (metadataMap.containsKey(KmConstants.CONVERSATION_ASSIGNEE)) {
                    conversationAssignee = metadataMap.get(KmConstants.CONVERSATION_ASSIGNEE);
                }

                if (metadataMap.containsKey(KmConstants.KM_CONVERSATION_TITLE)) {
                    conversationTitle = metadataMap.get(KmConstants.KM_CONVERSATION_TITLE);
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

            if (metadataMap.containsKey(KmConstants.CONVERSATION_ASSIGNEE)) {
                conversationAssignee = metadataMap.get(KmConstants.CONVERSATION_ASSIGNEE);
            }

            if (metadataMap.containsKey(KmConstants.KM_CONVERSATION_TITLE)) {
                conversationTitle = metadataMap.get(KmConstants.KM_CONVERSATION_TITLE);
            }

            if (!TextUtils.isEmpty(conversationAssignee)) {
                return TextUtils.isEmpty(conversationAssignee) ? null : contactService.getContactById(conversationAssignee);
            }
            return TextUtils.isEmpty(conversationTitle) ? null : contactService.getContactById(conversationTitle);
        }
        return null;
    }

    public static void removeMembersFromConversation(final Context context, Integer channelKey, final Set<String> userIds, final KmRemoveMemberCallback listener) {
        if (userIds == null || channelKey == null) {
            return;
        }

        int i = 0;
        for (String userId : userIds) {
            KmRemoveMemberCallback recListener = new KmRemoveMemberCallback() {
                @Override
                public void onSuccess(String response, int i) {
                    if (i == userIds.size() - 1) {
                        listener.onSuccess(response, i);
                    }
                }

                @Override
                public void onFailure(String response, Exception e) {
                    listener.onFailure(response, e);
                }
            };
            new KmConversationRemoveMemberTask(context, channelKey, userId, i, recListener).execute();
            i++;
        }
    }

    public static void updateConversation(Context context, GroupInfoUpdate groupInfoUpdate, KmUpdateConversationTask.KmConversationUpdateListener listener) {
        new KmUpdateConversationTask(context, groupInfoUpdate, listener).execute();
    }
}