package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.app.Activity;

import dev.kommunicate.commons.people.channel.Channel;
import dev.kommunicate.commons.people.contact.Contact;


public interface KmToolbarClickListener {
    public void onClick(Activity activity, Channel channel, Contact contact);
}
