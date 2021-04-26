package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentTask;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.listners.ConversationListHandler;
import com.applozic.mobicomkit.listners.MediaDownloadProgressHandler;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlTask;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ashish on 05/01/18.
 */

public class ApplozicConversation {

    private static final String MESSAGE_STATUS_TOPIC = "message-status";

    public static void getLatestMessageList(Context context, String searchString, boolean isScroll, MessageListHandler handler) {
        if (!isScroll) {
            AlTask.execute(new MessageListTask(context, searchString, null, null, null, null, handler, true));
        } else {
            AlTask.execute(new MessageListTask(context, searchString, null, null, MobiComUserPreference.getInstance(context).getStartTimeForPagination(), null, handler, true));
        }
    }

    public static void getLatestMessageList(Context context, boolean isScroll, MessageListHandler handler) {
        getLatestMessageList(context, null, isScroll, handler);
    }

    public static void getLatestMessageList(Context context, String searchString, Long startTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, searchString, null, null, startTime, null, handler, true));
    }

    public static void getConversationList(Context context, String searchString, boolean isScroll, ConversationListHandler handler) {
        AlTask.execute(new ConversationListTask(context,
                searchString,
                null,
                null,
                (isScroll ? MobiComUserPreference.getInstance(context).getStartTimeForPagination() : null),
                null,
                handler,
                true));
    }

    public static void getMessageListForContact(Context context, Contact contact, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, contact, null, null, endTime, handler, false));
    }

    public static void getMessageListForChannel(Context context, Channel channel, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, channel, null, endTime, handler, false));
    }

    public static void getMessageListForContact(Context context, String userId, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, new AppContactService(context).getContactById(userId), null, null, endTime, handler, false));
    }

    public static void getMessageListForChannel(Context context, Integer channelKey, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, ChannelService.getInstance(context).getChannel(channelKey), null, endTime, handler, false));
    }

    public static void downloadMessage(Context context, Message message, MediaDownloadProgressHandler handler) {
        ApplozicException e;
        if (message == null || handler == null) {
            return;
        }
        if (!message.hasAttachment()) {
            e = new ApplozicException("Message does not have Attachment");
            handler.onProgressUpdate(0, e);
            handler.onCompleted(null, e);
        } else if (message.isAttachmentDownloaded()) {
            e = new ApplozicException("Attachment for the message already downloaded");
            handler.onProgressUpdate(0, e);
            handler.onCompleted(null, e);
        } else {
            AttachmentTask mDownloadThread = null;
            if (!AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                // Starts downloading this View, using the current cache setting
                mDownloadThread = AttachmentManager.startDownload(null, true, message, handler, context);
                // After successfully downloading the image, this marks that it's available.
            }
            if (mDownloadThread == null) {
                mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
                if (mDownloadThread != null) {
                    mDownloadThread.setAttachment(message, handler, context);
                }
            }
        }
    }

    public static synchronized void addLatestMessage(Message message, List<Message> messageList) {
        Iterator<Message> iterator = messageList.iterator();
        boolean shouldAdd = false;

        while (iterator.hasNext()) {
            Message currentMessage = iterator.next();

            if ((message.getGroupId() != null && currentMessage.getGroupId() != null && message.getGroupId().equals(currentMessage.getGroupId())) ||
                    (message.getGroupId() == null && currentMessage.getGroupId() == null && message.getContactIds() != null && currentMessage.getContactIds() != null &&
                            message.getContactIds().equals(currentMessage.getContactIds()))) {
                //do nothing
            } else {
                currentMessage = null;
            }

            if (currentMessage != null) {
                if (message.getCreatedAtTime() >= currentMessage.getCreatedAtTime()) {
                    iterator.remove();
                } else {
                    return;
                }
            }

            shouldAdd = true;
        }

        if (shouldAdd) {
            messageList.add(0, message);
        }
    }

    public static synchronized void addLatestConversation(Context context, Message message, List<AlConversation> conversationList) {
        Iterator<AlConversation> iterator = conversationList.iterator();
        boolean shouldAdd = false;

        while (iterator.hasNext()) {
            AlConversation currentMessage = iterator.next();

            if ((message.getGroupId() != null && currentMessage.getMessage().getGroupId() != null && message.getGroupId().equals(currentMessage.getMessage().getGroupId())) ||
                    (message.getGroupId() == null && currentMessage.getMessage().getGroupId() == null && message.getContactIds() != null && currentMessage.getMessage().getContactIds() != null &&
                            message.getContactIds().equals(currentMessage.getMessage().getContactIds()))) {
                //do nothing
            } else {
                currentMessage = null;
            }

            if (currentMessage != null) {
                if (message.getCreatedAtTime() >= currentMessage.getMessage().getCreatedAtTime()) {
                    iterator.remove();
                } else {
                    return;
                }
            }

            shouldAdd = true;
        }

        if (shouldAdd) {
            conversationList.add(0, getConversationFromMessage(context, message));
        }
    }

    public static synchronized void removeLatestConversation(String userId, Integer groupId, List<AlConversation> conversationList) {
        int index = -1;

        for (AlConversation message : conversationList) {
            if (message.getMessage().getGroupId() != null) {
                if (message.getMessage().getGroupId() != 0 && message.getMessage().getGroupId().equals(groupId)) {
                    index = conversationList.indexOf(message);
                }
            } else if (message.getMessage().getContactIds() != null && message.getMessage().getContactIds().equals(userId)) {
                index = conversationList.indexOf(message);
            }
        }
        if (index != -1) {
            conversationList.remove(index);
        }
    }

    public static AlConversation getConversationFromMessage(Context context, Message message) {
        AlConversation conversation = new AlConversation();

        conversation.setMessage(message);

        if (message.getGroupId() == null || message.getGroupId() == 0) {
            conversation.setContact(new AppContactService(context).getContactById(message.getContactIds()));
            conversation.setChannel(null);
            conversation.setUnreadCount(new MessageDatabaseService(context).getUnreadMessageCountForContact(message.getContactIds()));
        } else {
            conversation.setChannel(ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId()));
            conversation.setContact(null);
            conversation.setUnreadCount(new MessageDatabaseService(context).getUnreadMessageCountForChannel(message.getGroupId()));
        }

        return conversation;
    }

    public static synchronized void removeLatestMessage(String userId, Integer groupId, List<Message> messageList) {
        Message tempMessage = null;

        for (Message message : messageList) {
            if (message.getGroupId() != null) {
                if (message.getGroupId() != 0 && message.getGroupId().equals(groupId)) {
                    tempMessage = message;
                }
            } else if (message.getContactIds() != null && message.getContactIds().equals(userId)) {
                tempMessage = message;
            }
        }
        if (tempMessage != null) {
            messageList.remove(tempMessage);
        }
    }

    public static boolean isMessageStatusPublished(Context context, String pairedMessageKey, Short status) {
        ApplozicMqttService applozicMqttService = ApplozicMqttService.getInstance(context);

        if (!TextUtils.isEmpty(pairedMessageKey) && applozicMqttService.isConnected()) {
            applozicMqttService.publishMessageStatus(MESSAGE_STATUS_TOPIC, MobiComUserPreference.getInstance(context).getUserId() + "," + pairedMessageKey + "," + status);
            return true;
        }
        return false;
    }

    public static void markAsRead(Context context, String pairedMessageKey, String userId, Integer groupId) {
        try {
            int unreadCount = 0;
            Contact contact = null;
            Channel channel = null;
            if (userId != null) {
                contact = new AppContactService(context).getContactById(userId);
                unreadCount = contact.getUnreadCount();
                new MessageDatabaseService(context).updateReadStatusForContact(userId);
            } else if (groupId != null && groupId != 0) {
                channel = ChannelService.getInstance(context).getChannelByChannelKey(groupId);
                unreadCount = channel.getUnreadCount();
                new MessageDatabaseService(context).updateReadStatusForChannel(String.valueOf(groupId));
            }

            Intent intent = new Intent(context, UserIntentService.class);
            intent.putExtra(UserIntentService.CONTACT, contact);
            intent.putExtra(UserIntentService.CHANNEL, channel);
            intent.putExtra(UserIntentService.UNREAD_COUNT, unreadCount);
            if (!TextUtils.isEmpty(pairedMessageKey)) {
                intent.putExtra(UserIntentService.PAIRED_MESSAGE_KEY_STRING, pairedMessageKey);
            }
            UserIntentService.enqueueWork(context, intent);
        } catch (Exception e) {
        }
    }
}
