package com.applozic.mobicomkit.uiwidgets.uilistener;

import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

public interface CustomToolbarListener {
    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void setToolbarImage(Contact contact, Channel channel);

    void hideSubtitleAndProfilePic();
}
