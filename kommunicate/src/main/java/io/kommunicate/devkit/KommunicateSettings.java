package io.kommunicate.devkit;

import static io.kommunicate.utils.KmConstants.KM_USER_LOCALE;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;

import io.kommunicate.devkit.api.account.register.RegistrationResponse;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.authentication.AuthService;
import io.kommunicate.devkit.api.conversation.MqttIntentService;
import io.kommunicate.devkit.api.notification.MobiComPushReceiver;
import io.kommunicate.devkit.api.notification.NotificationChannels;
import io.kommunicate.devkit.broadcast.ConversationBroadcastReceiver;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.contact.database.ContactDatabase;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.devkit.listners.LoginHandler;
import io.kommunicate.devkit.listners.LogoutHandler;
import io.kommunicate.devkit.listners.PushNotificationHandler;
import io.kommunicate.devkit.listners.UIEventListener;

import io.kommunicate.usecase.PushNotificationUseCase;
import io.kommunicate.usecase.UserLoginUseCase;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.data.PrefSettings;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import annotations.CleanUpRequired;
import io.kommunicate.KmSettings;
import io.kommunicate.R;
import io.kommunicate.usecase.UserLogoutUseCase;

/**
 * Created by sunil on 29/8/16.
 */
public class KommunicateSettings {

    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String DEVICE_REGISTRATION_ID = "DEVICE_REGISTRATION_ID";
    private static final String MY_PREFERENCE = "applozic_preference_key";
    private static final String NOTIFICATION_CHANNEL_VERSION_STATE = "NOTIFICATION_CHANNEL_VERSION_STATE";
    private static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";
    public static KommunicateSettings kommunicateSettings;
    private SharedPreferences sharedPreferences;
    private Context context;
    private ConversationBroadcastReceiver conversationBroadcastReceiver;

