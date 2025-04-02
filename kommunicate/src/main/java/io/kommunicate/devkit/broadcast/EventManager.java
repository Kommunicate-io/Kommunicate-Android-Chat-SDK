package io.kommunicate.devkit.broadcast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import io.kommunicate.devkit.feed.MqttMessageResponse;
import io.kommunicate.devkit.listners.MqttListener;
import io.kommunicate.devkit.listners.UIEventListener;
import io.kommunicate.devkit.listners.KmConversationInfoListener;
import io.kommunicate.devkit.listners.KmStatusListener;
import io.kommunicate.commons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import io.kommunicate.callbacks.KmPluginEventListener;

/**
 * Handles real time update events that are registered to listen for.
 * multiple listeners can be registered by unique id
 */
public class EventManager {
    public static final String AL_EVENT = "AL_EVENT";
    private static EventManager eventManager;
    private Map<String, UIEventListener> listenerMap;
    private Map<String, MqttListener> mqttListenerMap;
    private Map<String, KmStatusListener> statusListenerMap;
    private KmPluginEventListener kmPluginEventListener;
    private KmConversationInfoListener kmConversationInfoListener;
    private Handler uiHandler;

    public static EventManager getInstance() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }

    public void registerUIListener(String id, UIEventListener listener) {
        if (listenerMap == null) {
            listenerMap = new HashMap<>();
        }
        initHandler();
        if (!listenerMap.containsKey(id)) {
            listenerMap.put(id, listener);
        }
    }

    public void unregisterUIListener(String id) {
        if (listenerMap != null) {
            listenerMap.remove(id);
        }
    }

    public void registerMqttListener(String id, MqttListener mqttListener) {
        if (mqttListenerMap == null) {
            mqttListenerMap = new HashMap<>();
        }

        if (!mqttListenerMap.containsKey(id)) {
            mqttListenerMap.put(id, mqttListener);
        }
    }

    public void unregisterMqttListener(String id) {
        if (mqttListenerMap != null) {
            mqttListenerMap.remove(id);
        }
    }

    public void registerStatusListener(String id, KmStatusListener listener) {
        if (statusListenerMap == null) {
            statusListenerMap = new HashMap<>();
        }
        initHandler();
        if (!statusListenerMap.containsKey(id)) {
            statusListenerMap.put(id, listener);
        }
    }

    public void unregisterStatusListener(String id) {
        if (statusListenerMap != null) {
            statusListenerMap.remove(id);
        }
    }

    void postEventData(MessageEvent messageEvent) {
        if (uiHandler != null) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(AL_EVENT, GsonUtils.getJsonFromObject(messageEvent, MessageEvent.class));
            message.setData(bundle);
            uiHandler.sendMessage(message);
        }
    }

    public void postMqttEventData(MqttMessageResponse messageResponse) {
        if (mqttListenerMap != null && !mqttListenerMap.isEmpty()) {
            for (MqttListener mqttListener : mqttListenerMap.values()) {
                mqttListener.onMqttMessageReceived(messageResponse);
            }
        }
    }

    public void registerPluginEventListener(KmPluginEventListener kmPluginEventListener) {
        this.kmPluginEventListener = kmPluginEventListener;
        initHandler();
    }

    public void registerConversationInfoListener(KmConversationInfoListener kmConversationInfoListener) {
        this.kmConversationInfoListener = kmConversationInfoListener;
    }

    public void sendOnConversationInfoClicked() {
        if(kmConversationInfoListener != null) {
            kmConversationInfoListener.onConversationInfoClicked();
        }
    }

    private void initHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    handleState(msg);
                    super.handleMessage(msg);
                }
            };
        }
    }

    public void unregisterPluginEventListener() {
        this.kmPluginEventListener = null;
    }

    public void sendOnPluginLaunchEvent() {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onPluginLaunch();
        }
    }

    public void sendOnPluginDismissedEvent() {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onPluginDismiss();
        }
    }

    public void sendOnConversationResolvedEvent(io.kommunicate.devkit.api.conversation.Message message) {
        if (kmPluginEventListener != null && message.isTypeResolved() && message.getGroupId() != null) {
            kmPluginEventListener.onConversationResolved(message.getGroupId());
        }
    }

    public void sendOnConversationRestartedEvent(Integer conversationId) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onConversationRestarted(conversationId);
        }
    }

    public void sendOnRichMessageButtonClickEvent(Integer conversationId, String actionType, Object action) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onRichMessageButtonClick(conversationId, actionType, action);
        }
    }

    public void sendOnStartNewConversation(Integer conversationId) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onStartNewConversation(conversationId);
        }

    }
    public void sendOnBackButtonClicked(boolean isConversationOpened) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onBackButtonClicked(isConversationOpened);
        }
    }

    public void sendOnSubmitRatingClicked(Integer conversationId, Integer rating, String feedback) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onSubmitRatingClick(conversationId, rating, feedback);
        }
    }

    public void sendOnMessageSent(io.kommunicate.devkit.api.conversation.Message message) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onMessageSent(message);
        }
    }

    public void sendOnMessageReceived(io.kommunicate.devkit.api.conversation.Message message) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onMessageReceived(message);
        }
    }

    public void sendOnAttachmentClick(String attachmentType) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onAttachmentClick(attachmentType);
        }
    }
    public void sendOnFaqClick(String FaqUrl) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onFaqClick(FaqUrl);
        }
    }
    public void sendOnLocationClick() {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onLocationClick();
        }
    }
    public void sendOnNotificationClick(io.kommunicate.devkit.api.conversation.Message message) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onNotificationClick(message);
        }
    }
    public void sendOnVoiceButtonClick(String action) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onVoiceButtonClick(action);
        }
    }
    public void sendOnRatingEmoticonsClick(Integer ratingValue) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onRatingEmoticonsClick(ratingValue);
        }
    }
    public void sendOnRateConversationClick() {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onRateConversationClick();
        }
    }
    private void handleState(Message message) {
        if (message != null) {
            Bundle bundle = message.getData();
            MessageEvent messageEvent = null;
            if (bundle != null) {
                messageEvent = (MessageEvent) GsonUtils.getObjectFromJson(bundle.getString(AL_EVENT), MessageEvent.class);
            }
            if (messageEvent == null) {
                return;
            }

            if(kmPluginEventListener != null) {
                switch (messageEvent.getAction()) {
                    case MessageEvent.ActionType.MESSAGE_SENT:
                        sendOnMessageSent(messageEvent.getMessage());
                        break;
                    case MessageEvent.ActionType.MESSAGE_RECEIVED:
                        sendOnMessageReceived(messageEvent.getMessage());
                        break;
                    case MessageEvent.ActionType.MESSAGE_SYNC:
                        sendOnConversationResolvedEvent(messageEvent.getMessage());
                        break;
                }
            }

            if(statusListenerMap != null && !statusListenerMap.isEmpty() && MessageEvent.ActionType.AWAY_STATUS.equals(messageEvent.getAction())) {
                for(KmStatusListener listener : statusListenerMap.values()) {
                    listener.onStatusChange(messageEvent.getUserId(), messageEvent.getStatus());
                }
            }

            if (listenerMap != null && !listenerMap.isEmpty()) {
                for (UIEventListener listener : listenerMap.values()) {
                    switch (messageEvent.getAction()) {
                        case MessageEvent.ActionType.MESSAGE_SENT:
                            listener.onMessageSent(messageEvent.getMessage());
                            break;
                        case MessageEvent.ActionType.MESSAGE_RECEIVED:
                            listener.onMessageReceived(messageEvent.getMessage());
                            break;
                        case MessageEvent.ActionType.MESSAGE_SYNC:
                            listener.onMessageSync(messageEvent.getMessage(), messageEvent.getMessageKey());
                            break;
                        case MessageEvent.ActionType.LOAD_MORE:
                            listener.onLoadMore(messageEvent.isLoadMore());
                            break;
                        case MessageEvent.ActionType.MESSAGE_DELETED:
                            listener.onMessageDeleted(messageEvent.getMessageKey(), messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.MESSAGE_DELIVERED:
                            listener.onMessageDelivered(messageEvent.getMessage(), messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.ALL_MESSAGES_DELIVERED:
                            listener.onAllMessagesDelivered(messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.ALL_MESSAGES_READ:
                            listener.onAllMessagesRead(messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.CONVERSATION_DELETED:
                            listener.onConversationDeleted(messageEvent.getUserId(), messageEvent.getGroupId(), messageEvent.getResponse());
                            break;
                        case MessageEvent.ActionType.UPDATE_TYPING_STATUS:
                            listener.onUpdateTypingStatus(messageEvent.getUserId(), messageEvent.isTyping());
                            break;
                        case MessageEvent.ActionType.UPDATE_LAST_SEEN:
                            listener.onUpdateLastSeen(messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.MQTT_DISCONNECTED:
                            listener.onMqttDisconnected();
                            break;
                        case MessageEvent.ActionType.MQTT_CONNECTED:
                            listener.onMqttConnected();
                            break;
                        case MessageEvent.ActionType.CURRENT_USER_OFFLINE:
                            listener.onUserOffline();
                            break;
                        case MessageEvent.ActionType.CURRENT_USER_ONLINE:
                            listener.onUserOnline();
                            break;
                        case MessageEvent.ActionType.CHANNEL_UPDATED:
                            listener.onChannelUpdated();
                            break;
                        case MessageEvent.ActionType.CONVERSATION_READ:
                            listener.onConversationRead(messageEvent.getUserId(), messageEvent.isGroup());
                            break;
                        case MessageEvent.ActionType.USER_DETAILS_UPDATED:
                            listener.onUserDetailUpdated(messageEvent.getUserId());
                            break;
                        case MessageEvent.ActionType.USER_ACTIVATED:
                            listener.onUserActivated(true);
                            break;
                        case MessageEvent.ActionType.USER_DEACTIVATED:
                            listener.onUserActivated(false);
                            break;
                        case MessageEvent.ActionType.MESSAGE_METADATA_UPDATED:
                            listener.onMessageMetadataUpdated(messageEvent.getMessageKey());
                            break;
                        case MessageEvent.ActionType.GROUP_MUTE:
                            listener.onGroupMute(messageEvent.getGroupId());
                    }
                }
            }
        }
    }
}
