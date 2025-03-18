package com.applozic.mobicomkit.uiwidgets.uilistener;

import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

public interface CustomToolbarListener {
    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void setToolbarImage(Contact contact, Channel channel);

    void hideSubtitleAndProfilePic();
}
