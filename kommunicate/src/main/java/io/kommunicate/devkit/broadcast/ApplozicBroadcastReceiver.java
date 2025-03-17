package io.kommunicate.devkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.listners.ApplozicUIListener;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;

/**
 * Created by reytum on 5/12/17.
 */

public class ApplozicBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ApplozicUIReceiver";
    private static final String LOAD_MORE = "loadMore";
    private static final String CONTACT_NUMBER = "contactNumber";
    private static final String contactId = "contactId";
    private static final String CHANNEL_KEY = "channelKey";
    private static final String RESPONSE = "response";
    private static final String USER_ID = "userId";
    private static final String CURRENT_ID = "currentId";
    private static final String IS_GROUP = "isGroup";
    private static final String IS_TYPING = "isTyping";

    private ApplozicUIListener applozicUIListener;

    public ApplozicBroadcastReceiver(ApplozicUIListener listener) {
        this.applozicUIListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = null;
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
        }
        Utils.printLog(context, TAG, "Received broadcast, action: " + action + ", message: " + message);

        if (message != null && !message.isSentToMany()) {
            if (!message.isTypeOutbox()) {
                applozicUIListener.onMessageReceived(message);
            }
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                if (!message.isTypeOutbox()) {
                    applozicUIListener.onMessageReceived(message);
                }
            }
        }

        String keyString = intent.getStringExtra("keyString");
        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            applozicUIListener.onLoadMore(intent.getBooleanExtra(LOAD_MORE, true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            applozicUIListener.onMessageSent(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            applozicUIListener.onMessageSync(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra(CONTACT_NUMBER);
            applozicUIListener.onMessageDeleted(keyString, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            applozicUIListener.onMessageDelivered(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            applozicUIListener.onAllMessagesDelivered(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            applozicUIListener.onAllMessagesRead(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra(CONTACT_NUMBER);
            Integer channelKey = intent.getIntExtra(CHANNEL_KEY, 0);
            String response = intent.getStringExtra(RESPONSE);
            applozicUIListener.onConversationDeleted(contactNumber, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra(USER_ID);
            String isTyping = intent.getStringExtra(IS_TYPING);
            applozicUIListener.onUpdateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            applozicUIListener.onUpdateLastSeen(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            applozicUIListener.onMqttDisconnected();
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_CONNECTED.toString().equals(action)) {
            applozicUIListener.onMqttConnected();
        } else if (BroadcastService.INTENT_ACTIONS.USER_OFFLINE.toString().equals(action)) {
            applozicUIListener.onUserOffline();
        } else if (BroadcastService.INTENT_ACTIONS.USER_ONLINE.toString().equals(action)) {
            applozicUIListener.onUserOnline();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            applozicUIListener.onChannelUpdated();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra(CURRENT_ID);
            boolean isGroup = intent.getBooleanExtra(IS_GROUP, false);
            applozicUIListener.onConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            applozicUIListener.onUserDetailUpdated(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            applozicUIListener.onMessageMetadataUpdated(keyString);
        } else if (BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString().equals(action)) {
            applozicUIListener.onUserMute(intent.getBooleanExtra("mute", false), intent.getStringExtra(USER_ID));
        }
    }
}
