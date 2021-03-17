package com.applozic.mobicomkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.account.user.UserLogoutTask;
import com.applozic.mobicomkit.api.authentication.AlAuthService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.broadcast.ApplozicBroadcastReceiver;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicomkit.listners.AlLogoutHandler;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.data.AlPrefSettings;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlTask;

import java.util.Map;

/**
 * Created by sunil on 29/8/16.
 */
public class Applozic {

    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String DEVICE_REGISTRATION_ID = "DEVICE_REGISTRATION_ID";
    private static final String MY_PREFERENCE = "applozic_preference_key";
    private static final String NOTIFICATION_CHANNEL_VERSION_STATE = "NOTIFICATION_CHANNEL_VERSION_STATE";
    private static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";
    public static Applozic applozic;
    private SharedPreferences sharedPreferences;
    private Context context;
    private ApplozicBroadcastReceiver applozicBroadcastReceiver;

    private Applozic(Context context) {
        this.context = ApplozicService.getContext(context);
        this.sharedPreferences = this.context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static Applozic init(Context context, String applicationKey) {
        applozic = getInstance(context);
        AlPrefSettings.getInstance(context).setApplicationKey(applicationKey);
        return applozic;
    }

    public static Applozic getInstance(Context context) {
        if (applozic == null) {
            applozic = new Applozic(ApplozicService.getContext(context));
        }
        return applozic;
    }

    public void setGeoApiKey(String geoApiKey) {
        AlPrefSettings.getInstance(context).setGeoApiKey(geoApiKey);
    }

    public String getGeoApiKey() {
        String geoApiKey = AlPrefSettings.getInstance(context).getGeoApiKey();
        if (!TextUtils.isEmpty(geoApiKey)) {
            return geoApiKey;
        }
        return Utils.getMetaDataValue(context, AlPrefSettings.GOOGLE_API_KEY_META_DATA);
    }

    public String getApplicationKey() {
        String decryptedApplicationKey = AlPrefSettings.getInstance(context).getApplicationKey();
        if (!TextUtils.isEmpty(decryptedApplicationKey)) {
            return decryptedApplicationKey;
        }
        String existingAppKey = sharedPreferences.getString(APPLICATION_KEY, null);
        if (!TextUtils.isEmpty(existingAppKey)) {
            AlPrefSettings.getInstance(context).setApplicationKey(existingAppKey);
            sharedPreferences.edit().remove(APPLICATION_KEY).commit();
        }
        return existingAppKey;
    }

    public String getDeviceRegistrationId() {
        return sharedPreferences.getString(DEVICE_REGISTRATION_ID, null);
    }

    @SuppressLint("NewApi")
    public int getNotificationChannelVersion() {
        return sharedPreferences.getInt(NOTIFICATION_CHANNEL_VERSION_STATE, NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
    }

    public void setNotificationChannelVersion(int version) {
        sharedPreferences.edit().putInt(NOTIFICATION_CHANNEL_VERSION_STATE, version).commit();
    }

    public Applozic setDeviceRegistrationId(String registrationId) {
        sharedPreferences.edit().putString(DEVICE_REGISTRATION_ID, registrationId).commit();
        return this;
    }

    public Applozic setCustomNotificationSound(String filePath) {
        sharedPreferences.edit().putString(CUSTOM_NOTIFICATION_SOUND, filePath).commit();
        return this;
    }

    public String getCustomNotificationSound() {
        return sharedPreferences.getString(CUSTOM_NOTIFICATION_SOUND, null);
    }

    public static void disconnectPublish(Context context, String deviceKeyString, String userKeyString, boolean useEncrypted) {
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            Intent intent = new Intent(context, ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
            intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
            intent.putExtra(ApplozicMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
            ApplozicMqttIntentService.enqueueWork(context, intent);
        }
    }

    @Deprecated
    public static boolean isLoggedIn(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    public static void disconnectPublish(Context context) {
        disconnectPublish(context, true);
        disconnectPublish(context, false);
    }

    public static void connectPublish(Context context) {
        connectPublish(context, true);
        connectPublish(context, false);
    }

    public static void connectPublishWithVerifyToken(final Context context, String loadingMessage) {
        AlAuthService.verifyToken(context, loadingMessage, new AlCallback() {
            @Override
            public void onSuccess(Object response) {
                connectPublish(context, true);
                connectPublish(context, false);
            }

            @Override
            public void onError(Object error) {

            }
        });
    }


    public static void disconnectPublish(Context context, boolean useEncrypted) {
        final String deviceKeyString = MobiComUserPreference.getInstance(context).getDeviceKeyString();
        final String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
        disconnectPublish(context, deviceKeyString, userKeyString, useEncrypted);
    }

    public static void connectPublish(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, ApplozicMqttIntentService.class);
        subscribeIntent.putExtra(ApplozicMqttIntentService.SUBSCRIBE, true);
        subscribeIntent.putExtra(ApplozicMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        ApplozicMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, ApplozicMqttIntentService.class);
        subscribeIntent.putExtra(ApplozicMqttIntentService.CONNECT_TO_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(ApplozicMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        ApplozicMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void unSubscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, ApplozicMqttIntentService.class);
        subscribeIntent.putExtra(ApplozicMqttIntentService.DISCONNECT_FROM_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(ApplozicMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        ApplozicMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        if (channel != null) {
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(ApplozicMqttIntentService.SUBSCRIBE_TO_TYPING, true);
        ApplozicMqttIntentService.enqueueWork(context, intent);
    }

    public static void unSubscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        if (channel != null) {
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(ApplozicMqttIntentService.UN_SUBSCRIBE_TO_TYPING, true);
        ApplozicMqttIntentService.enqueueWork(context, intent);
    }

    public static void publishTypingStatus(Context context, Channel channel, Contact contact, boolean typingStarted) {
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);

        if (channel != null) {
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
        ApplozicMqttIntentService.enqueueWork(context, intent);
    }

    @Deprecated
    public static void loginUser(Context context, User user, AlLoginHandler loginHandler) {
        if (MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    public static void connectUser(Context context, User user, AlLoginHandler loginHandler) {
        if (isConnected(context)) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            Contact contact = new ContactDatabase(context).getContactById(MobiComUserPreference.getInstance(context).getUserId());
            if (contact != null) {
                registrationResponse.setUserId(contact.getUserId());
                registrationResponse.setContactNumber(contact.getContactNumber());
                registrationResponse.setRoleType(contact.getRoleType());
                registrationResponse.setImageLink(contact.getImageURL());
                registrationResponse.setDisplayName(contact.getDisplayName());
                registrationResponse.setStatusMessage(contact.getStatus());
            }
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    public static void connectUserWithoutCheck(Context context, User user, AlLoginHandler loginHandler) {
        AlTask.execute(new UserLoginTask(user, loginHandler, context));
    }

    public static boolean isConnected(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    public static boolean isRegistered(Context context) {
        return MobiComUserPreference.getInstance(context).isRegistered();
    }

    public static boolean isApplozicNotification(Context context, Map<String, String> data) {
        if (MobiComPushReceiver.isMobiComPushNotification(data)) {
            MobiComPushReceiver.processMessageAsync(context, data);
            return true;
        }
        return false;
    }

    @Deprecated
    public static void loginUser(Context context, User user, boolean withLoggedInCheck, AlLoginHandler loginHandler) {
        if (withLoggedInCheck && MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    public static void logoutUser(final Context context, AlLogoutHandler logoutHandler) {
        AlTask.execute(new UserLogoutTask(logoutHandler, context));
    }

    public static void registerForPushNotification(Context context, String pushToken, AlPushNotificationHandler handler) {
        AlTask.execute(new PushNotificationTask(context, pushToken, handler));
    }

    public static void registerForPushNotification(Context context, AlPushNotificationHandler handler) {
        registerForPushNotification(context, Applozic.getInstance(context).getDeviceRegistrationId(), handler);
    }


    @Deprecated
    public void registerUIListener(ApplozicUIListener applozicUIListener) {
        applozicBroadcastReceiver = new ApplozicBroadcastReceiver(applozicUIListener);
        LocalBroadcastManager.getInstance(context).registerReceiver(applozicBroadcastReceiver, BroadcastService.getIntentFilter());
    }

    @Deprecated
    public void unregisterUIListener() {
        if (applozicBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(applozicBroadcastReceiver);
            applozicBroadcastReceiver = null;
        }
    }
}