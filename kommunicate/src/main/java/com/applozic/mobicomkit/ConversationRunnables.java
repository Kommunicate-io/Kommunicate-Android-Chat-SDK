package com.applozic.mobicomkit;

import android.content.Context;
import android.os.Process;

import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.List;

/**
 * Created by ashish on 27/03/18.
 */

public class ConversationRunnables {

    Context context;
    private static final int PRE_FETCH_MESSAGES_FOR = 6;
    private boolean isMutedList;
    private boolean isSync;
    private boolean isMessageMetadataSync;
    private static String TAG = "ConversationSyncThread";
    private MobiComMessageService mobiComMessageService;
    private Message message;

    public ConversationRunnables(Context context, Message message, boolean isMutedList, boolean isSync, boolean isMessageMetadataSync) {
        this.context = context;
        this.isSync = isSync;
        this.isMutedList = isMutedList;
        this.isMessageMetadataSync = isMessageMetadataSync;
        this.message = message;
        mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);

        startSync();
    }

    public void startSync() {
        Thread syncThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isMutedList) {
                    Utils.printLog(context, TAG, "Muted user list sync started from thread..");
                    try {
                        UserService.getInstance(context).getMutedUserList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if (isMessageMetadataSync) {
                    Utils.printLog(context, TAG, "Syncing messages service started for metadata update from thread");
                    mobiComMessageService.syncMessageForMetadataUpdate();
                    return;
                }

                Utils.printLog(context, TAG, "Syncing messages service started from thread: " + isSync);

                if (message != null) {
                    mobiComMessageService.processInstantMessage(message);
                } else {
                    if (isSync) {
                        mobiComMessageService.syncMessages();
                    } else {
                        syncConversation();
                    }
                }
            }
        });

        syncThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        syncThread.start();
    }

    private void syncConversation() {
        try {
            MobiComConversationService mobiComConversationService = new MobiComConversationService(context);
            List<Message> messages = mobiComConversationService.getLatestMessagesGroupByPeople();
            UserService.getInstance(context).processSyncUserBlock();

            for (Message message : messages.subList(0, Math.min(PRE_FETCH_MESSAGES_FOR, messages.size()))) {
                Contact contact = null;
                Channel channel = null;

                if (message.getGroupId() != null) {
                    channel = new Channel(message.getGroupId());
                } else {
                    contact = new Contact(message.getContactIds());
                }

                mobiComConversationService.getMessagesWithNetworkMetaData(1L, null, contact, channel, null, true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
