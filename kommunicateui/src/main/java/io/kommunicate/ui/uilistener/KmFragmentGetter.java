package io.kommunicate.ui.uilistener;

import io.kommunicate.ui.conversation.fragment.ConversationFragment;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

public interface KmFragmentGetter {

    ConversationFragment getConversationFragment(Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString);
}
