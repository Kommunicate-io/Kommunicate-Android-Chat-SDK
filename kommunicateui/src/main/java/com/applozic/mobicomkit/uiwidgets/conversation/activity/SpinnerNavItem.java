package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 23/2/14.
 */
public class SpinnerNavItem {

    private String type;
    private int icon;
    private Contact contact;
    private String contactNumber;

    public SpinnerNavItem(Contact contact, String contactNumber, String type, int icon) {
        this.contact = contact;
        this.type = type;
        this.contactNumber = contactNumber;
        this.icon = icon;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getType() {
        return this.type;
    }

    public int getIcon() {
        return this.icon;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
