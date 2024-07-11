package com.applozic.mobicommons.people.channel;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by devashish on 17/12/14.
 */
public class ChannelUtils {

    public static String getChannelTitleName(Channel channel, String loggedInUserId) {

        if (!TextUtils.isEmpty(loggedInUserId)) {
            if (Channel.GroupType.SELLER.getValue().equals(channel.getType())) {
                String[] userIdSplit = new String[1];
                if (!TextUtils.isEmpty(channel.getName())) {
                    userIdSplit = channel.getName().split(":");
                }
                if (loggedInUserId.equals(channel.getAdminKey())) {
                    return channel.getName();
                } else {
                    return userIdSplit[0];
                }
            } else {
                return channel.getName();
            }
        }
        return "";
    }

    public static boolean isAdminUserId(String userId, Channel channel) {
        if (channel != null && !TextUtils.isEmpty(channel.getAdminKey()) && !TextUtils.isEmpty(userId)) {
            return channel.getAdminKey().equals(userId);
        }
        return false;
    }

    public static String getWithUserId(Channel channel, String loggedInUserId) {
        try {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String[] userIdSplit = new String[2];
                if (!TextUtils.isEmpty(channel.getClientGroupId())) {
                    userIdSplit = channel.getClientGroupId().split(":");
                    String userId1 = userIdSplit[1];
                    String userId2 = userIdSplit[2];
                    if (!loggedInUserId.equals(userId2)) {
                        return userId2;
                    } else if (!loggedInUserId.equals(userId1)) {
                        return userId1;
                    }
                }
            }
        } catch (Exception e) {
            Log.i("ChannelUtils", "Got exception in Group of two");
        }
        return "";
    }

}
