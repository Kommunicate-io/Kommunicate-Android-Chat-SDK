package io.kommunicate.uiwidgets.uilistener;

import io.kommunicate.uiwidgets.conversation.fragment.ConversationFragment;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;

public interface KmFragmentGetter {

    ConversationFragment getConversationFragment(Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString);
}
