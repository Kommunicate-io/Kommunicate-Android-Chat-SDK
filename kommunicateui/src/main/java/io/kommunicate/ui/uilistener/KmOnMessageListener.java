package io.kommunicate.ui.uilistener;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

public interface KmOnMessageListener {
    void onNewMessage(Message message, Channel channel, Contact contact);
}
