package io.kommunicate.ui.kommunicate.callbacks;

import android.app.Activity;

import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;


public interface KmToolbarClickListener {
    public void onClick(Activity activity, Channel channel, Contact contact);
}
