package com.applozic.mobicomkit.api.people;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.AlChannelListener;
import com.applozic.mobicomkit.listners.AlContactListener;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;

public class AlGetPeopleTask extends AlAsyncTask<Object, Object> {
    private String userId;
    private String clientChannelKey;
    private Integer groupId;
    private AlChannelListener channelListener;
    private AlContactListener contactListener;
    private ChannelService channelService;
    private AppContactService appContactService;

    public AlGetPeopleTask(Context context, String userId, String clientChannelKey, Integer channelKey, AlChannelListener channelListener, AlContactListener contactListener, AppContactService appContactService, ChannelService channelService) {
        this.userId = userId;
        this.clientChannelKey = clientChannelKey;
        this.groupId = channelKey;
        this.channelListener = channelListener;
        this.contactListener = contactListener;
        this.appContactService = appContactService;

        if (appContactService == null) {
            this.appContactService = new AppContactService(context);
        } else {
            this.appContactService = appContactService;
        }

        if (channelService == null) {
            this.channelService = ChannelService.getInstance(context);
        } else {
            this.channelService = channelService;
        }
    }


    @Override
    protected Object doInBackground() {
        try {
            if (!TextUtils.isEmpty(userId)) {
                return appContactService.getContactById(userId);
            }

            if (!TextUtils.isEmpty(clientChannelKey)) {
                return channelService.getChannelByClientGroupId(clientChannelKey);
            }

            if (groupId != null && groupId > 0) {
                return channelService.getChannelByChannelKey(groupId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (o instanceof Contact && contactListener != null) {
            contactListener.onGetContact((Contact) o);
        }
        if (o instanceof Channel && channelListener != null) {
            channelListener.onGetChannel((Channel) o);
        }
    }
}
