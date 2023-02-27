package io.kommunicate.uiwidgets.uilistener;

import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;

public interface CustomToolbarListener {
    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void setToolbarImage(Contact contact, Channel channel);

    void hideSubtitleAndProfilePic();
}
