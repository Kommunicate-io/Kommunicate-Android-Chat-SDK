package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.AlJobIntentService;

import android.text.TextUtils;

import io.kommunicate.devkit.api.MqttService;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

import java.util.List;

/**
 * Created by sunil on 30/12/15.
 */
public class MqttIntentService extends AlJobIntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String TAG = "MqttIntentService";
    public static final String SUBSCRIBE = "subscribe";
    public static final String SUBSCRIBE_TO_TYPING = "subscribeToTyping";
    public static final String UN_SUBSCRIBE_TO_TYPING = "unSubscribeToTyping";
    public static final String DEVICE_KEY_STRING = "deviceKeyString";
    public static final String USER_KEY_STRING = "userKeyString";
    public static final String CONNECTED_PUBLISH = "connectedPublish";
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    public static final String TYPING = "typing";
    public static final String STOP_TYPING = "STOP_TYPING";
    public static final String CONNECT_TO_SUPPORT_GROUP_TOPIC = "connectToSupportGroupTopic";
    public static final String DISCONNECT_FROM_SUPPORT_GROUP_TOPIC = "disconnectFromSupportGroupTopic";
    public static final String CONNECT_TO_TEAM_TOPIC = "connectToTeamTopic";
    public static final String DISCONNECT_FROM_TEAM_TOPIC = "disconnectFromTeamTopic";
    public static final String TEAM_TOPIC_LIST = "teamTopicList";


    public static final String USE_ENCRYPTED_TOPIC = "useEncryptedTopic";

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1110;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(AppContextService.getContext(context), MqttIntentService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent == null) {
            return;
        }
        boolean useEncryptedTopic = intent.getBooleanExtra(USE_ENCRYPTED_TOPIC, false);
        boolean subscribe = intent.getBooleanExtra(SUBSCRIBE, false);
        if (subscribe) {
            MqttService.getInstance(getApplicationContext()).subscribe(useEncryptedTopic);
        }
        Contact contact = (Contact) intent.getSerializableExtra(CONTACT);
        Channel channel = (Channel) intent.getSerializableExtra(CHANNEL);

        boolean subscribeToTyping = intent.getBooleanExtra(SUBSCRIBE_TO_TYPING, false);
        if (subscribeToTyping) {
            MqttService.getInstance(getApplicationContext()).subscribeToTypingTopic(channel);
            if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                MqttService.getInstance(getApplicationContext()).subscribeToOpenGroupTopic(channel);
            }
            return;
        }
        boolean unSubscribeToTyping = intent.getBooleanExtra(UN_SUBSCRIBE_TO_TYPING, false);
        if (unSubscribeToTyping) {
            MqttService.getInstance(getApplicationContext()).unSubscribeToTypingTopic(channel);
            if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                MqttService.getInstance(getApplicationContext()).unSubscribeToOpenGroupTopic(channel);
            }
            return;
        }

        boolean subscribeToSupportGroupTopic = intent.getBooleanExtra(CONNECT_TO_SUPPORT_GROUP_TOPIC, false);
        if (subscribeToSupportGroupTopic) {
            MqttService.getInstance(getApplicationContext()).subscribeToSupportGroup(useEncryptedTopic);
            return;
        }

        boolean unSubscribeToSupportGroupTopic = intent.getBooleanExtra(DISCONNECT_FROM_SUPPORT_GROUP_TOPIC, false);
        if (unSubscribeToSupportGroupTopic) {
            MqttService.getInstance(getApplicationContext()).unSubscribeToSupportGroup(useEncryptedTopic);
            return;
        }

        boolean subscribeToTeamTopic = intent.getBooleanExtra(CONNECT_TO_TEAM_TOPIC, false);
        List<String> teamList = intent.getStringArrayListExtra(TEAM_TOPIC_LIST);
        if (subscribeToTeamTopic) {
            MqttService.getInstance(getApplicationContext()).subscribeToTeamTopic(useEncryptedTopic, teamList);
            return;
        }
        boolean unSubscribeToTeamTopic = intent.getBooleanExtra(DISCONNECT_FROM_TEAM_TOPIC, false);
        if (unSubscribeToTeamTopic) {
            MqttService.getInstance(getApplicationContext()).unSubscribeToSupportGroup(useEncryptedTopic);
            return;
        }

        String userKeyString = intent.getStringExtra(USER_KEY_STRING);
        String deviceKeyString = intent.getStringExtra(DEVICE_KEY_STRING);
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            MqttService.getInstance(getApplicationContext()).disconnectPublish(userKeyString, deviceKeyString, "0", useEncryptedTopic);
        }

        boolean connectedStatus = intent.getBooleanExtra(CONNECTED_PUBLISH, false);
        if (connectedStatus) {
            MqttService.getInstance(getApplicationContext()).connectPublish(MobiComUserPreference.getInstance(getApplicationContext()).getSuUserKeyString(), MobiComUserPreference.getInstance(getApplicationContext()).getDeviceKeyString(), "1");
        }

        if (contact != null) {
            boolean stop = intent.getBooleanExtra(STOP_TYPING, false);
            if (stop) {
                MqttService.getInstance(getApplicationContext()).typingStopped(contact, null);
            }
        }

        if (contact != null && (contact.isBlocked() || contact.isBlockedBy())) {
            return;
        }

        if (contact != null || channel != null) {
            boolean typing = intent.getBooleanExtra(TYPING, false);
            if (typing) {
                MqttService.getInstance(getApplicationContext()).typingStarted(contact, channel);
            } else {
                MqttService.getInstance(getApplicationContext()).typingStopped(contact, channel);
            }
        }

    }
}