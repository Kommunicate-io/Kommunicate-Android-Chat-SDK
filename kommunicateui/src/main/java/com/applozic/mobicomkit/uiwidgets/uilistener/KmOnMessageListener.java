package com.applozic.mobicomkit.uiwidgets.uilistener;

import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.commons.people.channel.Channel;
import dev.kommunicate.commons.people.contact.Contact;

public interface KmOnMessageListener {
    void onNewMessage(Message message, Channel channel, Contact contact);
}
