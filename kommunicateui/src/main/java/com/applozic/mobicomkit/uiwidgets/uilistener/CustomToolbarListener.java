package com.applozic.mobicomkit.uiwidgets.uilistener;

import dev.kommunicate.commons.people.channel.Channel;
import dev.kommunicate.commons.people.contact.Contact;

public interface CustomToolbarListener {
    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void setToolbarImage(Contact contact, Channel channel);

    void hideSubtitleAndProfilePic();
}
