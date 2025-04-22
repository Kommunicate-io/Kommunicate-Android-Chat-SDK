package io.kommunicate.devkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.listners.UIEventListener;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;

/**
 * Created by reytum on 5/12/17.
 */

public class ConversationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ConversationBroadcastReceiver";
    private static final String LOAD_MORE = "loadMore";
    private static final String CONTACT_NUMBER = "contactNumber";
    private static final String contactId = "contactId";
    private static final String CHANNEL_KEY = "channelKey";
    private static final String RESPONSE = "response";
    private static final String USER_ID = "userId";
    private static final String CURRENT_ID = "currentId";
    private static final String IS_GROUP = "isGroup";
    private static final String IS_TYPING = "isTyping";

    private UIEventListener UIEventListener;

    public ConversationBroadcastReceiver(UIEventListener listener) {
        this.UIEventListener = listener;
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
                UIEventListener.onMessageReceived(message);
            }
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                if (!message.isTypeOutbox()) {
                    UIEventListener.onMessageReceived(message);
                }
            }
        }

        String keyString = intent.getStringExtra("keyString");
        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            UIEventListener.onLoadMore(intent.getBooleanExtra(LOAD_MORE, true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            UIEventListener.onMessageSent(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            UIEventListener.onMessageSync(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra(CONTACT_NUMBER);
            UIEventListener.onMessageDeleted(keyString, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            UIEventListener.onMessageDelivered(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            UIEventListener.onAllMessagesDelivered(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            UIEventListener.onAllMessagesRead(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra(CONTACT_NUMBER);
            Integer channelKey = intent.getIntExtra(CHANNEL_KEY, 0);
            String response = intent.getStringExtra(RESPONSE);
            UIEventListener.onConversationDeleted(contactNumber, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra(USER_ID);
            String isTyping = intent.getStringExtra(IS_TYPING);
            UIEventListener.onUpdateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            UIEventListener.onUpdateLastSeen(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            UIEventListener.onMqttDisconnected();
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_CONNECTED.toString().equals(action)) {
            UIEventListener.onMqttConnected();
        } else if (BroadcastService.INTENT_ACTIONS.USER_OFFLINE.toString().equals(action)) {
            UIEventListener.onUserOffline();
        } else if (BroadcastService.INTENT_ACTIONS.USER_ONLINE.toString().equals(action)) {
            UIEventListener.onUserOnline();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            UIEventListener.onChannelUpdated();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra(CURRENT_ID);
            boolean isGroup = intent.getBooleanExtra(IS_GROUP, false);
            UIEventListener.onConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            UIEventListener.onUserDetailUpdated(intent.getStringExtra(contactId));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            UIEventListener.onMessageMetadataUpdated(keyString);
        } else if (BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString().equals(action)) {
            UIEventListener.onUserMute(intent.getBooleanExtra("mute", false), intent.getStringExtra(USER_ID));
        }
    }
}
