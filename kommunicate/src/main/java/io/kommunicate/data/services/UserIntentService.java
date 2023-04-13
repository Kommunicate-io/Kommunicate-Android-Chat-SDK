package io.kommunicate.data.services;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.AlJobIntentService;
import io.kommunicate.KommunicateService;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.account.user.UserDetail;
import io.kommunicate.data.conversation.KmConversation;
import io.kommunicate.data.conversation.Message;
import io.kommunicate.data.conversation.MessageClientService;
import io.kommunicate.data.conversation.MobiComConversationService;
import io.kommunicate.data.conversation.SyncCallService;
import io.kommunicate.data.conversation.database.MessageDatabaseService;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;

/**
 * Created by devashish on 15/12/13.
 */
public class UserIntentService extends AlJobIntentService {

    public static final String USER_ID = "userId";
    public static final String USER_LAST_SEEN_AT_STATUS = "USER_LAST_SEEN_AT_STATUS";
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    public static final String UNREAD_COUNT = "UNREAD_COUNT";
    public static final String PAIRED_MESSAGE_KEY_STRING = "PAIRED_MESSAGE_KEY_STRING";
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1100;
    private static final String TAG = "UserIntentService";
    MessageClientService messageClientService;
    MobiComConversationService mobiComConversationService;
    MessageDatabaseService messageDatabaseService;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(KommunicateService.getContext(context), UserIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        messageClientService = new MessageClientService(getApplicationContext());
        messageDatabaseService = new MessageDatabaseService(getApplicationContext());
        mobiComConversationService = new MobiComConversationService(getApplicationContext());
    }

    public void checkAndSaveLoggedUserDeletedDataToSharedPref() {
        String userId = MobiComUserPreference.getInstance(UserIntentService.this).getUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        UserDetail[] userDetails = new MessageClientService(UserIntentService.this).getUserDetails(userId);
        if (userDetails == null) {
            return;
        }

        for (UserDetail userDetail : userDetails) {
            if (userId.equals(userDetail.getUserId()) && userDetail.getDeletedAtTime() != null) {
                SyncCallService.getInstance(UserIntentService.this).processLoggedUserDelete(); //this will add a user deleted entry to MobicomUserPreferences
            }
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Integer unreadCount = intent.getIntExtra(UNREAD_COUNT, 0);
        Contact contact = (Contact) intent.getSerializableExtra(CONTACT);
        Channel channel = (Channel) intent.getSerializableExtra(CHANNEL);
        String messageKey = intent.getStringExtra(PAIRED_MESSAGE_KEY_STRING);

        if (contact != null) {
            messageDatabaseService.updateReadStatusForContact(contact.getContactIds());
        } else if (channel != null) {
            messageDatabaseService.updateReadStatusForChannel(String.valueOf(channel.getKey()));
        }

        if (unreadCount != 0 || !TextUtils.isEmpty(messageKey) && !KmConversation.isMessageStatusPublished(getApplicationContext(), messageKey, Message.Status.READ.getValue())) {
            messageClientService.updateReadStatus(contact, channel);
        } else {
            String userId = intent.getStringExtra(USER_ID);
            if (!TextUtils.isEmpty(userId)) {
                SyncCallService.getInstance(UserIntentService.this).processUserStatus(userId);
            } else if (intent.getBooleanExtra(USER_LAST_SEEN_AT_STATUS, false)) {
                mobiComConversationService.processLastSeenAtStatus();
            }
        }
    }
}
