package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.app.Activity;

import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;


public interface KmToolbarClickListener {
    public void onClick(Activity activity, Channel channel, Contact contact);
}
