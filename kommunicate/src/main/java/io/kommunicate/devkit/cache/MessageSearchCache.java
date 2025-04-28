package io.kommunicate.devkit.cache;

import android.text.TextUtils;
import android.util.SparseArray;

import io.kommunicate.devkit.api.account.user.UserDetail;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ChannelFeed;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//This is a temporary static data storage class.
//Replace this with LRU cache implementation in future
public class MessageSearchCache {

    private static SparseArray<Channel> channelSparseArray;
    private static Map<String, Contact> contactMap;
    private static List<Message> messageList;

    public static Channel getChannelByKey(Integer channelKey) {
        if (channelSparseArray != null) {
            return channelSparseArray.get(channelKey);
        }
        return null;
    }

    public static Contact getContactById(String userId) {
        if (contactMap != null) {
            return contactMap.get(userId);
        }
        return null;
    }

    public static List<Message> getMessageList() {
        return messageList;
    }

    public static void setMessageList(List<Message> messageList) {
        MessageSearchCache.messageList = messageList;
    }

    public static void processChannelFeeds(ChannelFeed[] channelFeeds) {
        if (channelFeeds != null) {
            if (channelSparseArray == null) {
                channelSparseArray = new SparseArray<>();
            }
            ChannelService channelService = ChannelService.getInstance(null);
            for (ChannelFeed channelFeed : channelFeeds) {
                channelSparseArray.append(channelFeed.getId(), channelService.getChannel(channelFeed));
            }
        }
    }

    public static void processUserDetails(UserDetail[] userDetails) {
        if (userDetails != null) {
            if (contactMap == null) {
                contactMap = new HashMap<>();
            }
            for (UserDetail userDetail : userDetails) {
                Contact contact = new Contact();
                contact.setUserId(userDetail.getUserId());
                contact.setContactNumber(userDetail.getPhoneNumber());
                contact.setConnected(userDetail.isConnected());
                contact.setStatus(userDetail.getStatusMessage());
                if (!TextUtils.isEmpty(userDetail.getDisplayName())) {
                    contact.setFullName(userDetail.getDisplayName());
                }
                contact.setLastSeenAt(userDetail.getLastSeenAtTime());
                contact.setUserTypeId(userDetail.getUserTypeId());
                contact.setUnreadCount(0);
                contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
                contact.setMetadata(userDetail.getMetadata());
                contact.setRoleType(userDetail.getRoleType());
                contact.setDeletedAtTime(userDetail.getDeletedAtTime());
                contact.setEmailId(userDetail.getEmailId());
                if (!TextUtils.isEmpty(userDetail.getImageLink())) {
                    contact.setImageURL(userDetail.getImageLink());
                }
                contact.setContactType(Contact.ContactType.APPLOZIC.getValue());
                contactMap.put(userDetail.getUserId(), contact);
            }
        }
    }
}
