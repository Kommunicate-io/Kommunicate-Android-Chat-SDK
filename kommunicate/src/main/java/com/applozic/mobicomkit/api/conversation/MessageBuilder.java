package com.applozic.mobicomkit.api.conversation;

import android.content.Context;

import com.applozic.mobicomkit.listners.MediaUploadProgressHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish on 20/12/17.
 */

public class MessageBuilder {

    private Message message;
    private Context context;

    public MessageBuilder(Context context) {
        this.message = new Message();
        this.context = context;
    }

    public MessageBuilder setTo(String to) {
        message.setTo(to);
        return this;
    }

    public MessageBuilder setMessage(String message) {
        this.message.setMessage(message);
        return this;
    }

    public MessageBuilder setType(Short type) {
        message.setType(type);
        return this;
    }

    public MessageBuilder setFilePath(String filePath) {
        List<String> pathList = new ArrayList<>();
        pathList.add(filePath);
        message.setFilePaths(pathList);
        return this;
    }

    public MessageBuilder setContentType(short contentType) {
        message.setContentType(contentType);
        return this;
    }

    public MessageBuilder setGroupId(Integer groupId) {
        message.setGroupId(groupId);
        return this;
    }

    public MessageBuilder setMetadata(Map<String, String> metadata) {
        message.setMetadata(metadata);
        return this;
    }

    public MessageBuilder setClientGroupId(String clientGroupId) {
        message.setClientGroupId(clientGroupId);
        return this;
    }

    public MessageBuilder setMessageObject(Message message) {
        this.message = message;
        return this;
    }

    public Message getMessageObject() {
        return message;
    }

    public void send() {
        new MobiComConversationService(context).sendMessage(message);
    }

    public void send(MediaUploadProgressHandler handler) {
        new MobiComConversationService(context).sendMessage(message, handler, MessageIntentService.class);
    }
}
