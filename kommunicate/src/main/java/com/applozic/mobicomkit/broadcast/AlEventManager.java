package com.applozic.mobicomkit.broadcast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicomkit.listners.AlMqttListener;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;

import io.kommunicate.callbacks.KmPluginEventListener;

/**
 * Handles real time update events that are registered to listen for.
 * multiple listeners can be registered by unique id
 */
public class AlEventManager {
    public static final String AL_EVENT = "AL_EVENT";
    private static AlEventManager eventManager;
    private Map<String, ApplozicUIListener> listenerMap;
    private Map<String, AlMqttListener> mqttListenerMap;
    private KmPluginEventListener kmPluginEventListener;
    private Handler uiHandler;

    public static AlEventManager getInstance() {
        if (eventManager == null) {
            eventManager = new AlEventManager();
        }
        return eventManager;
    }

    public void registerUIListener(String id, ApplozicUIListener listener) {
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

    public void registerMqttListener(String id, AlMqttListener mqttListener) {
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

    void postEventData(AlMessageEvent messageEvent) {
        if (uiHandler != null) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(AL_EVENT, GsonUtils.getJsonFromObject(messageEvent, AlMessageEvent.class));
            message.setData(bundle);
            uiHandler.sendMessage(message);
        }
    }

    public void postMqttEventData(MqttMessageResponse messageResponse) {
        if (mqttListenerMap != null && !mqttListenerMap.isEmpty()) {
            for (AlMqttListener alMqttListener : mqttListenerMap.values()) {
                alMqttListener.onMqttMessageReceived(messageResponse);
            }
        }
    }

    public void registerPluginEventListener(KmPluginEventListener kmPluginEventListener) {
        this.kmPluginEventListener = kmPluginEventListener;
        initHandler();
    }

    private void initHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    handleState(msg);
                    return false;
                }
            });
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

    public void sendOnConversationResolvedEvent(com.applozic.mobicomkit.api.conversation.Message message) {
        if (kmPluginEventListener != null && message.isTypeResolved() && message.getGroupId() != null) {
            kmPluginEventListener.onConversationResolved(message.getGroupId());
        }
    }

    public void sendOnConversationRestartedEvent(com.applozic.mobicomkit.api.conversation.Message message) {
        if (kmPluginEventListener != null && message.isTypeOpen() && message.getGroupId() != null) {
            kmPluginEventListener.onConversationRestarted(message.getGroupId());
        }
    }

    public void sendOnRichMessageButtonClickEvent(Integer conversationId, String actionType, Object action) {
        if (kmPluginEventListener != null) {
            kmPluginEventListener.onRichMessageButtonClick(conversationId, actionType, action);
        }
    }

    private void handleState(Message message) {
        if (message != null) {
            Bundle bundle = message.getData();
            AlMessageEvent messageEvent = null;
            if (bundle != null) {
                messageEvent = (AlMessageEvent) GsonUtils.getObjectFromJson(bundle.getString(AL_EVENT), AlMessageEvent.class);
            }
            if (messageEvent == null) {
                return;
            }
            if (kmPluginEventListener != null && AlMessageEvent.ActionType.MESSAGE_SYNC.equals(messageEvent.getAction())) {
                sendOnConversationResolvedEvent(messageEvent.getMessage());
                sendOnConversationRestartedEvent(messageEvent.getMessage());
            }

            if (listenerMap != null && !listenerMap.isEmpty()) {
                for (ApplozicUIListener listener : listenerMap.values()) {
                    switch (messageEvent.getAction()) {
                        case AlMessageEvent.ActionType.MESSAGE_SENT:
                            listener.onMessageSent(messageEvent.getMessage());
                            break;
                        case AlMessageEvent.ActionType.MESSAGE_RECEIVED:
                            listener.onMessageReceived(messageEvent.getMessage());
                            break;
                        case AlMessageEvent.ActionType.MESSAGE_SYNC:
                            listener.onMessageSync(messageEvent.getMessage(), messageEvent.getMessageKey());
                            break;
                        case AlMessageEvent.ActionType.LOAD_MORE:
                            listener.onLoadMore(messageEvent.isLoadMore());
                            break;
                        case AlMessageEvent.ActionType.MESSAGE_DELETED:
                            listener.onMessageDeleted(messageEvent.getMessageKey(), messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.MESSAGE_DELIVERED:
                            listener.onMessageDelivered(messageEvent.getMessage(), messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.ALL_MESSAGES_DELIVERED:
                            listener.onAllMessagesDelivered(messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.ALL_MESSAGES_READ:
                            listener.onAllMessagesRead(messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.CONVERSATION_DELETED:
                            listener.onConversationDeleted(messageEvent.getUserId(), messageEvent.getGroupId(), messageEvent.getResponse());
                            break;
                        case AlMessageEvent.ActionType.UPDATE_TYPING_STATUS:
                            listener.onUpdateTypingStatus(messageEvent.getUserId(), messageEvent.isTyping());
                            break;
                        case AlMessageEvent.ActionType.UPDATE_LAST_SEEN:
                            listener.onUpdateLastSeen(messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.MQTT_DISCONNECTED:
                            listener.onMqttDisconnected();
                            break;
                        case AlMessageEvent.ActionType.MQTT_CONNECTED:
                            listener.onMqttConnected();
                            break;
                        case AlMessageEvent.ActionType.CURRENT_USER_OFFLINE:
                            listener.onUserOffline();
                            break;
                        case AlMessageEvent.ActionType.CURRENT_USER_ONLINE:
                            listener.onUserOnline();
                            break;
                        case AlMessageEvent.ActionType.CHANNEL_UPDATED:
                            listener.onChannelUpdated();
                            break;
                        case AlMessageEvent.ActionType.CONVERSATION_READ:
                            listener.onConversationRead(messageEvent.getUserId(), messageEvent.isGroup());
                            break;
                        case AlMessageEvent.ActionType.USER_DETAILS_UPDATED:
                            listener.onUserDetailUpdated(messageEvent.getUserId());
                            break;
                        case AlMessageEvent.ActionType.USER_ACTIVATED:
                            listener.onUserActivated(true);
                            break;
                        case AlMessageEvent.ActionType.USER_DEACTIVATED:
                            listener.onUserActivated(false);
                            break;
                        case AlMessageEvent.ActionType.MESSAGE_METADATA_UPDATED:
                            listener.onMessageMetadataUpdated(messageEvent.getMessageKey());
                            break;
                        case AlMessageEvent.ActionType.GROUP_MUTE:
                            listener.onGroupMute(messageEvent.getGroupId());
                    }
                }
            }
        }
    }
}
