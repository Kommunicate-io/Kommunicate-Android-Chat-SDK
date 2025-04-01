package io.kommunicate.devkit.api.account.user;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import io.kommunicate.devkit.AlUserUpdate;
import io.kommunicate.devkit.Applozic;
import io.kommunicate.devkit.api.notification.NotificationChannels;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.exception.ApplozicException;
import io.kommunicate.commons.ALSpecificSettings;
import io.kommunicate.devkit.api.HttpRequestUtils;
import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.ApplozicMqttIntentService;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.api.notification.MuteUserResponse;
import io.kommunicate.devkit.database.MobiComDatabaseHelper;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.feed.SyncBlockUserApiResponse;
import io.kommunicate.devkit.feed.UserDetailListFeed;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by devashish on 24/12/14.
 */
public class UserClientService extends MobiComKitClientService {

    public static final String APP_VERSION_UPDATE_URL = "/rest/ws/register/version/update";
    public static final String USER_INFO_URL = "/rest/ws/user/info?";
    public static final Short MOBICOMKIT_VERSION_CODE = 109;
    public static final String USER_DISPLAY_NAME_UPDATE = "/rest/ws/user/name?";
    public static final String BLOCK_USER_URL = "/rest/ws/user/block";
    public static final String BLOCK_USER_SYNC_URL = "/rest/ws/user/blocked/sync";
    public static final String UNBLOCK_USER_SYNC_URL = "/rest/ws/user/unblock";
    public static final String USER_DETAILS_URL = "/rest/ws/user/detail?";
    public static final String ONLINE_USER_LIST_URL = "/rest/ws/user/ol/list";
    public static final String REGISTERED_USER_LIST_URL = "/rest/ws/user/filter";
    public static final String BUSINESS_SETTINGS_URL = "/rest/ws/team/business-settings";
    public static final String USER_PROFILE_UPDATE_URL = "/rest/ws/user/update";
    public static final String USER_READ_URL = "/rest/ws/user/read";
    public static final String USER_DETAILS_LIST_POST_URL = "/rest/ws/user/detail";
    public static final String  USER_DETAILS_LIST_POST_URL_V2 = "/rest/ws/user/v2/detail";
    public static final String UPDATE_USER_PASSWORD = "/rest/ws/user/update/password";
    public static final String USER_LOGOUT = "/rest/ws/device/logout";
    private static final String MUTE_USER_URL = "/rest/ws/user/chat/mute";
    private static final String USER_SEARCH_URL = "/rest/ws/user/search/contact";
    private static final String GET_MUTED_USER_LIST = "/rest/ws/user/chat/mute/list";
    public static final int BATCH_SIZE = 60;
    private static final String TAG = "UserClientService";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String APPLICATION_JSON = "application/json";
    private static final String UNAUTH_ACCESS = "UnAuthorized Access";
    private static final String ELASTIC_UPDATE_TRUE = "?elasticUpdate=true&allowEmail=true";
    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getUserProfileUpdateUrl() {
        return getBaseUrl() + USER_PROFILE_UPDATE_URL;
    }

    public String getAppVersionUpdateUrl() {
        return getBaseUrl() + APP_VERSION_UPDATE_URL;
    }

    public String getUpdateUserDisplayNameUrl() {
        return getBaseUrl() + USER_DISPLAY_NAME_UPDATE;
    }

    public String getUserInfoUrl() {
        return getBaseUrl() + USER_INFO_URL;
    }

    public String getBlockUserUrl() {
        return getBaseUrl() + BLOCK_USER_URL;
    }

    public String getBlockUserSyncUrl() {
        return getBaseUrl() + BLOCK_USER_SYNC_URL;
    }

    public String getUnBlockUserSyncUrl() {
        return getBaseUrl() + UNBLOCK_USER_SYNC_URL;
    }

    public String getUserDetailsListUrl() {
        return getBaseUrl() + USER_DETAILS_URL;
    }

    public String getOnlineUserListUrl() {
        return getBaseUrl() + ONLINE_USER_LIST_URL;
    }

    public String getRegisteredUserListUrl() {
        return getBaseUrl() + REGISTERED_USER_LIST_URL;
    }

    public String getBusinessSettingsUrl() {
        return getBaseUrl() + BUSINESS_SETTINGS_URL;
    }

    public String getUserDetailsListPostUrl() {
        return getBaseUrl() + USER_DETAILS_LIST_POST_URL_V2;
    }

    public String getUserReadUrl() {
        return getBaseUrl() + USER_READ_URL;
    }

    public String getUpdateUserPasswordUrl() {
        return getBaseUrl() + UPDATE_USER_PASSWORD;
    }

    public String getUserLogout() {
        return getBaseUrl() + USER_LOGOUT;
    }

    private String getMuteUserUrl() {
        return getBaseUrl() + MUTE_USER_URL;
    }

    private String getMutedUserListUrl() {
        return getBaseUrl() + GET_MUTED_USER_LIST;
    }

    private String getUserSearchUrl() {
        return getBaseUrl() + USER_SEARCH_URL;
    }

