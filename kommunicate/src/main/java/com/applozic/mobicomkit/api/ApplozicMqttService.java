package com.applozic.mobicomkit.api;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.broadcast.AlMessageEvent;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GcmMessageResponse;
import com.applozic.mobicomkit.feed.InstantMessageResponse;
import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.encryption.EncryptionUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;

/**
 * Created by sunil on 26/11/15.
 */
public class ApplozicMqttService extends MobiComKitClientService implements MqttCallback {

    private static final String STATUS = "status-v2";
    private static final String TAG = "ApplozicMqttService";
    private static final String TYPINGTOPIC = "typing-";
    private static final String OPEN_GROUP = "group-";
    private static final String MQTT_ENCRYPTION_TOPIC = "encr-";
    private static final String SUPPORT_GROUP_TOPIC = "support-channel-";
    private static ApplozicMqttService applozicMqttService;
    private AlMqttClient client;
    private MemoryPersistence memoryPersistence;
    private Context context;

    private ApplozicMqttService(Context context) {
        super(context);
        this.context = context;
        memoryPersistence = new MemoryPersistence();
    }

    public static ApplozicMqttService getInstance(Context context) {

        if (applozicMqttService == null) {
            applozicMqttService = new ApplozicMqttService(context.getApplicationContext());
        }
        return applozicMqttService;
    }

