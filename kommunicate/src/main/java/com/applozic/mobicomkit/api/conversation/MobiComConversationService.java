package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.cache.MessageSearchCache;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.listners.MediaUploadProgressHandler;
import com.applozic.mobicomkit.sync.SyncUserDetailsResponse;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.AnnotationExclusionStrategy;
import com.applozic.mobicommons.json.ArrayAdapterFactory;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.kommunicate.KmConversationResponse;

public class MobiComConversationService {

    private static final String TAG = "Conversation";
    protected Context context = null;
    protected MessageClientService messageClientService;
    protected MessageDatabaseService messageDatabaseService;
    private BaseContactService baseContactService;
    private ConversationService conversationService;
    private ChannelService channelService;
    public static final int UPLOAD_STARTED = 1;
    public static final int UPLOAD_PROGRESS = 2;
    public static final int UPLOAD_CANCELLED = 3;
    public static final int UPLOAD_COMPLETED = 4;
    public static final int MESSAGE_SENT = 5;
    private boolean isHideActionMessage = false;


    public MobiComConversationService(Context context) {
        this.context = ApplozicService.getContext(context);
        this.messageClientService = new MessageClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.baseContactService = new AppContactService(context);
        this.conversationService = ConversationService.getInstance(context);
        this.channelService = ChannelService.getInstance(context);
        this.isHideActionMessage = ApplozicClient.getInstance(context).isActionMessagesHidden();
    }

    @VisibleForTesting
    public void setMessageClientService(MessageClientService messageClientService) {
        this.messageClientService = messageClientService;
    }

    @VisibleForTesting
    public void setMessageDatabaseService(MessageDatabaseService messageDatabaseService) {
        this.messageDatabaseService = messageDatabaseService;
    }

    @VisibleForTesting
    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @VisibleForTesting
    public void setContactService(AppContactService appContactService) {
        this.baseContactService = appContactService;
    }

    public void sendMessage(Message message) {
        sendMessage(message, null, MessageIntentService.class);
    }

