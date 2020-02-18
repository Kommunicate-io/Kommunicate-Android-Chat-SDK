package com.applozic.mobicomkit.uiwidgets.conversation.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.KmConversationStatus;
import com.applozic.mobicomkit.uiwidgets.conversation.KmResolve;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;


public class KmResolveViewModel extends KmViewModel {

    public MutableLiveData<String> assigneeNameLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> resolveStatusLiveData = new MutableLiveData<>();
    private Channel channel;

    private int conversationStatus;
    private KmResolve kmResolve;

    public KmResolveViewModel(final AlCustomizationSettings alCustomizationSettings) {
        super(alCustomizationSettings);
        kmResolve = new KmResolve();
    }

    public void setChannel(Channel channel) {
        if (assigneeNameLiveData != null) {
            assigneeNameLiveData.postValue(getConversationAssinee(channel));
        }
        if (resolveStatusLiveData != null) {
            conversationStatus = getConversationStatus(channel);
            updateConversationStatus(conversationStatus);
            resolveStatusLiveData.postValue(conversationStatus);
        }
        this.channel = channel;
    }

    public KmResolve getKmResolveModel() {
        return this.kmResolve;
    }

    public int getCurrentStatus() {
        return conversationStatus;
    }

    public void updateConversationStatus(KmResolve resolve) {
        if (!KmConversationStatus.SPAM_STATUS_NAME.equals(resolve.getStatusName())) {
            KmConversationStatus.updateConversationStatus(resolve, channel);
        }
    }

    public void updateConversationStatus(int conversationStatus) {
        this.conversationStatus = conversationStatus;
        kmResolve.setColorResId(KmConversationStatus.getColorId(conversationStatus));
        kmResolve.setIconId(KmConversationStatus.getIconId(conversationStatus));
        kmResolve.setStatusName(KmConversationStatus.getStatusText(conversationStatus));
        kmResolve.setVisible(alCustomizationSettings != null && alCustomizationSettings.isAgentApp());
    }

    public String getConversationAssinee(Channel channel) {
        if (channel != null && channel.getMetadata() != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
            String assigneId = channel.getMetadata().get(Channel.CONVERSATION_ASSIGNEE);
            if (!TextUtils.isEmpty(assigneId)) {
                Contact assignee = new AppContactService(ApplozicService.getAppContext()).getContactById(assigneId);
                if (assignee != null) {
                    return assignee.getDisplayName();
                }
            }
        }
        return null;
    }

    public int getConversationStatus(Channel channel) {
        if (channel != null && channel.getMetadata() != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
            String status = channel.getMetadata().get(Channel.CONVERSATION_STATUS);
            if (status != null) {
                return Integer.parseInt(status);
            }
        }
        return -1;
    }
}
