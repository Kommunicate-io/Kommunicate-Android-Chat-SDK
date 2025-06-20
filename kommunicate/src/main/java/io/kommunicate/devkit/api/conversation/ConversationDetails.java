package io.kommunicate.devkit.api.conversation;

import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

public class ConversationDetails {
    private Channel channel;
    private Contact contact;
    private Message message;
    private int unreadCount;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
