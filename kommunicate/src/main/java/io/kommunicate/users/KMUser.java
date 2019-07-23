package io.kommunicate.users;

import android.content.Context;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by ashish on 23/01/18.
 */

public class KMUser extends User {

    private String userName;
    private String applicationName;
    private boolean chatNotificationMailSent = true;

    public static boolean isLoggedIn(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    public KMUser() {
        setSkipDeletedGroups(true);
    }

    public KMUser(boolean skipDeletedGroups) {
        setSkipDeletedGroups(skipDeletedGroups);
    }

    public static KMUser getLoggedInUser(Context context) {
        KMUser user = new KMUser();
        String userId = MobiComUserPreference.getInstance(context).getUserId();
        user.setUserId(userId);
        user.setPassword(MobiComUserPreference.getInstance(context).getPassword());

        Contact contact = new AppContactService(context).getContactById(userId);
        if (contact != null) {
            user.setRoleType(contact.getRoleType());
            user.setUserId(contact.getUserId());
            user.setDisplayName(contact.getDisplayName());
            user.setPassword(MobiComUserPreference.getInstance(context).getPassword());
            user.setEmail(contact.getEmailId());
            user.setContactNumber(contact.getContactNumber());
            user.setImageLink(contact.getImageURL());
        }

        return user;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        setUserId(userName);
    }

    public boolean isChatNotificationMailSent() {
        return chatNotificationMailSent;
    }

    public void setChatNotificationMailSent(boolean chatNotificationMailSent) {
        this.chatNotificationMailSent = chatNotificationMailSent;
    }

    @Override
    public String toString() {
        return "KMUser{" +
                "  userId : " + getUserId() +
                ", Role Type : " + getRoleType() +
                ", Role Name : " + getRoleName() +
                ", Email id : " + getEmail() +
                ", Contact number : " + getContactNumber() +
                ", Display name : " + getDisplayName() +
                "}";
    }
}
