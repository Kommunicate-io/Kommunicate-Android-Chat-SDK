package com.applozic.mobicomkit.uiwidgets.uilistener;

import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import dev.kommunicate.commons.people.channel.Channel;
import dev.kommunicate.commons.people.contact.Contact;

public interface KmFragmentGetter {

    ConversationFragment getConversationFragment(Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString);
}