    public void sendMessage(Message message, final MediaUploadProgressHandler progressHandler, Class messageIntentClass) {
        Intent intent = new Intent(context, messageIntentClass);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message message) {
                handleState(message, progressHandler);
                return true;
            }
        });

        MessageIntentService.enqueueWork(context, intent, handler);
    }

    public void sendMessage(Message message, Class messageIntentClass, String userDisplayName) {
        Intent intent = new Intent(context, messageIntentClass);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        if (!TextUtils.isEmpty(userDisplayName)) {
            intent.putExtra(MobiComKitConstants.DISPLAY_NAME, userDisplayName);
        }
        MessageIntentService.enqueueWork(context, intent, null);
    }

    public void sendMessage(Message message, Class messageIntentClass) {
        sendMessage(message, messageIntentClass, null);
    }

    public void sendMessage(Message message, MediaUploadProgressHandler handler) {
        if (message == null) {
            return;
        }

        ApplozicException e = null;

        if (!message.hasAttachment()) {
            e = new ApplozicException("Message does not have any attachment");
            if (handler != null) {
                handler.onUploadStarted(e, null);
                handler.onProgressUpdate(0, e, null);
                handler.onCancelled(e, null);
            }
        }
        sendMessage(message, handler, MessageIntentService.class);
    }

    public List<Message> getLatestMessagesGroupByPeople() {
        return getLatestMessagesGroupByPeople(null, null);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt, String searchString, Integer parentGroupKey) {

        if (!ApplozicClient.getInstance(context).wasServerCallDoneBefore(null, null, null) || createdAt != null && createdAt != 0) {
            getMessagesWithNetworkMetaData(null, createdAt, null, null, null, false, false);
        }

        return messageDatabaseService.getMessages(createdAt, searchString, parentGroupKey);
    }

    public synchronized NetworkListDecorator<Message> getLatestMessagesGroupByPeopleWithNetworkMetaData(Long createdAt, String searchString, Integer parentGroupKey) {
        boolean networkFail = false;

        if (!ApplozicClient.getInstance(context).wasServerCallDoneBefore(null, null, null) || createdAt != null && createdAt != 0) {
            NetworkListDecorator<Message> networkListDecorator = getMessagesWithNetworkMetaData(null, createdAt, null, null, null, false, false);
            networkFail = networkListDecorator.wasNetworkFail();
        }

        return new NetworkListDecorator<>(messageDatabaseService.getMessages(createdAt, searchString, parentGroupKey), networkFail);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt, String searchString) {
        return getLatestMessagesGroupByPeople(createdAt, searchString, null);
    }

    public List<Message> getMessages(String userId, Long startTime, Long endTime) {
        return getMessagesWithNetworkMetaData(startTime, endTime, new Contact(userId), null, null, false, false).getList();
    }

    public synchronized List<Message> getMessages(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId) {
        return getMessagesWithNetworkMetaData(startTime, endTime, contact, channel, conversationId, false, false).getList();
    }

    public synchronized NetworkListDecorator<Message> getMessagesForParticularThreadWithNetworkMetaData(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId, boolean isSkipRead) {
        String data = null;
        try {
            data = messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead);
            Utils.printLog(context, TAG, "Received response from server for Messages: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (data == null || TextUtils.isEmpty(data) || data.equals("UnAuthorized Access") || !data.contains("{")) {
            return new NetworkListDecorator<>(null, true);
        }

        AlConversationResponse alConversationResponse = null;
        boolean wasNetworkFail = false; //for determining network fail in case of exception
        try {
            alConversationResponse = (AlConversationResponse) GsonUtils.getObjectFromJson(messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead), AlConversationResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            wasNetworkFail = true;
        }

        if (alConversationResponse == null) {
            return new NetworkListDecorator<>(null, wasNetworkFail);
        }

        List<Message> messageList = new ArrayList<>();

        try {
            if (alConversationResponse.getUserDetails() != null) {
                MessageSearchCache.processUserDetails(alConversationResponse.getUserDetails());
            }

            if (alConversationResponse.getGroupFeeds() != null) {
                MessageSearchCache.processChannelFeeds(alConversationResponse.getGroupFeeds());
            }

            List<String> messageKeys = new ArrayList<>();

            for (Message message : alConversationResponse.getMessage()) {
                if (message.getTo() == null) {
                    continue;
                }
                if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                    setFilePathifExist(message);
                }
                if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                    FileClientService fileClientService = new FileClientService(context);
                    fileClientService.loadContactsvCard(message);
                }
                if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                    continue;
                }
                if (message.getMetadata() != null && Message.GroupMessageMetaData.TRUE.getValue().equals(message.getMetadata().get(Message.GroupMessageMetaData.HIDE_KEY.getValue()))) {
                    continue;
                }
                if (isHideActionMessage && message.isActionMessage()) {
                    message.setHidden(true);
                }
                messageList.add(message);
                if (message.getMetadata() != null && message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null) {
                    messageKeys.add(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
                }
            }

            if (alConversationResponse.getMessage() != null) {
                Collections.reverse(messageList);
                MessageSearchCache.setMessageList(messageList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NetworkListDecorator<>(messageList, wasNetworkFail);
    }

    public synchronized List<Message> getMessagesForParticularThread(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId, boolean isSkipRead) {
        String data = null;
        try {
            data = messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead);
            Utils.printLog(context, TAG, "Received response from server for Messages: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (data == null || TextUtils.isEmpty(data) || data.equals("UnAuthorized Access") || !data.contains("{")) {
            return null;
        }

        AlConversationResponse kmConversationResponse = null;
        try {
            kmConversationResponse = (AlConversationResponse) GsonUtils.getObjectFromJson(messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead), KmConversationResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (kmConversationResponse == null) {
            return null;
        }

        List<Message> messageList = new ArrayList<>();

        try {
            if (kmConversationResponse.getUserDetails() != null) {
                MessageSearchCache.processUserDetails(kmConversationResponse.getUserDetails());
            }

            if (kmConversationResponse.getGroupFeeds() != null) {
                MessageSearchCache.processChannelFeeds(kmConversationResponse.getGroupFeeds());
            }

            List<String> messageKeys = new ArrayList<>();

            for (Message message : kmConversationResponse.getMessage()) {
                if (message.getTo() == null) {
                    continue;
                }
                if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                    setFilePathifExist(message);
                }
                if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                    FileClientService fileClientService = new FileClientService(context);
                    fileClientService.loadContactsvCard(message);
                }
                if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                    continue;
                }
                if (message.getMetadata() != null && Message.GroupMessageMetaData.TRUE.getValue().equals(message.getMetadata().get(Message.GroupMessageMetaData.HIDE_KEY.getValue()))) {
                    continue;
                }
                if (isHideActionMessage && message.isActionMessage()) {
                    message.setHidden(true);
                }
                messageList.add(message);
                if (message.getMetadata() != null && message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null) {
                    messageKeys.add(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
                }
            }

            if (kmConversationResponse.getMessage() != null) {
                Collections.reverse(messageList);
                MessageSearchCache.setMessageList(messageList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }


    public synchronized List<Message> getMessages(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId, boolean isSkipRead, boolean isForSearch) {
        if (isForSearch) {
            return getMessagesForParticularThread(startTime, endTime, contact, channel, conversationId, isSkipRead);
        }
        List<Message> messageList = new ArrayList<Message>();
        List<Message> cachedMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        boolean isServerCallNotRequired = false;

        if (channel != null) {
            Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(channel.getKey());
            isServerCallNotRequired = (newChannel != null && !Channel.GroupType.OPEN.getValue().equals(newChannel.getType()));
        } else if (contact != null) {
            isServerCallNotRequired = true;
        }

        if (isServerCallNotRequired && (!cachedMessageList.isEmpty() &&
                ApplozicClient.getInstance(context).wasServerCallDoneBefore(contact, channel, conversationId)
                || (contact == null && channel == null && cachedMessageList.isEmpty() && ApplozicClient.getInstance(context).wasServerCallDoneBefore(contact, channel, conversationId)))) {
            Utils.printLog(context, TAG, "cachedMessageList size is : " + cachedMessageList.size());
            return cachedMessageList;
        }

        String data;
        try {
            data = messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead);
            Utils.printLog(context, TAG, "Received response from server for Messages: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return cachedMessageList;
        }

        if (data == null || TextUtils.isEmpty(data) || data.equals("UnAuthorized Access") || !data.contains("{")) {
            //Note: currently not supporting syncing old channel messages from server
            if (channel != null && channel.getKey() != null) {
                return cachedMessageList;
            }
            return cachedMessageList;
        }

        ApplozicClient.getInstance(context).updateServerCallDoneStatus(contact, channel, conversationId);

        try {
            Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory())
                    .setExclusionStrategies(new AnnotationExclusionStrategy()).create();
            JsonParser parser = new JsonParser();
            JSONObject jsonObject = new JSONObject(data);
            String channelFeedResponse = "";
            String conversationPxyResponse = "";
            String element = parser.parse(data).getAsJsonObject().get("message").toString();
            String userDetailsElement = parser.parse(data).getAsJsonObject().get("userDetails").toString();

            if (!TextUtils.isEmpty(userDetailsElement)) {
                UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(userDetailsElement, UserDetail[].class);
                processUserDetails(userDetails);
            }

            if (jsonObject.has("groupFeeds")) {
                channelFeedResponse = parser.parse(data).getAsJsonObject().get("groupFeeds").toString();
                ChannelFeed[] channelFeeds = (ChannelFeed[]) GsonUtils.getObjectFromJson(channelFeedResponse, ChannelFeed[].class);
                ChannelService.getInstance(context).processChannelFeedList(channelFeeds, false);
                if (channel != null && !isServerCallNotRequired) {
                    BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
                }
            }
            if (jsonObject.has("conversationPxys")) {
                conversationPxyResponse = parser.parse(data).getAsJsonObject().get("conversationPxys").toString();
                Conversation[] conversationPxy = (Conversation[]) GsonUtils.getObjectFromJson(conversationPxyResponse, Conversation[].class);
                ConversationService.getInstance(context).processConversationArray(conversationPxy, channel, contact);
            }
            Message[] messages = gson.fromJson(element, Message[].class);
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
            /*String connectedUsersResponse = parser.parse(data).getAsJsonObject().get("connectedUsers").toString();
            String[] connectedUserIds = (String[]) GsonUtils.getObjectFromJson(connectedUsersResponse, String[].class);*/

            if (messages != null && messages.length > 0 && cachedMessageList.size() > 0 && cachedMessageList.get(0).isLocalMessage()) {
                if (cachedMessageList.get(0).equals(messages[0])) {
                    Utils.printLog(context, TAG, "Both messages are same.");
                    deleteMessage(cachedMessageList.get(0));
                }
            }


            for (Message message : messages) {
                if (!message.isCall() || userPreferences.isDisplayCallRecordEnable()) {
                    //TODO: remove this check..right now in some cases it is coming as null.
                    // we have to figure out if it is a parsing problem or response from server.
                    if (message.getTo() == null) {
                        continue;
                    }

                    if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(message);
                    }
                    if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(message);
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }

                    message.setHidden(isHideActionMessage && message.isActionMessage());

                    if (messageDatabaseService.isMessagePresent(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue())) {
                        messageDatabaseService.updateMessageReplyType(message.getKeyString(), Message.ReplyMessage.NON_HIDDEN.getValue());
                    } else {
                        if (isServerCallNotRequired || contact == null && channel == null) {
                            messageDatabaseService.createMessage(message);
                        }
                    }
                    if (contact == null && channel == null) {
                        if (message.hasHideKey()) {
                            if (message.getGroupId() != null) {
                                Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                                if (newChannel != null) {
                                    getMessages(null, null, null, newChannel, null, true, isForSearch);
                                }
                            } else {
                                getMessages(null, null, new Contact(message.getContactIds()), null, null, true, isForSearch);
                            }
                        }
                    }
                }

                if (!isServerCallNotRequired && !message.isHidden()) {
                    messageList.add(message);
                }
            }
            if (contact == null && channel == null) {
                Intent intent = new Intent(MobiComKitConstants.APPLOZIC_UNREAD_COUNT);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Message> finalMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        List<String> messageKeys = new ArrayList<>();
        for (Message msg : finalMessageList) {
            if (msg.getTo() == null) {
                continue;
            }
            if (Message.MetaDataType.HIDDEN.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                continue;
            }
            if (msg.getMetadata() != null && msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null && !messageDatabaseService.isMessagePresent(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()))) {
                messageKeys.add(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
            }
        }
        if (messageKeys != null && messageKeys.size() > 0) {
            Message[] replyMessageList = getMessageListByKeyList(messageKeys);
            if (replyMessageList != null) {
                for (Message replyMessage : replyMessageList) {
                    if (replyMessage.getTo() == null) {
                        continue;
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (replyMessage.hasAttachment() && !(replyMessage.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(replyMessage);
                    }
                    if (replyMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(replyMessage);
                    }
                    replyMessage.setReplyMessage(Message.ReplyMessage.HIDE_MESSAGE.getValue());
                    if (isServerCallNotRequired || contact == null && channel == null) {
                        messageDatabaseService.createMessage(replyMessage);
                    }
                }
            }
        }

        if (messageList != null && !messageList.isEmpty()) {
            Collections.sort(messageList, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return lhs.getCreatedAtTime().compareTo(rhs.getCreatedAtTime());
                }
            });
        }

        return channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType()) ? messageList : finalMessageList;
    }

    public synchronized NetworkListDecorator<Message> getMessagesWithNetworkMetaData(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId, boolean isSkipRead, boolean isForSearch) {
        if (isForSearch) {
            return getMessagesForParticularThreadWithNetworkMetaData(startTime, endTime, contact, channel, conversationId, isSkipRead);
        }
        List<Message> messageList = new ArrayList<Message>();
        List<Message> cachedMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        boolean isServerCallNotRequired = false;

        if (channel != null) {
            Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(channel.getKey());
            isServerCallNotRequired = (newChannel != null && !Channel.GroupType.OPEN.getValue().equals(newChannel.getType()));
        } else if (contact != null) {
            isServerCallNotRequired = true;
        }

        if (isServerCallNotRequired && (!cachedMessageList.isEmpty() &&
                ApplozicClient.getInstance(context).wasServerCallDoneBefore(contact, channel, conversationId)
                || (contact == null && channel == null && cachedMessageList.isEmpty() && ApplozicClient.getInstance(context).wasServerCallDoneBefore(contact, channel, conversationId)))) {
            Utils.printLog(context, TAG, "cachedMessageList size is : " + cachedMessageList.size());
            return new NetworkListDecorator<>(cachedMessageList, false);
        }

        String data;
        try {
            data = messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead);
            Utils.printLog(context, TAG, "Received response from server for Messages: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new NetworkListDecorator<>(cachedMessageList, true);
        }

        if (data == null || TextUtils.isEmpty(data) || data.equals("UnAuthorized Access") || !data.contains("{")) {
            //Note: currently not supporting syncing old channel messages from server
            if (channel != null && channel.getKey() != null) {
                return new NetworkListDecorator<>(cachedMessageList, true);
            }
            return new NetworkListDecorator<>(cachedMessageList, true);
        }

        ApplozicClient.getInstance(context).updateServerCallDoneStatus(contact, channel, conversationId);

        boolean wasNetworkFail = false; //for the try catch

        try {
            Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory())
                    .setExclusionStrategies(new AnnotationExclusionStrategy()).create();
            JsonParser parser = new JsonParser();
            JSONObject jsonObject = new JSONObject(data);
            String channelFeedResponse = "";
            String conversationPxyResponse = "";
            String element = parser.parse(data).getAsJsonObject().get("message").toString();
            String userDetailsElement = parser.parse(data).getAsJsonObject().get("userDetails").toString();

            if (!TextUtils.isEmpty(userDetailsElement)) {
                UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(userDetailsElement, UserDetail[].class);
                processUserDetails(userDetails);
            }

            if (jsonObject.has("groupFeeds")) {
                channelFeedResponse = parser.parse(data).getAsJsonObject().get("groupFeeds").toString();
                ChannelFeed[] channelFeeds = (ChannelFeed[]) GsonUtils.getObjectFromJson(channelFeedResponse, ChannelFeed[].class);
                ChannelService.getInstance(context).processChannelFeedList(channelFeeds, false);
                if (channel != null && !isServerCallNotRequired) {
                    BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
                }
            }
            if (jsonObject.has("conversationPxys")) {
                conversationPxyResponse = parser.parse(data).getAsJsonObject().get("conversationPxys").toString();
                Conversation[] conversationPxy = (Conversation[]) GsonUtils.getObjectFromJson(conversationPxyResponse, Conversation[].class);
                ConversationService.getInstance(context).processConversationArray(conversationPxy, channel, contact);
            }
            Message[] messages = gson.fromJson(element, Message[].class);
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
            /*String connectedUsersResponse = parser.parse(data).getAsJsonObject().get("connectedUsers").toString();
            String[] connectedUserIds = (String[]) GsonUtils.getObjectFromJson(connectedUsersResponse, String[].class);*/

            if (messages != null && messages.length > 0 && cachedMessageList.size() > 0 && cachedMessageList.get(0).isLocalMessage()) {
                if (cachedMessageList.get(0).equals(messages[0])) {
                    Utils.printLog(context, TAG, "Both messages are same.");
                    deleteMessage(cachedMessageList.get(0));
                }
            }


            for (Message message : messages) {
                if (!message.isCall() || userPreferences.isDisplayCallRecordEnable()) {
                    //TODO: remove this check..right now in some cases it is coming as null.
                    // we have to figure out if it is a parsing problem or response from server.
                    if (message.getTo() == null) {
                        continue;
                    }

                    if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(message);
                    }
                    if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(message);
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }

                    message.setHidden(isHideActionMessage && message.isActionMessage());

                    if (messageDatabaseService.isMessagePresent(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue())) {
                        messageDatabaseService.updateMessageReplyType(message.getKeyString(), Message.ReplyMessage.NON_HIDDEN.getValue());
                    } else {
                        if (isServerCallNotRequired || contact == null && channel == null) {
                            messageDatabaseService.createMessage(message);
                        }
                    }
                    if (contact == null && channel == null) {
                        if (message.hasHideKey()) {
                            if (message.getGroupId() != null) {
                                Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                                if (newChannel != null) {
                                    getMessagesWithNetworkMetaData(null, null, null, newChannel, null, true, isForSearch);
                                }
                            } else {
                                getMessagesWithNetworkMetaData(null, null, new Contact(message.getContactIds()), null, null, true, isForSearch);
                            }
                        }
                    }
                }

                if (!isServerCallNotRequired && !message.isHidden()) {
                    messageList.add(message);
                }
            }
            if (contact == null && channel == null) {
                Intent intent = new Intent(MobiComKitConstants.APPLOZIC_UNREAD_COUNT);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        } catch (JsonSyntaxException | JSONException jsonException) {
            jsonException.printStackTrace();
            wasNetworkFail = true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        List<Message> finalMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        List<String> messageKeys = new ArrayList<>();
        for (Message msg : finalMessageList) {
            if (msg.getTo() == null) {
                continue;
            }
            if (Message.MetaDataType.HIDDEN.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                continue;
            }
            if (msg.getMetadata() != null && msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null && !messageDatabaseService.isMessagePresent(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()))) {
                messageKeys.add(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
            }
        }
        if (messageKeys != null && messageKeys.size() > 0) {
            Message[] replyMessageList = getMessageListByKeyList(messageKeys);
            if (replyMessageList != null) {
                for (Message replyMessage : replyMessageList) {
                    if (replyMessage.getTo() == null) {
                        continue;
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (replyMessage.hasAttachment() && !(replyMessage.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(replyMessage);
                    }
                    if (replyMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(replyMessage);
                    }
                    replyMessage.setReplyMessage(Message.ReplyMessage.HIDE_MESSAGE.getValue());
                    if (isServerCallNotRequired || contact == null && channel == null) {
                        messageDatabaseService.createMessage(replyMessage);
                    }
                }
            }
        }

        if (messageList != null && !messageList.isEmpty()) {
            Collections.sort(messageList, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return lhs.getCreatedAtTime().compareTo(rhs.getCreatedAtTime());
                }
            });
        }

        return new NetworkListDecorator<>(channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType()) ? messageList : finalMessageList, wasNetworkFail);
    }

    public List<Message> getConversationSearchList(String searchString) throws Exception {
        String response = messageClientService.getMessageSearchResult(searchString);
        ApiResponse<AlConversationResponse> apiResponse = (ApiResponse<AlConversationResponse>) GsonUtils.getObjectFromJson(response, new TypeToken<ApiResponse<AlConversationResponse>>() {
        }.getType());
        if (apiResponse != null) {
            if (apiResponse.isSuccess()) {
                processMessageSearchResult(apiResponse.getResponse());
                return Arrays.asList(apiResponse.getResponse().getMessage());
            } else if (apiResponse.getErrorResponse() != null) {
                throw new ApplozicException(GsonUtils.getJsonFromObject(apiResponse.getErrorResponse(), List.class));
            }
        }
        return null;
    }

    private void processMessageSearchResult(AlConversationResponse alConversationResponse) {
        if (alConversationResponse != null) {
            MessageSearchCache.processChannelFeeds(alConversationResponse.getGroupFeeds());
            MessageSearchCache.processUserDetails(alConversationResponse.getUserDetails());
            MessageSearchCache.setMessageList(Arrays.asList(alConversationResponse.getMessage()));
        }
    }

    public synchronized List<Message> getAlConversationList(int status, int pageSize, Long lastFetchTime, boolean makeServerCall) throws Exception {
        List<Message> conversationList = new ArrayList<>();
        List<Message> cachedConversationList = messageDatabaseService.getAlConversationList(status, lastFetchTime);

        if (!makeServerCall && !cachedConversationList.isEmpty()) {
            return cachedConversationList;
        }

        AlConversationResponse alConversationResponse = null;
        try {
            ApiResponse<AlConversationResponse> apiResponse = (ApiResponse<AlConversationResponse>) GsonUtils.getObjectFromJson(messageClientService.getAlConversationList(status, pageSize, lastFetchTime), new TypeToken<ApiResponse<AlConversationResponse>>() {
            }.getType());
            if (apiResponse != null) {
                alConversationResponse = apiResponse.getResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        if (alConversationResponse == null) {
            return null;
        }

        try {
            if (alConversationResponse.getUserDetails() != null) {
                processUserDetails(alConversationResponse.getUserDetails());
            }

            if (alConversationResponse.getGroupFeeds() != null) {
                ChannelService.getInstance(context).processChannelFeedList(alConversationResponse.getGroupFeeds(), false);
            }

            Message[] messages = alConversationResponse.getMessage();
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

            if (messages != null && messages.length > 0 && cachedConversationList.size() > 0 && cachedConversationList.get(0).isLocalMessage()) {
                if (cachedConversationList.get(0).equals(messages[0])) {
                    Utils.printLog(context, TAG, "Both messages are same.");
                    deleteMessage(cachedConversationList.get(0));
                }
            }

            for (Message message : messages) {
                if (!message.isCall() || userPreferences.isDisplayCallRecordEnable()) {
                    if (message.getTo() == null) {
                        continue;
                    }

                    if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(message);
                    }
                    if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(message);
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (isHideActionMessage && message.isActionMessage()) {
                        message.setHidden(true);
                    }
                    if (messageDatabaseService.isMessagePresent(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue())) {
                        messageDatabaseService.updateMessageReplyType(message.getKeyString(), Message.ReplyMessage.NON_HIDDEN.getValue());
                    } else {
                        messageDatabaseService.createMessage(message);
                    }

                    if (message.hasHideKey()) {
                        if (message.getGroupId() != null) {
                            Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                            if (newChannel != null) {
                                getMessagesWithNetworkMetaData(null, null, null, newChannel, null, true, false);
                            }
                        } else {
                            getMessagesWithNetworkMetaData(null, null, new Contact(message.getContactIds()), null, null, true, false);
                        }
                    }
                }
                conversationList.add(message);
            }
            Intent intent = new Intent(MobiComKitConstants.APPLOZIC_UNREAD_COUNT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        List<Message> finalMessageList = messageDatabaseService.getAlConversationList(status, lastFetchTime);
        List<String> messageKeys = new ArrayList<>();
        for (Message msg : finalMessageList) {
            if (msg.getTo() == null) {
                continue;
            }
            if (Message.MetaDataType.HIDDEN.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                continue;
            }
            if (msg.getMetadata() != null && msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null && !messageDatabaseService.isMessagePresent(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()))) {
                messageKeys.add(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
            }
        }
        if (messageKeys != null && messageKeys.size() > 0) {
            Message[] replyMessageList = getMessageListByKeyList(messageKeys);
            if (replyMessageList != null) {
                for (Message replyMessage : replyMessageList) {
                    if (replyMessage.getTo() == null) {
                        continue;
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (replyMessage.hasAttachment() && !(replyMessage.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(replyMessage);
                    }
                    if (replyMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(replyMessage);
                    }
                    replyMessage.setReplyMessage(Message.ReplyMessage.HIDE_MESSAGE.getValue());
                    messageDatabaseService.createMessage(replyMessage);
                }
            }
        }

        if (!conversationList.isEmpty()) {
            Collections.sort(conversationList, new Comparator<Message>() {
                @Override
                public int compare(Message lhs, Message rhs) {
                    return lhs.getCreatedAtTime().compareTo(rhs.getCreatedAtTime());
                }
            });
        }
        return finalMessageList;
    }

    private void processUserDetails(SyncUserDetailsResponse userDetailsResponse) {
        for (UserDetail userDetail : userDetailsResponse.getResponse()) {
            Contact newContact = baseContactService.getContactById(userDetail.getUserId());
            Contact contact = new Contact();
            contact.setUserId(userDetail.getUserId());
            contact.setContactNumber(userDetail.getPhoneNumber());
            contact.setStatus(userDetail.getStatusMessage());
            //contact.setApplicationId(); Todo: set the application id
            contact.setConnected(userDetail.isConnected());
            if (!TextUtils.isEmpty(userDetail.getDisplayName())) {
                contact.setFullName(userDetail.getDisplayName());
            }
            contact.setLastSeenAt(userDetail.getLastSeenAtTime());
            if (userDetail.getUnreadCount() != null) {
                contact.setUnreadCount(userDetail.getUnreadCount());
            }
            if (!TextUtils.isEmpty(userDetail.getImageLink())) {
                contact.setImageURL(userDetail.getImageLink());
            }
            contact.setUserTypeId(userDetail.getUserTypeId());
            contact.setDeletedAtTime(userDetail.getDeletedAtTime());
            contact.setRoleType(userDetail.getRoleType());
            contact.setMetadata(userDetail.getMetadata());
            contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
            if (newContact != null) {
                if (newContact.isConnected() != contact.isConnected()) {
                    BroadcastService.sendUpdateLastSeenAtTimeBroadcast(context, BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString(), contact.getContactIds());
                }
            }
            baseContactService.upsert(contact);
        }
        MobiComUserPreference.getInstance(context).setLastSeenAtSyncTime(userDetailsResponse.getGeneratedAt());
    }


    public Message[] getMessageListByKeyList(List<String> messageKeyList) {
        String response = messageClientService.getMessageByMessageKeys(messageKeyList);
        if (!TextUtils.isEmpty(response)) {
            JsonParser parser = new JsonParser();
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                String status = null;
                if (jsonObject.has("status")) {
                    status = jsonObject.getString("status");
                }
                if (!TextUtils.isEmpty(status) && "success".equals(status)) {
                    String responseString = jsonObject.getString("response");
                    String messageResponse = parser.parse(responseString).getAsJsonObject().get("message").toString();
                    if (!TextUtils.isEmpty(messageResponse)) {
                        return (Message[]) GsonUtils.getObjectFromJson(messageResponse, Message[].class);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setFilePathifExist(Message message) {
        FileMeta fileMeta = message.getFileMetas();
        File file = FileClientService.getFilePath(FileUtils.getName(fileMeta.getName()) + message.getCreatedAtTime() + "." + FileUtils.getFileFormat(fileMeta.getName()), context.getApplicationContext(), fileMeta.getContentType());
        if (file.exists()) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(file.getAbsolutePath());
            message.setFilePaths(arrayList);
        }
    }

    public boolean deleteMessage(Message message, Contact contact) {
        if (!message.isSentToServer()) {
            deleteMessageFromDevice(message, contact != null ? contact.getContactIds() : null);
            return true;
        }
        String response = messageClientService.deleteMessage(message, contact);
        if ("success".equals(response)) {
            deleteMessageFromDevice(message, contact != null ? contact.getContactIds() : null);
        } else {
            messageDatabaseService.updateDeleteSyncStatus(message, "1");
        }
        return true;
    }

    public boolean deleteMessage(Message message) {
        return deleteMessage(message, null);
    }

    public String deleteMessageFromDevice(Message message, String contactNumber) {
        if (message == null) {
            return null;
        }
        return messageDatabaseService.deleteMessage(message, contactNumber);
    }

    public void deleteConversationFromDevice(String contactNumber) {
        messageDatabaseService.deleteConversation(contactNumber);
    }

    public void deleteChannelConversationFromDevice(Integer channelKey) {
        messageDatabaseService.deleteChannelConversation(channelKey);
    }

    public void deleteAndBroadCast(final Contact contact, boolean deleteFromServer) {
        deleteConversationFromDevice(contact.getContactIds());
        if (deleteFromServer) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    messageClientService.deleteConversationThreadFromServer(contact);
                }
            });
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
        }
        BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), contact.getContactIds(), 0, "success");
    }

    public String deleteSync(final Contact contact, final Channel channel, Integer conversationId) {
        String response = "";
        if (contact != null || channel != null) {
            response = messageClientService.syncDeleteConversationThreadFromServer(contact, channel);
        }

        if (!TextUtils.isEmpty(response) && "success".equals(response)) {
            if (contact != null) {
                messageDatabaseService.deleteConversation(contact.getContactIds());
                if (conversationId != null && conversationId != 0) {
                    conversationService.deleteConversation(contact.getContactIds());
                }
            } else {
                messageDatabaseService.deleteChannelConversation(channel.getKey());
            }

        }
        BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(),
                contact != null ? contact.getContactIds() : null, channel != null ? channel.getKey() : null, response);
        return response;
    }

    public String deleteMessageFromDevice(String keyString, String contactNumber) {
        return deleteMessageFromDevice(messageDatabaseService.getMessage(keyString), contactNumber);
    }

    public synchronized void processLastSeenAtStatus() {
        try {
            SyncUserDetailsResponse userDetailsResponse = messageClientService.getUserDetailsList(MobiComUserPreference.getInstance(context).getLastSeenAtSyncTime());
            if (userDetailsResponse != null && userDetailsResponse.getResponse() != null && "success".equals(userDetailsResponse.getStatus())) {
                processUserDetails(userDetailsResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processUserDetails(UserDetail[] userDetails) {
        if (userDetails != null && userDetails.length > 0) {
            for (UserDetail userDetail : userDetails) {
                Contact contact = new Contact();
                contact.setUserId(userDetail.getUserId());
                contact.setContactNumber(userDetail.getPhoneNumber());
                contact.setConnected(userDetail.isConnected());
                if (!TextUtils.isEmpty(userDetail.getDisplayName())) {
                    contact.setFullName(userDetail.getDisplayName());
                }
                contact.setLastSeenAt(userDetail.getLastSeenAtTime());
                contact.setStatus(userDetail.getStatusMessage());
                contact.setUnreadCount(userDetail.getUnreadCount());
                contact.setUserTypeId(userDetail.getUserTypeId());
                contact.setImageURL(userDetail.getImageLink());
                contact.setDeletedAtTime(userDetail.getDeletedAtTime());
                contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
                contact.setMetadata(userDetail.getMetadata());
                contact.setRoleType(userDetail.getRoleType());
                baseContactService.upsert(contact);
            }
        }
    }

    public String getConversationIdString(Integer conversationId) {
        return BroadcastService.isContextBasedChatEnabled() && conversationId != null && conversationId != 0 ? "_" + conversationId : "";
    }

    public String reportMessage(String messageKey) {
        return messageClientService.reportMessage(messageKey);
    }

    public void read(Contact contact, Channel channel) {
        try {
            int unreadCount = 0;
            if (contact != null) {
                unreadCount = contact.getUnreadCount();
            } else if (channel != null) {
                unreadCount = channel.getUnreadCount();
            }

            Intent intent = new Intent(context, UserIntentService.class);
            intent.putExtra(UserIntentService.CONTACT, contact);
            intent.putExtra(UserIntentService.CHANNEL, channel);
            intent.putExtra(UserIntentService.UNREAD_COUNT, unreadCount);
            UserIntentService.enqueueWork(context, intent);
        } catch (Exception e) {
        }
    }

    private void handleState(android.os.Message message, MediaUploadProgressHandler progressHandler) {
        if (message != null) {
            Bundle bundle = message.getData();
            String e = null;
            String oldMessageKey = null;
            if (bundle != null) {
                e = bundle.getString("error");
                oldMessageKey = bundle.getString(MobiComKitConstants.OLD_MESSAGE_KEY_INTENT_EXTRA);
            }

            switch (message.what) {
                case UPLOAD_STARTED:
                    if (progressHandler != null) {
                        progressHandler.onUploadStarted(e != null ? new ApplozicException(e) : null, oldMessageKey);
                    }
                    break;

                case UPLOAD_PROGRESS:
                    if (progressHandler != null) {
                        progressHandler.onProgressUpdate(message.arg1, e != null ? new ApplozicException(e) : null, oldMessageKey);
                    }
                    break;

                case UPLOAD_COMPLETED:
                    if (progressHandler != null) {
                        progressHandler.onCompleted(e != null ? new ApplozicException(e) : null, oldMessageKey);
                    }
                    break;

                case UPLOAD_CANCELLED:
                    if (progressHandler != null) {
                        progressHandler.onCancelled(e != null ? new ApplozicException(e) : null, oldMessageKey);
                    }
                    break;

                case MESSAGE_SENT:
                    if (bundle != null) {
                        if (progressHandler != null) {
                            Message messageObject = messageDatabaseService.getMessage(bundle.getString(MobiComKitConstants.MESSAGE_INTENT_EXTRA));
                            String messageJson = bundle.getString(MobiComKitConstants.MESSAGE_JSON_INTENT_EXTRA);
                            if (messageObject == null) {
                                messageObject = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
                            }
                            progressHandler.onSent(messageObject, oldMessageKey);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Composition of a link list and network request status boolean that is used to store data retrieved from a network.
     *
     * <p>This class can be helpful when you need meta-data regarding the status of the network request that was used to retrieve the data.
     * This information can be used for UI elements etc.</p>
     *
     * @param <E> data type for liked list
     */
    public static class NetworkListDecorator<E> {
        private List<E> list;
        private boolean wasNetworkFail;

        public NetworkListDecorator(List<E> list, boolean wasNetworkFail) {
            this.list = list;
            this.wasNetworkFail = wasNetworkFail;
        }

        public List<E> getList() {
            return list;
        }

        public void setList(List<E> list) {
            this.list = list;
        }

        public boolean wasNetworkFail() {
            return wasNetworkFail;
        }

        public void setNetworkFail(boolean wasNetworkFail) {
            this.wasNetworkFail = wasNetworkFail;
        }
    }
}