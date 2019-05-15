package com.applozic.mobicomkit.uiwidgets.uilistener;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

public interface KmOnMessageListener {
    void onNewMessage(Message message, Channel channel, Contact contact);
}
