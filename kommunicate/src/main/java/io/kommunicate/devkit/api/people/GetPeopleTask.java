package io.kommunicate.devkit.api.people;

import android.content.Context;
import android.text.TextUtils;

import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.listners.ChannelListener;
import io.kommunicate.devkit.listners.ContactListener;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;
import io.kommunicate.commons.task.CoreAsyncTask;

public class GetPeopleTask extends CoreAsyncTask<Object, Object> {
    private String userId;
    private String clientChannelKey;
    private Integer groupId;
    private ChannelListener channelListener;
    private ContactListener contactListener;
    private ChannelService channelService;
    private AppContactService appContactService;

    public GetPeopleTask(Context context, String userId, String clientChannelKey, Integer channelKey, ChannelListener channelListener, ContactListener contactListener, AppContactService appContactService, ChannelService channelService) {
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
