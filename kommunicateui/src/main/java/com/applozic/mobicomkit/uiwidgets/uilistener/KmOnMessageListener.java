package com.applozic.mobicomkit.uiwidgets.uilistener;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;

public interface KmOnMessageListener {
    void onNewMessage(Message message, Channel channel, Contact contact);
}