    public ApiResponse logout() {
        return logout(false);
    }

    public void clearDataAndPreference() {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        ChannelService.clearInstance();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
        mobiComUserPreference.setUrl(url);
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
        intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
        ApplozicMqttIntentService.enqueueWork(context, intent);
    }

    public ApiResponse logout(boolean fromLogin) {
        Utils.printLog(context, TAG, "Al Logout call !!");
        ApiResponse apiResponse = userLogoutResponse();
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Applozic.getInstance(context).setCustomNotificationSound(null);
            new NotificationChannels(context, null).deleteAllChannels();
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        ALSpecificSettings.getInstance(context).clearAll();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
        mobiComUserPreference.setUrl(url);
        if (!fromLogin) {
            Intent intent = new Intent(context, ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
            intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
            ApplozicMqttIntentService.enqueueWork(context, intent);
        }
        return apiResponse;
    }

    public ApiResponse userLogoutResponse() {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.postData(getUserLogout(), APPLICATION_JSON, APPLICATION_JSON, null);
            if (!TextUtils.isEmpty(response)) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public void updateCodeVersion(final String deviceKeyString) {
        String url = getAppVersionUpdateUrl() + "?appVersionCode=" + MOBICOMKIT_VERSION_CODE + "&deviceKey=" + deviceKeyString;
        String response = httpRequestUtils.getResponse(url, TEXT_PLAIN, TEXT_PLAIN);
        Utils.printLog(context, TAG, "Version update response: " + response);
    }

    public Map<String, String> getUserInfo(Set<String> userIds) throws JSONException, UnsupportedEncodingException {

        if (userIds == null && userIds.isEmpty()) {
            return new HashMap<>();
        }

        String userIdParam = "";
        for (String userId : userIds) {
            userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
        }

        String response = httpRequestUtils.getResponse(getUserInfoUrl() + userIdParam, APPLICATION_JSON, APPLICATION_JSON);
        Utils.printLog(context, TAG, "Response: " + response);

        JSONObject jsonObject = new JSONObject(response);

        Map<String, String> info = new HashMap<String, String>();

        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = jsonObject.getString(key);
            info.put(key, value);
        }
        return info;
    }

    public ApiResponse updateUserDisplayName(final String userId, final String displayName) {
        String parameters = "";
        try {
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(displayName)) {
                parameters = "userId=" + URLEncoder.encode(userId, "UTF-8") + "&displayName=" + URLEncoder.encode(displayName, "UTF-8");
                String response = httpRequestUtils.getResponse(getUpdateUserDisplayNameUrl() + parameters, APPLICATION_JSON, APPLICATION_JSON);

                if (!TextUtils.isEmpty(response)) {
                    return (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse userBlock(String userId, boolean block) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse((block ? getBlockUserUrl() : getUnBlockUserSyncUrl()) + "?userId=" + URLEncoder.encode(userId, "UTF-8"), APPLICATION_JSON, APPLICATION_JSON);
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse userUnBlock(String userId) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse(getUnBlockUserSyncUrl() + "?userId=" + URLEncoder.encode(userId, "UTF-8"), APPLICATION_JSON, APPLICATION_JSON);
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public SyncBlockUserApiResponse getSyncUserBlockList(String lastSyncTime) {
        try {
            String url = getBlockUserSyncUrl() + "?lastSyncTime=" + lastSyncTime;
            String response = httpRequestUtils.getResponse(url, APPLICATION_JSON, APPLICATION_JSON);

            if (response == null || TextUtils.isEmpty(response) || response.equals(UNAUTH_ACCESS)) {
                return null;
            }
            return (SyncBlockUserApiResponse) GsonUtils.getObjectFromJson(response, SyncBlockUserApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserDetails(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                String response = "";
                String userIdParam = "";
                for (String userId : userIds) {
                    userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
                }
                response = httpRequestUtils.getResponse(getUserDetailsListUrl() + userIdParam, APPLICATION_JSON, APPLICATION_JSON);
                Utils.printLog(context, TAG, "User details response is :" + response);
                if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                    return null;
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String postUserDetailsByUserIds(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                List<String> userDetailsList = new ArrayList<>();
                String response = "";
                int count = 0;
                for (String userId : userIds) {
                    count++;
                    userDetailsList.add(userId);
                    if (count % BATCH_SIZE == 0) {
                        UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                        userDetailListFeed.setContactSync(true);
                        userDetailListFeed.setUserIdList(userDetailsList);
                        String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                        Utils.printLog(context, TAG, "Sending json:" + jsonFromObject);
                        response = httpRequestUtils.postData(getUserDetailsListPostUrl(), APPLICATION_JSON, APPLICATION_JSON, jsonFromObject);
                        userDetailsList = new ArrayList<String>();
                        if (!TextUtils.isEmpty(response)) {
                            UserService.getInstance(context).processUserDetailsResponse(response);
                        }
                    }
                }
                if (!userDetailsList.isEmpty() && userDetailsList.size() > 0) {
                    UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                    userDetailListFeed.setContactSync(true);
                    userDetailListFeed.setUserIdList(userDetailsList);
                    String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                    response = httpRequestUtils.postData(getUserDetailsListPostUrl(), APPLICATION_JSON, APPLICATION_JSON, jsonFromObject);

                    Utils.printLog(context, TAG, "User details response is :" + response);
                    if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                        return null;
                    }

                    if (!TextUtils.isEmpty(response)) {
                        UserService.getInstance(context).processUserDetailsResponse(response);
                    }
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getOnlineUserList(int numberOfUser) {
        Map<String, String> info = new HashMap<String, String>();
        try {
            String response = httpRequestUtils.getResponse(getOnlineUserListUrl() + "?startIndex=0&pageSize=" + numberOfUser, APPLICATION_JSON, APPLICATION_JSON);
            if (response != null && !MobiComKitConstants.ERROR.equals(response)) {
                JSONObject jsonObject = new JSONObject(response);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = jsonObject.getString(key);
                    info.put(key, value);
                }
                return info;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public String getRegisteredUsers(Long startTime, int pageSize) {
        String response = null;
        try {
            String url = "?pageSize=" + pageSize;
            if (startTime > 0) {
                url = url + "&startTime=" + startTime;
            }
            response = httpRequestUtils.getResponse(getRegisteredUserListUrl() + url, APPLICATION_JSON, APPLICATION_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String getBusinessHoursData() {
        String response;
        try {
            response = httpRequestUtils.getResponse(getBusinessSettingsUrl(), APPLICATION_JSON, APPLICATION_JSON);
        } catch (Exception e) {
            return null;
        }
        return response;
    }

    public ApiResponse updateDisplayNameORImageLink(String displayName, String profileImageLink, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {
        AlUserUpdate userUpdate = new AlUserUpdate();
        try {
            if (!TextUtils.isEmpty(displayName)) {
                userUpdate.setDisplayName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                userUpdate.setImageLink(profileImageLink);
            }
            if (!TextUtils.isEmpty(status)) {
                userUpdate.setStatusMessage(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                userUpdate.setPhoneNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                userUpdate.setEmail(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                userUpdate.setMetadata(metadata);
            }

            String response = httpRequestUtils.postData(getUserProfileUpdateUrl() + (!TextUtils.isEmpty(emailId) ? ELASTIC_UPDATE_TRUE : ""), GsonUtils.getJsonFromObject(userUpdate, AlUserUpdate.class), userId);
            Utils.printLog(context, TAG, response);
            return ((ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse updateEmail(String emailId, String userId) {
        AlUserUpdate userUpdate = new AlUserUpdate();
        try {
            if (!TextUtils.isEmpty(emailId)) {
                userUpdate.setEmail(emailId);
            }

            String url = getUserProfileUpdateUrl() + ELASTIC_UPDATE_TRUE;

            String response = httpRequestUtils.postData(url, GsonUtils.getJsonFromObject(userUpdate, AlUserUpdate.class), userId);
            Utils.printLog(context, TAG, response);
            return ((ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse muteUserNotifications(String userId, Long notificationAfterTime) {
        if (userId == null || notificationAfterTime == null) {
            return null;
        }

        JSONObject jsonFromObject = new JSONObject();

        try {
            String url = getMuteUserUrl() + "?userId=" + userId + "&notificationAfterTime=" + notificationAfterTime;
            String response = httpRequestUtils.postData(url, APPLICATION_JSON, APPLICATION_JSON, jsonFromObject.toString());
            Utils.printLog(context, TAG, "Mute user chat response : " + response);

            if (!TextUtils.isEmpty(response)) {
                return (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public MuteUserResponse[] getMutedUserList() {
        try {
            String response = httpRequestUtils.getResponse(getMutedUserListUrl(), APPLICATION_JSON, APPLICATION_JSON);
            Utils.printLog(context, TAG, "Muted users list reponse : " + response);

            if (!TextUtils.isEmpty(response)) {
                return (MuteUserResponse[]) GsonUtils.getObjectFromJson(response, MuteUserResponse[].class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public ApiResponse getUserReadServerCall() {
        String response = null;
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUserReadUrl(), null, null);
            if (response != null) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
            Utils.printLog(context, TAG, "User read response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public String updateUserPassword(String oldPassword, String newPassword) {
        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            return null;
        }
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUpdateUserPasswordUrl() + "?oldPassword=" + oldPassword + "&newPassword=" + newPassword, APPLICATION_JSON, APPLICATION_JSON);
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse != null && apiResponse.isSuccess()) {
                return apiResponse.getStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse getUsersBySearchString(String searchString) throws ApplozicException {
        if (TextUtils.isEmpty(searchString)) {
            return null;
        }
        String response;
        ApiResponse apiResponse;

        try {
            response = httpRequestUtils.getResponse(getUserSearchUrl() + "?name=" + URLEncoder.encode(searchString, "UTF-8"), APPLICATION_JSON, APPLICATION_JSON);
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            Utils.printLog(context, TAG, "Search user response : " + response);
            apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
        } catch (Exception e) {
            throw new ApplozicException(e.getMessage());
        }

        return apiResponse;
    }
}
