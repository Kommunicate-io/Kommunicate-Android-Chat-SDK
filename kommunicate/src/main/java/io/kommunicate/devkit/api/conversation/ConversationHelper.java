package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import io.kommunicate.devkit.api.MqttService;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.attachment.AttachmentManager;
import io.kommunicate.devkit.api.attachment.AttachmentTask;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.api.people.UserIntentService;
import io.kommunicate.devkit.channel.database.ChannelDatabaseService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.exception.KommunicateException;
import io.kommunicate.devkit.listners.ConversationListHandler;
import io.kommunicate.devkit.listners.MediaDownloadProgressHandler;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;
import io.kommunicate.commons.task.CoreTask;

import java.util.Iterator;
import java.util.List;

import io.kommunicate.callbacks.TaskListener;
import io.kommunicate.usecase.MessageListUseCase;

/**
 * Created by ashish on 05/01/18.
 */

public class ConversationHelper {

    private static final String MESSAGE_STATUS_TOPIC = "message-status";
    private static final String not_message_attachement = "Message does not have Attachment";
    private static final String attachment_downloaded = "Attachment for the message already downloaded";

    public static void getLatestMessageList(Context context, String searchString, boolean isScroll, TaskListener<List<Message>> handler) {
        if (!isScroll) {
            MessageListUseCase.executeWithCallback(
                    context,
                    searchString,
                    null,
                    null,
                    null,
                    null,
                    true,
                    handler
            );
        }else {
            MessageListUseCase.executeWithCallback(
                    context,
                    searchString,
                    null,
                    null,
                    MobiComUserPreference.getInstance(context).getStartTimeForPagination(),
                    null,
                    true,
                    handler
            );
        }
    }

    public static void getLatestMessageList(Context context, boolean isScroll, TaskListener<List<Message>> handler) {
        getLatestMessageList(context, null, isScroll, handler);
    }

    public static void getLatestMessageList(Context context, String searchString, Long startTime, TaskListener<List<Message>> handler) {
        MessageListUseCase.executeWithCallback(
                context,
                searchString,
                null,
                null,
                startTime,
                null,
                true,
                handler
        );
    }

    public static void getConversationList(Context context, String searchString, boolean isScroll, ConversationListHandler handler) {
        CoreTask.execute(new ConversationListTask(context,
                searchString,
                null,
                null,
                (isScroll ? MobiComUserPreference.getInstance(context).getStartTimeForPagination() : null),
                null,
                handler,
                true));
    }

    public static void getMessageListForContact(Context context, Contact contact, Long endTime, TaskListener<List<Message>> handler) {
        MessageListUseCase.executeWithCallback(
                context,
                null,
                contact,
                null,
                null,
                endTime,
                false,
                handler
        );
    }

    public static void getMessageListForChannel(Context context, Channel channel, Long endTime, TaskListener<List<Message>> handler) {
        if (channel == null) {
           handler.onFailure(new KommunicateException("Channel key cannot be null"));
           return;
        }
        MessageListUseCase.executeWithCallback(
                context,
                null,
                null,
                channel,
                null,
                endTime,
                false,
                handler
        );
    }

    public static void getMessageListForContact(Context context, String userId, Long endTime, TaskListener<List<Message>> handler) {
        if (userId == null) {
            handler.onFailure(new KommunicateException("userId cannot be null"));
            return;
        }
        MessageListUseCase.executeWithCallback(
                context,
                null,
                new AppContactService(context).getContactById(userId),
                null,
                null,
                endTime,
                false,
                handler
        );
    }

    public static void getMessageListForChannel(Context context, Integer channelKey, Long endTime, TaskListener<List<Message>> handler) {
        if (channelKey == null) {
            handler.onFailure(new KommunicateException("Channel key cannot be null"));
            return;
        }
        MessageListUseCase.executeWithCallback(
                context,
                null,
                null,
                ChannelService.getInstance(context).getChannel(channelKey),
                null,
                endTime,
                false,
                handler
        );
    }

    public static void downloadMessage(Context context, Message message, MediaDownloadProgressHandler handler) {
        KommunicateException e;
        if (message == null || handler == null) {
            return;
        }
        if (!message.hasAttachment()) {
            e = new KommunicateException(not_message_attachement);
            handler.onProgressUpdate(0, e);
            handler.onCompleted(null, e);
        } else if (message.isAttachmentDownloaded()) {
            e = new KommunicateException(attachment_downloaded);
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

    public static synchronized void addLatestConversation(Context context, Message message, List<ConversationDetails> conversationList) {
        Iterator<ConversationDetails> iterator = conversationList.iterator();
        boolean shouldAdd = false;

        while (iterator.hasNext()) {
            ConversationDetails currentMessage = iterator.next();

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

    public static synchronized void removeLatestConversation(String userId, Integer groupId, List<ConversationDetails> conversationList) {
        int index = -1;

        for (ConversationDetails message : conversationList) {
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

    public static ConversationDetails getConversationFromMessage(Context context, Message message) {
        ConversationDetails conversation = new ConversationDetails();

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
        MqttService mqttService = MqttService.getInstance(context);

        if (!TextUtils.isEmpty(pairedMessageKey) && mqttService.isConnected()) {
            mqttService.publishMessageStatus(MESSAGE_STATUS_TOPIC, MobiComUserPreference.getInstance(context).getUserId() + "," + pairedMessageKey + "," + status);
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
