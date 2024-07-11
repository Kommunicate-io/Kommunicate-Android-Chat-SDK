package com.applozic.mobicomkit.api.people;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.applozic.mobicommons.people.contact.Contact;

import java.util.List;

public class ContactList {

    @Expose
    @SerializedName("contactList")
    private List<Contact> contacts;

    public ContactList() {

    }

    public ContactList(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
