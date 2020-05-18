package com.applozic.mobicomkit.uiwidgets.uilistener;

import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

public interface KmFragmentGetter {

    ConversationFragment getConversationFragment(Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString);
}
