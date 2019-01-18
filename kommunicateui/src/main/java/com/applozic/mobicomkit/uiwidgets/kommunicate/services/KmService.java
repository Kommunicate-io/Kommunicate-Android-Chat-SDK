package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KommunicateUI;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.Map;

/**
 * Created by ashish on 03/04/18.
 */

public class KmService {

    private Context context;
    private KmClientService clientService;


    public KmService(Context context) {
        this.context = context;
        clientService = new KmClientService(context);
    }

    public String getAwayMessage(String appKey, Integer groupId) throws Exception {
        String response = clientService.getAwayMessage(appKey, groupId);

        if (response == null) {
            return null;
        }

        return response;
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
                    return contactService.getContactById(conversationAssignee);
                }
                return contactService.getContactById(conversationTitle);
            }
        } else {
            String userId = KmChannelService.getInstance(context).getUserInSupportGroup(channel.getKey());
            return TextUtils.isEmpty(userId) ? null : contactService.getContactById(userId);
        }
        return null;
    }
}
