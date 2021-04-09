package com.applozic.mobicomkit.api.account.register;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.authentication.JWT;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.ConversationIntentService;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by devashish on 2/2/15.
 */
public class RegisterUserClientService extends MobiComKitClientService {

    private static final String CREATE_ACCOUNT_URL = "/rest/ws/register/client?";
    private static final String UPDATE_ACCOUNT_URL = "/rest/ws/register/update?";
    private static final String CHECK_PRICING_PACKAGE = "/rest/ws/application/pricing/package";
    private static final String REFRESH_TOKEN_URL = "/rest/ws/register/refresh/token";
    public static final Short MOBICOMKIT_VERSION_CODE = 112;
    private static final String TAG = "RegisterUserClient";
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    private HttpRequestUtils httpRequestUtils;

    public RegisterUserClientService(Context context) {
        this.context = ApplozicService.getContext(context);
        ApplozicService.initWithContext(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getCreateAccountUrl() {
        return getBaseUrl() + CREATE_ACCOUNT_URL;
    }

    public String getPricingPackageUrl() {
        return getBaseUrl() + CHECK_PRICING_PACKAGE;
    }

    public String getUpdateAccountUrl() {
        return getBaseUrl() + UPDATE_ACCOUNT_URL;
    }

    public String getRefreshTokenUrl() {
        return getBaseUrl() + REFRESH_TOKEN_URL;
    }

    public RegistrationResponse createAccount(User user) throws Exception {
        if (user.getDeviceType() == null) {
            user.setDeviceType(Short.valueOf("1"));
        }
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setEnableEncryption(user.isEnableEncryption());

        if (!TextUtils.isEmpty(user.getAlBaseUrl())) {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(user.getAlBaseUrl());
        }

        if (!TextUtils.isEmpty(user.getKmBaseUrl())) {
            ALSpecificSettings.getInstance(context).setKmBaseUrl(user.getKmBaseUrl());
        }

        if (TextUtils.isEmpty(user.getUserId())) {
            throw new ApplozicException("userId cannot be empty");
        }

        if (!user.isValidUserId()) {
            throw new ApplozicException("Invalid userId. Spacing and set of special characters ^!$%&*:(), are not accepted. \nOnly english language characters are accepted");
        }

        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(getApplicationKey(context));
        user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());

        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }

        Utils.printLog(context, TAG, "Net status" + Utils.isInternetAvailable(context.getApplicationContext()));

        if (!Utils.isInternetAvailable(context.getApplicationContext())) {
            throw new ConnectException("No Internet Connection");
        }

        HttpRequestUtils.isRefreshTokenInProgress = true;
        Utils.printLog(context, TAG, "Registration json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getCreateAccountUrl(), gson.toJson(user));

        Utils.printLog(context, TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception("503 Service Unavailable");
        }

        final RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);
        if (registrationResponse.isRegistrationSuccess()) {

            Utils.printLog(context, "Registration response ", "is " + registrationResponse);
            if (registrationResponse.getNotificationResponse() != null) {
                Utils.printLog(context, "Registration response ", "" + registrationResponse.getNotificationResponse());
            }
            JWT.parseToken(context, registrationResponse.getAuthToken());
            mobiComUserPreference.setEncryptionKey(registrationResponse.getEncryptionKey());
            mobiComUserPreference.enableEncryption(user.isEnableEncryption());
            mobiComUserPreference.setCountryCode(user.getCountryCode());
            mobiComUserPreference.setUserId(user.getUserId());
            mobiComUserPreference.setContactNumber(user.getContactNumber());
            mobiComUserPreference.setEmailVerified(user.isEmailVerified());
            mobiComUserPreference.setDisplayName(user.getDisplayName());
            mobiComUserPreference.setMqttBrokerUrl(registrationResponse.getBrokerUrl());
            mobiComUserPreference.setDeviceKeyString(registrationResponse.getDeviceKey());
            mobiComUserPreference.setEmailIdValue(user.getEmail());
            mobiComUserPreference.setImageLink(user.getImageLink());
            mobiComUserPreference.setSuUserKeyString(registrationResponse.getUserKey());
            mobiComUserPreference.setLastSyncTimeForMetadataUpdate(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setLastSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setLastSeenAtSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setChannelSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setUserBlockSyncTime("10000");
            mobiComUserPreference.setUserDeactivated(registrationResponse.isDeactivate());
            if (registrationResponse.getNotificationAfter() != null) {
                ALSpecificSettings.getInstance(context).setNotificationAfterTime(registrationResponse.getNotificationAfter());
            }
            ApplozicClient.getInstance(context).skipDeletedGroups(user.isSkipDeletedGroups()).hideActionMessages(user.isHideActionMessages());
            if (!TextUtils.isEmpty(registrationResponse.getUserEncryptionKey())) {
                mobiComUserPreference.setUserEncryptionKey(registrationResponse.getUserEncryptionKey());
            }
            mobiComUserPreference.setPassword(user.getPassword());
            mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
            mobiComUserPreference.setAuthenticationType(String.valueOf(user.getAuthenticationTypeId()));
            if (registrationResponse.getRoleType() != null) {
                mobiComUserPreference.setUserRoleType(registrationResponse.getRoleType());
            }
            if (user.getUserTypeId() != null) {
                mobiComUserPreference.setUserTypeId(String.valueOf(user.getUserTypeId()));
            }
            if (!TextUtils.isEmpty(user.getNotificationSoundFilePath())) {
                Applozic.getInstance(context).setCustomNotificationSound(user.getNotificationSoundFilePath());
            }
            Contact contact = new Contact();
            contact.setUserId(user.getUserId());
            contact.setFullName(registrationResponse.getDisplayName());
            contact.setImageURL(registrationResponse.getImageLink());
            contact.setContactNumber(registrationResponse.getContactNumber());
            contact.setMetadata(registrationResponse.getMetadata());
            if (user.getUserTypeId() != null) {
                contact.setUserTypeId(user.getUserTypeId());
            }
            contact.setRoleType(user.getRoleType());
            contact.setStatus(registrationResponse.getStatusMessage());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Applozic.getInstance(context).setNotificationChannelVersion(NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
                new NotificationChannels(context, Applozic.getInstance(context).getCustomNotificationSound()).prepareNotificationChannels();
            }
            ApplozicClient.getInstance(context).setChatDisabled(contact.isChatForUserDisabled());
            new AppContactService(context).upsert(contact);


            Intent conversationIntentService = new Intent(context, ConversationIntentService.class);
            conversationIntentService.putExtra(ConversationIntentService.SYNC, false);
            ConversationIntentService.enqueueWork(context, conversationIntentService);


            Intent mutedUserListService = new Intent(context, ConversationIntentService.class);
            mutedUserListService.putExtra(ConversationIntentService.MUTED_USER_LIST_SYNC, true);
            ConversationIntentService.enqueueWork(context, mutedUserListService);

            Intent intent = new Intent(context, ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH, true);
            ApplozicMqttIntentService.enqueueWork(context, intent);

        }

        return registrationResponse;
    }

    public boolean refreshAuthToken(String applicationId, String userId) {
        try {
            HttpRequestUtils.isRefreshTokenInProgress = true;
            Map<String, String> tokenRefreshBodyMap = new HashMap<>();
            tokenRefreshBodyMap.put("applicationId", applicationId);
            tokenRefreshBodyMap.put("userId", userId);
            String response = httpRequestUtils.postDataForAuthToken(getRefreshTokenUrl(), GsonUtils.getJsonFromObject(tokenRefreshBodyMap, Map.class), userId);
            if (!TextUtils.isEmpty(response)) {
                ApiResponse<String> jwtTokenResponse = (ApiResponse<String>) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                if (jwtTokenResponse != null && !TextUtils.isEmpty(jwtTokenResponse.getResponse())) {
                    JWT.parseToken(context, jwtTokenResponse.getResponse());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public RegistrationResponse createAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        String url = mobiComUserPreference.getUrl();
        mobiComUserPreference.clearAll();
        mobiComUserPreference.setUrl(url);

        return updateAccount(email, userId, phoneNumber, displayName, imageLink, pushNotificationId);
    }

    private RegistrationResponse updateAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setImageLink(imageLink);
        user.setRegistrationId(pushNotificationId);
        user.setDisplayName(displayName);
        user.setContactNumber(phoneNumber);

        final RegistrationResponse registrationResponse = createAccount(user);
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH, true);
        ApplozicMqttIntentService.enqueueWork(context, intent);
        return registrationResponse;
    }

    public RegistrationResponse updatePushNotificationId(final String pushNotificationId) throws Exception {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        //Note: In case if gcm registration is done before login then only updating in pref

        RegistrationResponse registrationResponse = null;
        User user = getUserDetail();

        if (!TextUtils.isEmpty(pushNotificationId)) {
            pref.setDeviceRegistrationId(pushNotificationId);
        }
        user.setRegistrationId(pushNotificationId);
        if (pref.isRegistered()) {
            registrationResponse = updateRegisteredAccount(user);
        }
        return registrationResponse;
    }


    public RegistrationResponse updateRegisteredAccount(User user) throws Exception {
        RegistrationResponse registrationResponse = null;

        if (user.getDeviceType() == null) {
            user.setDeviceType(Short.valueOf("1"));
        }
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);

        if (!TextUtils.isEmpty(user.getAlBaseUrl())) {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(user.getAlBaseUrl());
        }

        if (!TextUtils.isEmpty(user.getKmBaseUrl())) {
            ALSpecificSettings.getInstance(context).setKmBaseUrl(user.getKmBaseUrl());
        }

        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setEnableEncryption(mobiComUserPreference.isEncryptionEnabled());
        user.setApplicationId(getApplicationKey(context));
        user.setAuthenticationTypeId(Short.valueOf(mobiComUserPreference.getAuthenticationType()));
        if (!TextUtils.isEmpty(mobiComUserPreference.getUserTypeId())) {
            user.setUserTypeId(Short.valueOf(mobiComUserPreference.getUserTypeId()));
        }
        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }
        if (!TextUtils.isEmpty(mobiComUserPreference.getDeviceRegistrationId())) {
            user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());
        }
        Utils.printLog(context, TAG, "Registration update json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getUpdateAccountUrl(), gson.toJson(user));

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw null;
        }

        registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        Utils.printLog(context, TAG, "Registration update response: " + registrationResponse);
        mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
        if (registrationResponse.getNotificationResponse() != null) {
            Utils.printLog(context, TAG, "Notification response: " + registrationResponse.getNotificationResponse());
        }

        return registrationResponse;
    }

    private User getUserDetail() {

        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);

        User user = new User();
        user.setEmail(pref.getEmailIdValue());
        user.setUserId(pref.getUserId());
        user.setContactNumber(pref.getContactNumber());
        user.setDisplayName(pref.getDisplayName());
        user.setImageLink(pref.getImageLink());
        user.setRoleType(pref.getUserRoleType());
        return user;
    }

    public void syncAccountStatus() {
        try {
            String response = httpRequestUtils.getResponse(getPricingPackageUrl(), "application/json", "application/json");
            Utils.printLog(context, TAG, "Pricing package response: " + response);
            ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse.getResponse() != null) {
                int pricingPackage = Integer.parseInt(apiResponse.getResponse().toString());
                MobiComUserPreference.getInstance(context).setPricingPackage(pricingPackage);
            }
        } catch (Exception e) {
            Utils.printLog(context, TAG, "Account status sync call failed");
        }
    }
}
