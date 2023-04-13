package io.kommunicate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Map;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.kommunicate.broadcast.KommunicateBroadcastReceiver;
import io.kommunicate.broadcast.BroadcastService;
import io.kommunicate.callbacks.AlCallback;
import io.kommunicate.callbacks.AlLoginHandler;
import io.kommunicate.callbacks.AlLogoutHandler;
import io.kommunicate.callbacks.AlPushNotificationHandler;
import io.kommunicate.callbacks.KommunicateUIListener;
import io.kommunicate.data.conversation.KmMqttIntentService;
import io.kommunicate.data.preference.AlPrefSettings;
import io.kommunicate.data.account.register.RegistrationResponse;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.account.user.PushNotificationTask;
import io.kommunicate.data.account.user.User;
import io.kommunicate.data.account.user.UserLoginTask;
import io.kommunicate.data.account.user.UserLogoutTask;
import io.kommunicate.notification.MobiComPushReceiver;
import io.kommunicate.notification.NotificationChannels;
import io.kommunicate.data.api.authentication.AlAuthService;
import io.kommunicate.data.contact.database.ContactDatabase;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;
import io.kommunicate.data.async.task.AlTask;
import io.kommunicate.utils.Utils;

/**
 * Created by sunil on 29/8/16.
 */
public class KmChat {

    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String DEVICE_REGISTRATION_ID = "DEVICE_REGISTRATION_ID";
    private static final String MY_PREFERENCE = "applozic_preference_key";
    private static final String NOTIFICATION_CHANNEL_VERSION_STATE = "NOTIFICATION_CHANNEL_VERSION_STATE";
    private static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";
    public static KmChat kmChat;
    private SharedPreferences sharedPreferences;
    private Context context;
    private KommunicateBroadcastReceiver kommunicateBroadcastReceiver;

    private KmChat(Context context) {
        this.context = KommunicateService.getContext(context);
        this.sharedPreferences = this.context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static KmChat init(Context context, String applicationKey) {
        kmChat = getInstance(context);
        AlPrefSettings.getInstance(context).setApplicationKey(applicationKey);
        return kmChat;
    }

    public static KmChat getInstance(Context context) {
        if (kmChat == null) {
            kmChat = new KmChat(KommunicateService.getContext(context));
        }
        return kmChat;
    }

    public static void disconnectPublish(Context context, String deviceKeyString, String userKeyString, boolean useEncrypted) {
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            Intent intent = new Intent(context, KmMqttIntentService.class);
            intent.putExtra(KmMqttIntentService.USER_KEY_STRING, userKeyString);
            intent.putExtra(KmMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
            intent.putExtra(KmMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
            KmMqttIntentService.enqueueWork(context, intent);
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
        Intent subscribeIntent = new Intent(context, KmMqttIntentService.class);
        subscribeIntent.putExtra(KmMqttIntentService.SUBSCRIBE, true);
        subscribeIntent.putExtra(KmMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        KmMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, KmMqttIntentService.class);
        subscribeIntent.putExtra(KmMqttIntentService.CONNECT_TO_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(KmMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        KmMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void unSubscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, KmMqttIntentService.class);
        subscribeIntent.putExtra(KmMqttIntentService.DISCONNECT_FROM_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(KmMqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        KmMqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, KmMqttIntentService.class);
        if (channel != null) {
            intent.putExtra(KmMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(KmMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(KmMqttIntentService.SUBSCRIBE_TO_TYPING, true);
        KmMqttIntentService.enqueueWork(context, intent);
    }

    public static void unSubscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, KmMqttIntentService.class);
        if (channel != null) {
            intent.putExtra(KmMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(KmMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(KmMqttIntentService.UN_SUBSCRIBE_TO_TYPING, true);
        KmMqttIntentService.enqueueWork(context, intent);
    }

    public static void publishTypingStatus(Context context, Channel channel, Contact contact, boolean typingStarted) {
        Intent intent = new Intent(context, KmMqttIntentService.class);

        if (channel != null) {
            intent.putExtra(KmMqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(KmMqttIntentService.CONTACT, contact);
        }
        intent.putExtra(KmMqttIntentService.TYPING, typingStarted);
        KmMqttIntentService.enqueueWork(context, intent);
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

    public static boolean isKommunicateNotification(Context context, Map<String, String> data) {
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
        registerForPushNotification(context, KmChat.getInstance(context).getDeviceRegistrationId(), handler);
    }

    public String getGeoApiKey() {
        String geoApiKey = AlPrefSettings.getInstance(context).getGeoApiKey();
        if (!TextUtils.isEmpty(geoApiKey)) {
            return geoApiKey;
        }
        return Utils.getMetaDataValue(context, AlPrefSettings.GOOGLE_API_KEY_META_DATA);
    }

    public void setGeoApiKey(String geoApiKey) {
        AlPrefSettings.getInstance(context).setGeoApiKey(geoApiKey);
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

    public KmChat setDeviceRegistrationId(String registrationId) {
        sharedPreferences.edit().putString(DEVICE_REGISTRATION_ID, registrationId).commit();
        return this;
    }

    @SuppressLint("NewApi")
    public int getNotificationChannelVersion() {
        return sharedPreferences.getInt(NOTIFICATION_CHANNEL_VERSION_STATE, NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
    }

    public void setNotificationChannelVersion(int version) {
        sharedPreferences.edit().putInt(NOTIFICATION_CHANNEL_VERSION_STATE, version).commit();
    }

    public String getCustomNotificationSound() {
        return sharedPreferences.getString(CUSTOM_NOTIFICATION_SOUND, null);
    }

    public KmChat setCustomNotificationSound(String filePath) {
        sharedPreferences.edit().putString(CUSTOM_NOTIFICATION_SOUND, filePath).commit();
        return this;
    }

    @Deprecated
    public void registerUIListener(KommunicateUIListener kommunicateUIListener) {
        kommunicateBroadcastReceiver = new KommunicateBroadcastReceiver(kommunicateUIListener);
        LocalBroadcastManager.getInstance(context).registerReceiver(kommunicateBroadcastReceiver, BroadcastService.getIntentFilter());
    }

    @Deprecated
    public void unregisterUIListener() {
        if (kommunicateBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(kommunicateBroadcastReceiver);
            kommunicateBroadcastReceiver = null;
        }
    }
}