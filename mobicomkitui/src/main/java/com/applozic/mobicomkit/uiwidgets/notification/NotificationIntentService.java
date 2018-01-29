package com.applozic.mobicomkit.uiwidgets.notification;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;


public class NotificationIntentService extends JobIntentService {
    public static final String ACTION_AL_NOTIFICATION = "com.applozic.mobicomkit.api.notification.action.NOTIFICATION";

    AppContactService appContactService;
    MessageDatabaseService messageDatabaseService;
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1011;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(context, NotificationIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContactService = new AppContactService(NotificationIntentService.this);
        messageDatabaseService = new MessageDatabaseService(NotificationIntentService.this);

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_AL_NOTIFICATION.equals(action)) {
                String messageKey = intent.getStringExtra(MobiComKitConstants.AL_MESSAGE_KEY);
                Message message = messageDatabaseService.getMessage(messageKey);
                if (message != null) {
                    int notificationId = Utils.getLauncherIcon(getApplicationContext());
                    final NotificationService notificationService =
                            new NotificationService(notificationId == 0 ? R.drawable.mobicom_ic_launcher : notificationId, NotificationIntentService.this, R.string.wearable_action_label, R.string.wearable_action_title, R.drawable.mobicom_ic_action_send);

                    if (MobiComUserPreference.getInstance(NotificationIntentService.this).isLoggedIn()) {
                        Channel channel = ChannelService.getInstance(NotificationIntentService.this).getChannelInfo(message.getGroupId());
                        Contact contact = null;
                        if (message.getConversationId() != null) {
                            ConversationService.getInstance(NotificationIntentService.this).getConversation(message.getConversationId());
                        }
                        if (message.getGroupId() == null) {
                            contact = appContactService.getContactById(message.getContactIds());
                        }
                        if (ApplozicClient.getInstance(NotificationIntentService.this).isNotificationStacking()) {
                            notificationService.notifyUser(contact, channel, message);
                        } else {
                            notificationService.notifyUserForNormalMessage(contact, channel, message);
                        }
                    }
                }
            }

        }
    }
}
