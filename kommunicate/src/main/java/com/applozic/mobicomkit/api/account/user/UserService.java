package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.notification.MuteUserResponse;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.feed.SyncBlockUserApiResponse;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicomkit.sync.SyncUserBlockFeed;
import com.applozic.mobicomkit.sync.SyncUserBlockListFeed;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sunil on 17/3/16.
 */
public class UserService {

    private static final String TAG = "UserService";
    private static UserService userService;
    Context context;
    UserClientService userClientService;
    BaseContactService baseContactService;
    private MobiComUserPreference userPreference;

    private UserService(Context context) {
        this.context = ApplozicService.getContext(context);
        userClientService = new UserClientService(context);
        userPreference = MobiComUserPreference.getInstance(context);
        baseContactService = new AppContactService(context);
    }

    public static UserService getInstance(Context context) {
        if (userService == null) {
            userService = new UserService(ApplozicService.getContext(context));
        }
        return userService;
    }

    public synchronized void processSyncUserBlock() {
        try {
            SyncBlockUserApiResponse apiResponse = userClientService.getSyncUserBlockList(userPreference.getUserBlockSyncTime());
            if (apiResponse != null && SyncBlockUserApiResponse.SUCCESS.equals(apiResponse.getStatus())) {
                SyncUserBlockListFeed syncUserBlockListFeed = apiResponse.getResponse();
                if (syncUserBlockListFeed != null) {
                    List<SyncUserBlockFeed> blockedToUserList = syncUserBlockListFeed.getBlockedToUserList();
                    List<SyncUserBlockFeed> blockedByUserList = syncUserBlockListFeed.getBlockedByUserList();
                    if (blockedToUserList != null && blockedToUserList.size() > 0) {
                        for (SyncUserBlockFeed syncUserBlockedFeed : blockedToUserList) {
                            Contact contact = new Contact();
                            if (syncUserBlockedFeed.getUserBlocked() != null && !TextUtils.isEmpty(syncUserBlockedFeed.getBlockedTo())) {
                                if (baseContactService.isContactExists(syncUserBlockedFeed.getBlockedTo())) {
                                    baseContactService.updateUserBlocked(syncUserBlockedFeed.getBlockedTo(), syncUserBlockedFeed.getUserBlocked());
                                } else {
                                    contact.setBlocked(syncUserBlockedFeed.getUserBlocked());
                                    contact.setUserId(syncUserBlockedFeed.getBlockedTo());
                                    baseContactService.upsert(contact);
                                    baseContactService.updateUserBlocked(syncUserBlockedFeed.getBlockedTo(), syncUserBlockedFeed.getUserBlocked());
                                }
                            }
                        }
                    }
                    if (blockedByUserList != null && blockedByUserList.size() > 0) {
                        for (SyncUserBlockFeed syncUserBlockByFeed : blockedByUserList) {
                            Contact contact = new Contact();
                            if (syncUserBlockByFeed.getUserBlocked() != null && !TextUtils.isEmpty(syncUserBlockByFeed.getBlockedBy())) {
                                if (baseContactService.isContactExists(syncUserBlockByFeed.getBlockedBy())) {
                                    baseContactService.updateUserBlockedBy(syncUserBlockByFeed.getBlockedBy(), syncUserBlockByFeed.getUserBlocked());
                                } else {
                                    contact.setBlockedBy(syncUserBlockByFeed.getUserBlocked());
                                    contact.setUserId(syncUserBlockByFeed.getBlockedBy());
                                    baseContactService.upsert(contact);
                                    baseContactService.updateUserBlockedBy(syncUserBlockByFeed.getBlockedBy(), syncUserBlockByFeed.getUserBlocked());
                                }
                            }
                        }
                    }
                }
                userPreference.setUserBlockSyncTime(apiResponse.getGeneratedAt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ApiResponse processUserBlock(String userId, boolean block) {
        ApiResponse apiResponse = userClientService.userBlock(userId, block);
        if (apiResponse != null && apiResponse.isSuccess()) {
            baseContactService.updateUserBlocked(userId, block);
            return apiResponse;
        }
        return null;
    }

    public synchronized void processUserDetail(Set<UserDetail> userDetails) {
        if (userDetails != null && userDetails.size() > 0) {
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    public synchronized void processUserDetails(String userId) {
        Set<String> userIds = new HashSet<String>();
        userIds.add(userId);
        processUserDetails(userIds);
    }

    public synchronized void processUserDetails(Set<String> userIds) {
        String response = userClientService.getUserDetails(userIds);
        if (!TextUtils.isEmpty(response)) {
            UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(response, UserDetail[].class);
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    public synchronized void processUser(UserDetail userDetail) {
        processUser(userDetail, Contact.ContactType.APPLOZIC);
    }

    public synchronized Contact getContactFromUserDetail(UserDetail userDetail) {
        return getContactFromUserDetail(userDetail, Contact.ContactType.APPLOZIC);
    }

    public synchronized Contact getContactFromUserDetail(UserDetail userDetail, Contact.ContactType contactType) {
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
        contact.setContactType(contactType.getValue());
        baseContactService.upsert(contact);
        return contact;
    }

    public synchronized void processUser(UserDetail userDetail, Contact.ContactType contactType) {
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
        contact.setContactType(contactType.getValue());
        baseContactService.upsert(contact);
    }

    public synchronized void processMuteUserResponse(MuteUserResponse response) {
        Contact contact = new Contact();
        contact.setUserId(response.getUserId());
        BroadcastService.sendMuteUserBroadcast(context, BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString(), true, response.getUserId());
        if (!TextUtils.isEmpty(response.getImageLink())) {
            contact.setImageURL(response.getImageLink());
        }
        contact.setUnreadCount(response.getUnreadCount());
        if (response.getNotificationAfterTime() != null && response.getNotificationAfterTime() != 0) {
            contact.setNotificationAfterTime(response.getNotificationAfterTime());
        }
        contact.setConnected(response.isConnected());
        baseContactService.upsert(contact);
    }

    public synchronized String[] getOnlineUsers(int numberOfUser) {
        try {
            Map<String, String> userMapList = userClientService.getOnlineUserList(numberOfUser);
            if (userMapList != null && userMapList.size() > 0) {
                String[] userIdArray = new String[userMapList.size()];
                Set<String> userIds = new HashSet<String>();
                int i = 0;
                for (Map.Entry<String, String> keyValue : userMapList.entrySet()) {
                    Contact contact = new Contact();
                    contact.setUserId(keyValue.getKey());
                    contact.setConnected(keyValue.getValue().contains("true"));
                    userIdArray[i] = keyValue.getKey();
                    userIds.add(keyValue.getKey());
                    baseContactService.upsert(contact);
                    i++;
                }
                processUserDetails(userIds);
                return userIdArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized RegisteredUsersApiResponse getRegisteredUsersList(Long startTime, int pageSize) {
        String response = userClientService.getRegisteredUsers(startTime, pageSize);
        RegisteredUsersApiResponse apiResponse = null;
        if (!TextUtils.isEmpty(response) && !MobiComKitConstants.ERROR.equals(response)) {
            apiResponse = (RegisteredUsersApiResponse) GsonUtils.getObjectFromJson(response, RegisteredUsersApiResponse.class);
            if (apiResponse != null) {
                processUserDetail(apiResponse.getUsers());
                userPreference.setRegisteredUsersLastFetchTime(apiResponse.getLastFetchTime());
            }
            return apiResponse;
        }
        return null;
    }

    public ApiResponse muteUserNotifications(String userId, Long notificationAfterTime) {
        ApiResponse response = userClientService.muteUserNotifications(userId, notificationAfterTime);

        if (response == null) {
            return null;
        }
        if (response.isSuccess()) {
            new ContactDatabase(context).updateNotificationAfterTime(userId, notificationAfterTime);
        }

        return response;
    }

    public List<MuteUserResponse> getMutedUserList() {
        MuteUserResponse[] mutedUserList = userClientService.getMutedUserList();

        if (mutedUserList == null) {
            return null;
        }
        for (MuteUserResponse muteUserResponse : mutedUserList) {
            processMuteUserResponse(muteUserResponse);
        }
        return Arrays.asList(mutedUserList);
    }

    public String updateDisplayNameORImageLink(String displayName, String profileImageLink, String localURL, String status) {
        return updateDisplayNameORImageLink(displayName, profileImageLink, localURL, status, null, null, null, null);
    }

    public String updateDisplayNameORImageLink(String displayName, String profileImageLink, String localURL, String status, String contactNumber, Map<String, String> metadata) {
        return updateDisplayNameORImageLink(displayName, profileImageLink, localURL, status, null, null, null, null);
    }

    public String updateDisplayNameORImageLink(String displayName, String profileImageLink, String localURL, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {

        ApiResponse response = userClientService.updateDisplayNameORImageLink(displayName, profileImageLink, status, contactNumber, emailId, metadata, userId);

        if (response == null) {
            return null;
        }
        if (response.isSuccess()) {
            Contact contact = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            if (!TextUtils.isEmpty(displayName)) {
                contact.setFullName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                contact.setImageURL(profileImageLink);
            }
            contact.setLocalImageUrl(localURL);
            if (!TextUtils.isEmpty(status)) {
                contact.setStatus(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                contact.setContactNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                contact.setEmailId(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                Map<String, String> existingMetadata = contact.getMetadata();
                if (existingMetadata == null) {
                    existingMetadata = new HashMap<>();
                }
                existingMetadata.putAll(metadata);
                contact.setMetadata(existingMetadata);
            }
            baseContactService.upsert(contact);
            Contact contact1 = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            Utils.printLog(context, TAG, contact1.getImageURL() + ", " + contact1.getDisplayName() + "," + contact1.getStatus() + "," + contact1.getStatus() + "," + contact1.getMetadata() + "," + contact1.getEmailId() + "," + contact1.getContactNumber());
        }
        return response.getStatus();
    }

    public ApiResponse updateUserWithResponse(String displayName, String profileImageLink, String localURL, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {

        ApiResponse response = userClientService.updateDisplayNameORImageLink(displayName, profileImageLink, status, contactNumber, emailId, metadata, userId);

        if (response == null) {
            return null;
        }

        if (response.isSuccess()) {
            Contact contact = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            if (!TextUtils.isEmpty(displayName)) {
                contact.setFullName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                contact.setImageURL(profileImageLink);
            }
            contact.setLocalImageUrl(localURL);
            if (!TextUtils.isEmpty(status)) {
                contact.setStatus(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                contact.setContactNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                contact.setEmailId(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                Map<String, String> existingMetadata = contact.getMetadata();
                if (existingMetadata == null) {
                    existingMetadata = new HashMap<>();
                }
                existingMetadata.putAll(metadata);
                contact.setMetadata(existingMetadata);
            }
            baseContactService.upsert(contact);
        }
        return response;
    }

    public ApiResponse updateUserWithResponse(User user) {
        return updateUserWithResponse(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getEmail(), user.getMetadata(), user.getUserId());
    }

    public String updateLoggedInUser(User user) {
        return updateDisplayNameORImageLink(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getMetadata());
    }

    public String updateUser(User user) {
        return updateDisplayNameORImageLink(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getEmail(), user.getMetadata(), user.getUserId());
    }


    public void processUserDetailsResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            List<UserDetail> userDetails = (List<UserDetail>) GsonUtils.getObjectFromJson(response, new TypeToken<List<UserDetail>>() {
            }.getType());
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    public void processUserDetailsByUserIds(Set<String> userIds) {
        userClientService.postUserDetailsByUserIds(userIds);
    }

    public ApiResponse processUserReadConversation() {
        return userClientService.getUserReadServerCall();
    }

    public String processUpdateUserPassword(String oldPassword, String newPassword) {
        String response = userClientService.updateUserPassword(oldPassword, newPassword);
        if (!TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response)) {
            userPreference.setPassword(newPassword);
        }
        return response;
    }

    public List<Contact> getUserListBySearch(String searchString) throws ApplozicException {
        try {
            ApiResponse response = userClientService.getUsersBySearchString(searchString);

            if (response == null) {
                return null;
            }

            if (response.isSuccess()) {
                UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(response.getResponse(), List.class), UserDetail[].class);
                List<Contact> contactList = new ArrayList<>();

                for (UserDetail userDetail : userDetails) {
                    contactList.add(getContactFromUserDetail(userDetail));
                }
                return contactList;
            } else {
                if (response.getErrorResponse() != null && !response.getErrorResponse().isEmpty()) {
                    throw new ApplozicException(GsonUtils.getJsonFromObject(response.getErrorResponse(), List.class));
                }
            }
        } catch (Exception e) {
            throw new ApplozicException(e.getMessage());
        }
        return null;
    }

    public void updateUser(User user, AlCallback callback) {
        AlTask.execute(new AlUserUpdateTask(context, user, callback));
    }

    public ApiResponse updateUserDisplayName(String userId, String userDisplayName) {
       ApiResponse response =  userClientService.updateUserDisplayName(userId,userDisplayName);
       if (response != null && response.isSuccess()) {
           baseContactService.updateMetadataKeyValueForUserId(userId, Contact.AL_DISPLAY_NAME_UPDATED, "true");
           Utils.printLog(context, TAG, " Update display name Response :" + response.getStatus());
       }
       return response;
    }
}
