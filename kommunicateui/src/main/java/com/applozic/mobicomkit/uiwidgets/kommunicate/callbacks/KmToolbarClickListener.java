package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.app.Activity;

import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;


public interface KmToolbarClickListener {
    public void onClick(Activity activity, Channel channel, Contact contact);
}
