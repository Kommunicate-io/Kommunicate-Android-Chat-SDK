package io.kommunicate.users;

import android.content.Context;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;

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

    public static KMUser getLoggedInUser(Context context) {
        KMUser user = new KMUser();
        user.setRoleType(MobiComUserPreference.getInstance(context).getUserRoleType());
        user.setUserId(MobiComUserPreference.getInstance(context).getUserId());
        user.setDisplayName(MobiComUserPreference.getInstance(context).getDisplayName());
        user.setPassword(MobiComUserPreference.getInstance(context).getPassword());
        user.setEmail(MobiComUserPreference.getInstance(context).getEmailIdValue());
        user.setContactNumber(MobiComUserPreference.getInstance(context).getContactNumber());

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
