package io.kommunicate.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import io.kommunicate.callbacks.KommunicateUIListener;
import io.kommunicate.data.api.MobiComKitConstants;
import io.kommunicate.data.conversation.Message;
import io.kommunicate.data.json.GsonUtils;
import io.kommunicate.utils.Utils;

/**
 * Created by reytum on 5/12/17.
 */

public class KommunicateBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "KommunicateUIReceiver";
    private KommunicateUIListener kommunicateUIListener;

    public KommunicateBroadcastReceiver(KommunicateUIListener listener) {
        this.kommunicateUIListener = listener;
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
                kommunicateUIListener.onMessageReceived(message);
            }
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                if (!message.isTypeOutbox()) {
                    kommunicateUIListener.onMessageReceived(message);
                }
            }
        }

        String keyString = intent.getStringExtra("keyString");
        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            kommunicateUIListener.onLoadMore(intent.getBooleanExtra("loadMore", true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            kommunicateUIListener.onMessageSent(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            kommunicateUIListener.onMessageSync(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra("contactNumbers");
            kommunicateUIListener.onMessageDeleted(keyString, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            kommunicateUIListener.onMessageDelivered(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            kommunicateUIListener.onAllMessagesDelivered(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            kommunicateUIListener.onAllMessagesRead(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra("contactNumber");
            Integer channelKey = intent.getIntExtra("channelKey", 0);
            String response = intent.getStringExtra("response");
            kommunicateUIListener.onConversationDeleted(contactNumber, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra("userId");
            String isTyping = intent.getStringExtra("isTyping");
            kommunicateUIListener.onUpdateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            kommunicateUIListener.onUpdateLastSeen(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            kommunicateUIListener.onMqttDisconnected();
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_CONNECTED.toString().equals(action)) {
            kommunicateUIListener.onMqttConnected();
        } else if (BroadcastService.INTENT_ACTIONS.USER_OFFLINE.toString().equals(action)) {
            kommunicateUIListener.onUserOffline();
        } else if (BroadcastService.INTENT_ACTIONS.USER_ONLINE.toString().equals(action)) {
            kommunicateUIListener.onUserOnline();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            kommunicateUIListener.onChannelUpdated();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra("currentId");
            boolean isGroup = intent.getBooleanExtra("isGroup", false);
            kommunicateUIListener.onConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            kommunicateUIListener.onUserDetailUpdated(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            kommunicateUIListener.onMessageMetadataUpdated(keyString);
        } else if (BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString().equals(action)) {
            kommunicateUIListener.onUserMute(intent.getBooleanExtra("mute", false), intent.getStringExtra("userId"));
        }
    }
}
