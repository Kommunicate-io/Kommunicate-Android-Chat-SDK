package io.kommunicate.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.ConversationIntentService;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.util.TimeZone;

import io.kommunicate.KmUserPreference;
import io.kommunicate.models.KmUserRegistrationResponse;

//TODO: won't be needed after applozic sdk update
public class KmRegisterUserClientService extends RegisterUserClientService {
    private static final String TAG = "KmRegisterUserClient";

    private HttpRequestUtils httpRequestUtils;

    public KmRegisterUserClientService(Context context) {
        super(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    @Override
    public RegistrationResponse createAccount(User user) throws Exception {
        user.setDeviceType(Short.valueOf("1"));
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

        Utils.printLog(context, TAG, "Registration json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getCreateAccountUrl(), gson.toJson(user));

        Utils.printLog(context, TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception("503 Service Unavailable");
        }

        final KmUserRegistrationResponse registrationResponse = gson.fromJson(response, KmUserRegistrationResponse.class);

        if (registrationResponse.isRegistrationSuccess()) {

            Utils.printLog(context, "Registration response ", "is " + registrationResponse);
            if (registrationResponse.getNotificationResponse() != null) {
                Utils.printLog(context, "Registration response ", "" + registrationResponse.getNotificationResponse());
            }

            KmUserPreference.getInstance(context).setJwtToken(registrationResponse.getAuthToken());

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
            if(registrationResponse.getNotificationAfter() != null){
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
}