    private MqttConnectOptions getConnectionOptions() {
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        String authToken = userPreference.getUserAuthToken();

        MqttConnectOptions connOpts = new MqttConnectOptions();

        if (!TextUtils.isEmpty(authToken)) {
            connOpts.setUserName(getApplicationKey(context));
            connOpts.setPassword(authToken.toCharArray());
        }
        connOpts.setConnectionTimeout(60);
        connOpts.setWill(STATUS, (userPreference.getSuUserKeyString() + "," + userPreference.getDeviceKeyString() + "," + "0").getBytes(), 0, true);
        return connOpts;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    private AlMqttClient connect() {
        String userId = MobiComUserPreference.getInstance(context).getUserId();
        try {
            if (TextUtils.isEmpty(userId)) {
                return client;
            }
            if (client == null) {
                client = new AlMqttClient(getMqttBaseUrl(), userId + "-" + new Date().getTime(), memoryPersistence);
            }

            if (!client.isConnected()) {
                Utils.printLog(context, TAG, "Connecting to mqtt...");
                client.setCallback(ApplozicMqttService.this);
                client.connectWithResult(getConnectionOptions(), new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Utils.printLog(context, TAG, "Mqtt Connection successfull to : " + client.getServerURI());
                        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_CONNECTED.toString());
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Utils.printLog(context, TAG, "Mqtt Connection failed");
                        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
                    }
                });
            }
        } catch (MqttException e) {
            Utils.printLog(context, TAG, "Connecting already in progress.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return client;
    }

    public synchronized void connectPublish(final String userKeyString, final String deviceKeyString, final String status) {

        try {
            final AlMqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            MqttMessage message = new MqttMessage();
            message.setRetained(false);
            message.setPayload((userKeyString + "," + deviceKeyString + "," + status).getBytes());
            Utils.printLog(context, TAG, "UserKeyString,DeviceKeyString,status:" + userKeyString + "," + deviceKeyString + "," + status);
            message.setQos(0);
            client.publish(STATUS, message, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    BroadcastService.sendUpdate(context, "1".equals(status) ? BroadcastService.INTENT_ACTIONS.USER_ONLINE.toString() : BroadcastService.INTENT_ACTIONS.USER_OFFLINE.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(boolean useEncrypted) {
        if (!Utils.isInternetAvailable(context)) {
            return;
        }
        final String deviceKeyString = MobiComUserPreference.getInstance(context).getDeviceKeyString();
        final String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
        if (TextUtils.isEmpty(deviceKeyString) || TextUtils.isEmpty(userKeyString)) {
            return;
        }
        try {
            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            connectPublish(userKeyString, deviceKeyString, "1");
            subscribeToConversation(useEncrypted);
            if (client != null) {
                client.setCallback(ApplozicMqttService.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribe(boolean useEncrypted) {
        unSubscribeToConversation(useEncrypted);
    }

    public synchronized void subscribeToConversation(boolean useEncrypted) {
        try {
            String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
            if (TextUtils.isEmpty(userKeyString)) {
                return;
            }
            if (client != null && client.isConnected()) {
                Utils.printLog(context, TAG, "Subscribing to conversation topic : " + (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + userKeyString);
                client.subscribe((useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + userKeyString, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToConversation(final boolean useEncrypted) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client == null || !client.isConnected()) {
                        return;
                    }
                    String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
                    Utils.printLog(context, TAG, "UnSubscribing to conversation topic : " + (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + userKeyString);
                    client.unsubscribe((useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + userKeyString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    public synchronized void subscribeToCustomTopic(String customTopic, boolean useEncrypted) {
        try {
            final MqttClient client = connect();
            if (client != null && client.isConnected()) {
                String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic;
                Utils.printLog(context, TAG, "Subscribing to topic : " + topic);
                client.subscribe(topic, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToCustomTopic(final String customTopic, final boolean useEncrypted) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client == null || !client.isConnected()) {
                        return;
                    }
                    String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic;
                    Utils.printLog(context, TAG, "UnSubscribing from topic : " + topic);
                    client.unsubscribe(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    public synchronized void publishCustomData(final String customTopic, final String data, final boolean useEncrypted) {
        try {
            final AlMqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            MqttMessage message = new MqttMessage();
            message.setRetained(false);
            message.setPayload(data.getBytes());
            message.setQos(0);
            final String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + customTopic;
            client.publish(topic, message, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Utils.printLog(context, TAG, "Sent data : " + data + " to topic : " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Utils.printLog(context, TAG, "Error in sending data : " + data + " to topic : " + topic);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeToSupportGroup(boolean useEncrypted) {
        try {
            String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
            if (TextUtils.isEmpty(userKeyString)) {
                return;
            }
            final MqttClient client = connect();
            if (client != null && client.isConnected()) {
                String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + SUPPORT_GROUP_TOPIC + "-" + getApplicationKey(context);
                Utils.printLog(context, TAG, "Subscribing to support group topic : " + topic);
                client.subscribe(topic, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToSupportGroup(final boolean useEncrypted) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client == null || !client.isConnected()) {
                        return;
                    }
                    String topic = (useEncrypted ? MQTT_ENCRYPTION_TOPIC : "") + SUPPORT_GROUP_TOPIC + "-" + getApplicationKey(context);
                    Utils.printLog(context, TAG, "UnSubscribing to support group topic : " + topic);
                    client.unsubscribe(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    public void disconnectPublish(String userKeyString, String deviceKeyString, String status, boolean useEncrypted) {
        try {
            connectPublish(userKeyString, deviceKeyString, status);
            unSubscribe(useEncrypted);
            if (!MobiComUserPreference.getInstance(context).isLoggedIn()) {
                disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isMqttResponseForLoggedInUserDelete(Context context, MqttMessageResponse mqttMessageResponse) {
        String userIdFromMqttResponse = mqttMessageResponse.getMessage().toString();
        return NOTIFICATION_TYPE.USER_DELETE_NOTIFICATION.getValue().equals(mqttMessageResponse.getType()) && !TextUtils.isEmpty(userIdFromMqttResponse) && userIdFromMqttResponse.equals(MobiComUserPreference.getInstance(context).getUserId());
    }

    @Override
    public void connectionLost(Throwable throwable) {
        BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
    }

    @Override
    public void messageArrived(final String s, final MqttMessage mqttMessage) throws Exception {
        Utils.printLog(context, TAG, "Received MQTT message: " + new String(mqttMessage.getPayload()));
        try {
            if (!TextUtils.isEmpty(s) && s.startsWith(TYPINGTOPIC)) {
                String[] typingResponse = mqttMessage.toString().split(",");
                String applicationId = typingResponse[0];
                String userId = User.getDecodedUserId(typingResponse[1]);
                String isTypingStatus = typingResponse[2];
                BroadcastService.sendUpdateTypingBroadcast(context, BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString(), applicationId, userId, isTypingStatus);
            } else {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final MqttMessageResponse mqttMessageResponse;
                            String messageDataString = null;

                            if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getUserEncryptionKey())
                                    && !TextUtils.isEmpty(s) && s.startsWith(MQTT_ENCRYPTION_TOPIC)) {
                                if (!TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getUserEncryptionKey())) {
                                    messageDataString = EncryptionUtils.decrypt(MobiComUserPreference.getInstance(context).getUserEncryptionKey(), mqttMessage.toString());
                                }
                                if (TextUtils.isEmpty(messageDataString.trim())) {
                                    return;
                                }
                                mqttMessageResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(messageDataString, MqttMessageResponse.class);
                            } else {
                                messageDataString = mqttMessage.toString();
                                mqttMessageResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(messageDataString, MqttMessageResponse.class);
                            }

                            if (mqttMessageResponse != null) {
                                if (MobiComPushReceiver.processPushNotificationId(mqttMessageResponse.getId())) {
                                    return;
                                }
                                final SyncCallService syncCallService = SyncCallService.getInstance(context);
                                MobiComPushReceiver.addPushNotificationId(mqttMessageResponse.getId());

                                AlEventManager.getInstance().postMqttEventData(mqttMessageResponse);

                                Utils.printLog(context, TAG, "MQTT message type: " + mqttMessageResponse.getType());
                                if (NOTIFICATION_TYPE.MESSAGE_RECEIVED.getValue().equals(mqttMessageResponse.getType()) || "MESSAGE_RECEIVED".equals(mqttMessageResponse.getType())) {

                                    GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageDataString, GcmMessageResponse.class);
                                    if (messageResponse == null) {
                                        return;
                                    }
                                    Message message = messageResponse.getMessage();
                                    if (message.getGroupId() != null) {
                                        Channel channel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());

                                        if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                                            if (!MobiComUserPreference.getInstance(context).getDeviceKeyString().equals(message.getDeviceKeyString())) {
                                                syncCallService.syncMessages(message.getKeyString(), message);
                                            }
                                        } else {
                                            if (message.isGroupDeleteAction()) {
                                                syncCallService.deleteChannelConversationThread(message.getGroupId());
                                                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, message.getGroupId(), "success");
                                            }
                                            syncCallService.syncMessages(null);
                                        }
                                    } else {
                                        syncCallService.syncMessages(null);
                                    }
                                }

                                if (NOTIFICATION_TYPE.MESSAGE_DELIVERED.getValue().equals(mqttMessageResponse.getType())
                                        || "MT_MESSAGE_DELIVERED".equals(mqttMessageResponse.getType())) {
                                    String[] splitKeyString = (mqttMessageResponse.getMessage()).toString().split(",");
                                    String keyString = splitKeyString[0];
                                    //String userId = splitKeyString[1];
                                    syncCallService.updateDeliveryStatus(keyString);
                                }

                                if (NOTIFICATION_TYPE.MESSAGE_DELIVERED_AND_READ.getValue().equals(mqttMessageResponse.getType())
                                        || "MT_MESSAGE_DELIVERED_READ".equals(mqttMessageResponse.getType())) {
                                    String[] splitKeyString = (mqttMessageResponse.getMessage()).toString().split(",");
                                    String keyString = splitKeyString[0];
                                    syncCallService.updateReadStatus(keyString);
                                }

                                if (NOTIFICATION_TYPE.CONVERSATION_DELIVERED_AND_READ.getValue().equals(mqttMessageResponse.getType())) {
                                    String contactId = mqttMessageResponse.getMessage().toString();
                                    syncCallService.updateDeliveryStatusForContact(contactId, true);
                                }

                                if (NOTIFICATION_TYPE.CONVERSATION_READ.getValue().equals(mqttMessageResponse.getType())) {
                                    syncCallService.updateConversationReadStatus(mqttMessageResponse.getMessage().toString(), false);
                                }

                                if (NOTIFICATION_TYPE.GROUP_CONVERSATION_READ.getValue().equals(mqttMessageResponse.getType())) {
                                    InstantMessageResponse instantMessageResponse = (InstantMessageResponse) GsonUtils.getObjectFromJson(messageDataString, InstantMessageResponse.class);
                                    syncCallService.updateConversationReadStatus(instantMessageResponse.getMessage(), true);
                                }

                                if (NOTIFICATION_TYPE.USER_CONNECTED.getValue().equals(mqttMessageResponse.getType())) {
                                    syncCallService.updateConnectedStatus(mqttMessageResponse.getMessage().toString(), new Date(), true);
                                }

                                if (NOTIFICATION_TYPE.USER_DISCONNECTED.getValue().equals(mqttMessageResponse.getType())) {
                                    //disconnect comes with timestamp, ranjeet,1449866097000
                                    String[] parts = mqttMessageResponse.getMessage().toString().split(",");
                                    String userId = parts[0];
                                    Date lastSeenAt = new Date();
                                    if (parts.length >= 2 && !parts[1].equals("null")) {
                                        lastSeenAt = new Date(Long.parseLong(parts[1]));
                                    }
                                    syncCallService.updateConnectedStatus(userId, lastSeenAt, false);
                                }

                                if (NOTIFICATION_TYPE.CONVERSATION_DELETED.getValue().equals(mqttMessageResponse.getType())) {
                                    syncCallService.deleteConversationThread(mqttMessageResponse.getMessage().toString());
                                    BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), mqttMessageResponse.getMessage().toString(), 0, "success");
                                }

                                if (NOTIFICATION_TYPE.GROUP_CONVERSATION_DELETED.getValue().equals(mqttMessageResponse.getType())) {
                                    InstantMessageResponse instantMessageResponse = (InstantMessageResponse) GsonUtils.getObjectFromJson(messageDataString, InstantMessageResponse.class);
                                    syncCallService.deleteChannelConversationThread(instantMessageResponse.getMessage());
                                    BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, Integer.valueOf(instantMessageResponse.getMessage()), "success");
                                }

                                if (NOTIFICATION_TYPE.MESSAGE_DELETED.getValue().equals(mqttMessageResponse.getType())) {
                                    String messageKey = mqttMessageResponse.getMessage().toString().split(",")[0];
                                    syncCallService.deleteMessage(messageKey);
                                    BroadcastService.sendMessageDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString(), messageKey, null);
                                }

                                if (NOTIFICATION_TYPE.MESSAGE_SENT.getValue().equals(mqttMessageResponse.getType())) {
                                    GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageDataString, GcmMessageResponse.class);
                                    Message sentMessageSync = messageResponse.getMessage();
                                    syncCallService.syncMessages(sentMessageSync.getKeyString());
                                }

                                if (NOTIFICATION_TYPE.USER_BLOCKED.getValue().equals(mqttMessageResponse.getType()) ||
                                        NOTIFICATION_TYPE.USER_UN_BLOCKED.getValue().equals(mqttMessageResponse.getType())) {
                                    syncCallService.syncBlockUsers();
                                }

                                if (NOTIFICATION_TYPE.USER_DETAIL_CHANGED.getValue().equals(mqttMessageResponse.getType()) || NOTIFICATION_TYPE.USER_DELETE_NOTIFICATION.getValue().equals(mqttMessageResponse.getType())) {
                                    String userId = mqttMessageResponse.getMessage().toString();
                                    syncCallService.syncUserDetail(userId);

                                    if (isMqttResponseForLoggedInUserDelete(context, mqttMessageResponse)) {
                                        syncCallService.processLoggedUserDelete();
                                    }
                                }

                                if (NOTIFICATION_TYPE.MESSAGE_METADATA_UPDATE.getValue().equals(mqttMessageResponse.getType())) {
                                    try {
                                        GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageDataString, GcmMessageResponse.class);
                                        String keyString = messageResponse.getMessage().getKeyString();
                                        Message messageObject = messageResponse.getMessage();
                                        syncCallService.syncMessageMetadataUpdate(keyString, false, messageObject);
                                    } catch (Exception e) {
                                        Utils.printLog(context, TAG, e.getMessage());
                                    }
                                }

                                if (NOTIFICATION_TYPE.USER_MUTE_NOTIFICATION.getValue().equals(mqttMessageResponse.getType())) {
                                    try {
                                        InstantMessageResponse response = (InstantMessageResponse) GsonUtils.getObjectFromJson(messageDataString, InstantMessageResponse.class);
                                        if (response.getMessage() != null) {
                                            String muteFlag = String.valueOf(response.getMessage().charAt(response.getMessage().length() - 1));
                                            if ("1".equals(muteFlag)) {
                                                syncCallService.syncMutedUserList(false, null);
                                            } else if ("0".equals(muteFlag)) {
                                                String userId = response.getMessage().substring(0, response.getMessage().length() - 2);
                                                syncCallService.syncMutedUserList(false, userId);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (NOTIFICATION_TYPE.MUTE_NOTIFICATIONS.getValue().equals(mqttMessageResponse.getType())) {
                                    try {
                                        GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageDataString, GcmMessageResponse.class);
                                        if (messageResponse.getMessage() != null && messageResponse.getMessage().getMessage() != null) {
                                            long notificationAfterTime = Long.parseLong(messageResponse.getMessage().getMessage());
                                            ALSpecificSettings.getInstance(context).setNotificationAfterTime(notificationAfterTime);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (NOTIFICATION_TYPE.GROUP_MUTE_NOTIFICATION.getValue().equals(mqttMessageResponse.getType())) {
                                    try {
                                        InstantMessageResponse response = (InstantMessageResponse) GsonUtils.getObjectFromJson(messageDataString, InstantMessageResponse.class);
                                        if (!TextUtils.isEmpty(response.getMessage())) {
                                            String[] parts = response.getMessage().split(":");
                                            if (parts.length > 0) {
                                                Integer groupId = Integer.parseInt(parts[0]);
                                                if (parts.length == 2) {
                                                    Long notificationMuteTillTime = Long.parseLong(parts[1]);
                                                    ChannelService.getInstance(context).updateNotificationAfterTime(groupId, notificationMuteTillTime);
                                                    BroadcastService.sendUpdateGroupMuteForGroupId(context, groupId, BroadcastService.INTENT_ACTIONS.GROUP_MUTE.toString());
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (NOTIFICATION_TYPE.ACTIVATED.getValue().equals(mqttMessageResponse.getType())) {
                                    BroadcastService.sendUserActivatedBroadcast(context, AlMessageEvent.ActionType.USER_ACTIVATED);
                                }

                                if (NOTIFICATION_TYPE.DEACTIVATED.getValue().equals(mqttMessageResponse.getType())) {
                                    BroadcastService.sendUserActivatedBroadcast(context, AlMessageEvent.ActionType.USER_DEACTIVATED);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void publishMessageStatus(final String messageStatusTopic, final String data) {
        try {
            final AlMqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            MqttMessage message = new MqttMessage();
            message.setRetained(false);
            message.setPayload(data.getBytes());
            message.setQos(0);
            client.publish(messageStatusTopic, message, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Utils.printLog(context, TAG, "Sent data : " + data + " to topic : " + messageStatusTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Utils.printLog(context, TAG, "Error in sending data : " + data + " to topic : " + messageStatusTopic);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void publishTopic(final String applicationId, final String status, final String loggedInUserId, final String userId) {
        try {
            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }
            MqttMessage message = new MqttMessage();
            message.setRetained(false);
            message.setPayload((applicationId + "," + User.getEncodedUserId(loggedInUserId) + "," + status).getBytes());
            message.setQos(0);
            client.publish("typing" + "-" + applicationId + "-" + User.getEncodedUserId(userId), message);
            Utils.printLog(context, TAG, "Published " + new String(message.getPayload()) + " to topic: " + "typing" + "-" + applicationId + "-" + User.getEncodedUserId(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeToTypingTopic(Channel channel) {
        try {
            String currentId = null;
            if (channel != null) {
                currentId = String.valueOf(channel.getKey());
            } else {
                MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
                currentId = mobiComUserPreference.getUserId();
            }

            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }

            client.subscribe("typing-" + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId), 0);
            Utils.printLog(context, TAG, "Subscribed to topic: " + "typing-" + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToTypingTopic(Channel channel) {
        try {
            String currentId = null;
            if (channel != null) {
                currentId = String.valueOf(channel.getKey());
            } else {
                MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
                currentId = mobiComUserPreference.getUserId();
            }

            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }

            client.unsubscribe("typing-" + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
            Utils.printLog(context, TAG, "UnSubscribed to topic: " + "typing-" + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void typingStarted(Contact contact, Channel channel) {
        String currentId;
        if (channel != null) {
            currentId = String.valueOf(channel.getKey());
        } else {
            currentId = contact.getUserId();
        }
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        publishTopic(getApplicationKey(context), "1", User.getEncodedUserId(mobiComUserPreference.getUserId()), User.getEncodedUserId(currentId));
    }

    public void typingStopped(Contact contact, Channel channel) {
        String currentId;
        if (channel != null) {
            currentId = String.valueOf(channel.getKey());
        } else {
            currentId = contact.getUserId();
        }
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        publishTopic(getApplicationKey(context), "0", User.getEncodedUserId(mobiComUserPreference.getUserId()), User.getEncodedUserId(currentId));
    }

    public synchronized void subscribeToOpenGroupTopic(Channel channel) {
        try {
            String currentId = null;
            if (channel != null) {
                currentId = String.valueOf(channel.getKey());
            }
            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }

            client.subscribe(OPEN_GROUP + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId), 0);
            Utils.printLog(context, TAG, "Subscribed to Open group: " + OPEN_GROUP + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unSubscribeToOpenGroupTopic(Channel channel) {
        try {
            String currentId = null;
            if (channel != null) {
                currentId = String.valueOf(channel.getKey());
            }

            final MqttClient client = connect();
            if (client == null || !client.isConnected()) {
                return;
            }

            client.unsubscribe(OPEN_GROUP + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
            Utils.printLog(context, TAG, "UnSubscribed to topic: " + OPEN_GROUP + getApplicationKey(context) + "-" + User.getEncodedUserId(currentId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static enum NOTIFICATION_TYPE {
        MESSAGE_RECEIVED("APPLOZIC_01"), MESSAGE_SENT("APPLOZIC_02"),
        MESSAGE_SENT_UPDATE("APPLOZIC_03"), MESSAGE_DELIVERED("APPLOZIC_04"),
        MESSAGE_DELETED("APPLOZIC_05"), CONVERSATION_DELETED("APPLOZIC_06"),
        MESSAGE_READ("APPLOZIC_07"), MESSAGE_DELIVERED_AND_READ("APPLOZIC_08"),
        CONVERSATION_READ("APPLOZIC_09"), CONVERSATION_DELIVERED_AND_READ("APPLOZIC_10"),
        USER_CONNECTED("APPLOZIC_11"), USER_DISCONNECTED("APPLOZIC_12"),
        GROUP_DELETED("APPLOZIC_13"), GROUP_LEFT("APPLOZIC_14"), GROUP_SYNC("APPLOZIC_15"),
        USER_BLOCKED("APPLOZIC_16"), USER_UN_BLOCKED("APPLOZIC_17"),
        ACTIVATED("APPLOZIC_18"),
        DEACTIVATED("APPLOZIC_19"),
        REGISTRATION("APPLOZIC_20"),
        GROUP_CONVERSATION_READ("APPLOZIC_21"),
        GROUP_MESSAGE_DELETED("APPLOZIC_22"),
        GROUP_CONVERSATION_DELETED("APPLOZIC_23"),
        APPLOZIC_TEST("APPLOZIC_24"),
        USER_ONLINE_STATUS("APPLOZIC_25"),
        CONVERSATION_DELETED_NEW("APPLOZIC_27"),
        CONVERSATION_DELIVERED_AND_READ_NEW("APPLOZIC_28"),
        CONVERSATION_READ_NEW("APPLOZIC_29"),
        USER_DETAIL_CHANGED("APPLOZIC_30"),
        MESSAGE_METADATA_UPDATE("APPLOZIC_33"),
        USER_DELETE_NOTIFICATION("APPLOZIC_34"),
        USER_MUTE_NOTIFICATION("APPLOZIC_37"),
        MUTE_NOTIFICATIONS("APPLOZIC_38"),
        GROUP_MUTE_NOTIFICATION("APPLOZIC_39");

        private String value;

        private NOTIFICATION_TYPE(String c) {
            value = c;
        }

        public String getValue() {
            return String.valueOf(value);
        }
    }

}