    private KommunicateSettings(Context context) {
        this.context = AppContextService.getContext(context);
        this.sharedPreferences = this.context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static KommunicateSettings init(Context context, String applicationKey) {
        kommunicateSettings = getInstance(context);
        PrefSettings.getInstance(context).setApplicationKey(applicationKey);
        return kommunicateSettings;
    }
    public static void setDefaultLanguage(Context context){
        String deviceLanguage = context.getResources().getConfiguration().locale.getLanguage();
        if(TextUtils.isEmpty(deviceLanguage)){
            return;
        }
        PrefSettings.getInstance(context).setDeviceDefaultLanguageToBot(deviceLanguage);
        Map<String, String> localeMetadata = new HashMap<>();
        localeMetadata.put(KM_USER_LOCALE,deviceLanguage);
        KmSettings.updateChatContext(context,localeMetadata);
    }

    public static KommunicateSettings getInstance(Context context) {
        if (kommunicateSettings == null) {
            kommunicateSettings = new KommunicateSettings(AppContextService.getContext(context));
        }
        return kommunicateSettings;
    }

    public void setGeoApiKey(String geoApiKey) {
        PrefSettings.getInstance(context).setGeoApiKey(geoApiKey);
    }

    public String getGeoApiKey() {
        String geoApiKey = PrefSettings.getInstance(context).getGeoApiKey();
        if (!TextUtils.isEmpty(geoApiKey)) {
            return geoApiKey;
        }
        return Utils.getMetaDataValue(context, PrefSettings.GOOGLE_API_KEY_META_DATA);
    }

    public String getApplicationKey() {
        String decryptedApplicationKey = PrefSettings.getInstance(context).getApplicationKey();
        if (!TextUtils.isEmpty(decryptedApplicationKey)) {
            return decryptedApplicationKey;
        }
        String existingAppKey = sharedPreferences.getString(APPLICATION_KEY, null);
        if (!TextUtils.isEmpty(existingAppKey)) {
            PrefSettings.getInstance(context).setApplicationKey(existingAppKey);
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

    public KommunicateSettings setDeviceRegistrationId(String registrationId) {
        sharedPreferences.edit().putString(DEVICE_REGISTRATION_ID, registrationId).commit();
        return this;
    }

    public KommunicateSettings setCustomNotificationSound(String filePath) {
        sharedPreferences.edit().putString(CUSTOM_NOTIFICATION_SOUND, filePath).commit();
        return this;
    }

    public String getCustomNotificationSound() {
        return sharedPreferences.getString(CUSTOM_NOTIFICATION_SOUND, null);
    }

    public static void disconnectPublish(Context context, String deviceKeyString, String userKeyString, boolean useEncrypted) {
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            Intent intent = new Intent(context, MqttIntentService.class);
            intent.putExtra(MqttIntentService.USER_KEY_STRING, userKeyString);
            intent.putExtra(MqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
            intent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
            MqttIntentService.enqueueWork(context, intent);
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
        AuthService.verifyToken(context, loadingMessage, new ResultCallback() {
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
        Intent subscribeIntent = new Intent(context, MqttIntentService.class);
        subscribeIntent.putExtra(MqttIntentService.SUBSCRIBE, true);
        subscribeIntent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        MqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, MqttIntentService.class);
        subscribeIntent.putExtra(MqttIntentService.CONNECT_TO_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        MqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void unSubscribeToSupportGroup(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, MqttIntentService.class);
        subscribeIntent.putExtra(MqttIntentService.DISCONNECT_FROM_SUPPORT_GROUP_TOPIC, true);
        subscribeIntent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        MqttIntentService.enqueueWork(context, subscribeIntent);
    }

    public static void subscribeToTeamTopic(Context context, boolean useEncrypted, ArrayList<String> teams) {
        Intent subscribeIntent = new Intent(context, MqttIntentService.class);
        subscribeIntent.putExtra(MqttIntentService.CONNECT_TO_TEAM_TOPIC, true);
        subscribeIntent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        subscribeIntent.putStringArrayListExtra(MqttIntentService.TEAM_TOPIC_LIST, teams);
        MqttIntentService.enqueueWork(context, subscribeIntent);
    }
    public static void unSubscribeToTeamTopic(Context context, boolean useEncrypted) {
        Intent subscribeIntent = new Intent(context, MqttIntentService.class);
        subscribeIntent.putExtra(MqttIntentService.DISCONNECT_FROM_TEAM_TOPIC, true);
        subscribeIntent.putExtra(MqttIntentService.USE_ENCRYPTED_TOPIC, useEncrypted);
        MqttIntentService.enqueueWork(context, subscribeIntent);
    }
    public static void subscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, MqttIntentService.class);
        if (channel != null) {
            intent.putExtra(MqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(MqttIntentService.CONTACT, contact);
        }
        intent.putExtra(MqttIntentService.SUBSCRIBE_TO_TYPING, true);
        MqttIntentService.enqueueWork(context, intent);
    }

    public static void unSubscribeToTyping(Context context, Channel channel, Contact contact) {
        Intent intent = new Intent(context, MqttIntentService.class);
        if (channel != null) {
            intent.putExtra(MqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(MqttIntentService.CONTACT, contact);
        }
        intent.putExtra(MqttIntentService.UN_SUBSCRIBE_TO_TYPING, true);
        MqttIntentService.enqueueWork(context, intent);
    }

    public static void publishTypingStatus(Context context, Channel channel, Contact contact, boolean typingStarted) {
        Intent intent = new Intent(context, MqttIntentService.class);

        if (channel != null) {
            intent.putExtra(MqttIntentService.CHANNEL, channel);
        } else if (contact != null) {
            intent.putExtra(MqttIntentService.CONTACT, contact);
        }
        intent.putExtra(MqttIntentService.TYPING, typingStarted);
        MqttIntentService.enqueueWork(context, intent);
    }

    @Deprecated
    @CleanUpRequired(reason = "Not used anywhere")
    public static void loginUser(Context context, User user, LoginHandler loginHandler) {
        if (MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage(context.getString(R.string.user_logged_in));
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            UserLoginUseCase.Companion.executeWithExecutor(context, user, loginHandler);
        }
    }

    public static void connectUser(Context context, User user, LoginHandler loginHandler) {
        if (isConnected(context)) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage(context.getString(R.string.user_logged_in));
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
            UserLoginUseCase.Companion.executeWithExecutor(context, user, loginHandler);
        }
    }

    public static void connectUserWithoutCheck(Context context, User user, LoginHandler loginHandler) {
        UserLoginUseCase.Companion.executeWithExecutor(context, user, loginHandler);
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
    @CleanUpRequired(reason = "Not Used Anywhere")
    public static void loginUser(Context context, User user, boolean withLoggedInCheck, LoginHandler loginHandler) {
        if (withLoggedInCheck && MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage(context.getString(R.string.user_logged_in));
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            UserLoginUseCase.Companion.executeWithExecutor(context, user, loginHandler);
        }
    }

    public static void logoutUser(final Context context, LogoutHandler logoutHandler) {
        UserLogoutUseCase.executeWithExecutor(context, logoutHandler);
    }

    public static void registerForPushNotification(Context context, String pushToken, PushNotificationHandler handler) {
        PushNotificationUseCase.executeWithExecutor(context, pushToken, handler);
    }

    public static void registerForPushNotification(Context context, PushNotificationHandler handler) {
        registerForPushNotification(context, KommunicateSettings.getInstance(context).getDeviceRegistrationId(), handler);
    }


    @Deprecated
    public void registerUIListener(UIEventListener UIEventListener) {
        conversationBroadcastReceiver = new ConversationBroadcastReceiver(UIEventListener);
        LocalBroadcastManager.getInstance(context).registerReceiver(conversationBroadcastReceiver, BroadcastService.getIntentFilter());
    }

    @Deprecated
    public void unregisterUIListener() {
        if (conversationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(conversationBroadcastReceiver);
            conversationBroadcastReceiver = null;
        }
    }
}