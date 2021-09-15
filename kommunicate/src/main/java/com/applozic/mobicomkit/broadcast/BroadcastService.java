package com.applozic.mobicomkit.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.Map;

/**
 * For sending various android Broadcasts different parts of the app.
 *
 * <p>NOTE: If we want to send the broadcast to app make sure to not to add the Category intent.addCategory(Intent.CATEGORY_DEFAULT);
 * P.S: When creating a new broadcast do not forget to add it's INTENT_ACTIONS to {@link BroadcastService#getIntentFilter()}.</p>
 *
 * Created by devashish on 24/1/15.
 */
public class BroadcastService {

    private static final String TAG = "BroadcastService";
    private static final String MOBICOMKIT_ALL = "MOBICOMKIT_ALL";

    public static String currentUserId = null;
    public static Integer parentGroupKey = null;
    public static Integer currentConversationId = null;
    public static String currentInfoId = null;
    public static boolean videoCallAcitivityOpend = false;
    public static boolean callRinging = false;
    public static int lastIndexForChats = 0;
    private static boolean contextBasedChatEnabled = false;
    public static String currentUserProfileUserId = null;

    public static void selectMobiComKitAll() {
        currentUserId = MOBICOMKIT_ALL;
    }

    public static boolean isQuick() {
        return currentUserId != null && currentUserId.equals(MOBICOMKIT_ALL);
    }

    public static boolean isChannelInfo() {
        return currentInfoId != null;
    }

    public static boolean isIndividual() {
        return currentUserId != null && !isQuick();
    }

    public static synchronized boolean isContextBasedChatEnabled() {
        return contextBasedChatEnabled;
    }

    public static synchronized boolean setContextBasedChat(boolean contextBasedChat) {
        return contextBasedChatEnabled = contextBasedChat;
    }

    public static void sendLoadMoreBroadcast(Context context, boolean loadMore) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.LOAD_MORE).setLoadMore(loadMore));

        Utils.printLog(context, TAG, "Sending " + INTENT_ACTIONS.LOAD_MORE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("loadMore", loadMore);
        sendBroadcast(context, intent);
    }

    public static void sendDeliveryReportForContactBroadcast(Context context, String action, String contactId) {
        if (INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.ALL_MESSAGES_READ).setUserId(contactId));
        } else if (INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.ALL_MESSAGES_DELIVERED).setUserId(contactId));
        }

        Utils.printLog(context, TAG, "Sending message delivery report of contact broadcast for " + action + ", " + contactId);
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.CONTACT_ID, contactId);
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageUpdateBroadcast(Context context, String action, Message message) {
        if (!message.isSentToMany() && !message.isTypeOutbox()) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_RECEIVED).setMessage(message));
        }
        if (INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_SENT).setMessage(message));
        } else if (INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_SYNC).setMessage(message));
        } else if (INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) || INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_DELIVERED).setMessage(message).setUserId(message.getContactIds()));
        }

        Utils.printLog(context, TAG, "Sending message update broadcast for " + action + ", " + message.getKeyString());
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageDeleteBroadcast(Context context, String action, String keyString, String contactNumbers) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_DELETED).setMessageKey(keyString).setUserId(contactNumbers));

        Utils.printLog(context, TAG, "Sending message delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("keyString", keyString);
        intentDelete.putExtra("contactNumbers", contactNumbers);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentDelete);
    }

    public static void sendConversationDeleteBroadcast(Context context, String action, String contactNumber, Integer channelKey, String response) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.CONVERSATION_DELETED).setUserId(contactNumber).setGroupId(channelKey).setResponse(response));

        Utils.printLog(context, TAG, "Sending conversation delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("channelKey", channelKey);
        intentDelete.putExtra("contactNumber", contactNumber);
        intentDelete.putExtra("response", response);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentDelete);
    }

    public static void sendUserActivatedBroadcast(Context context, String action) {
        MobiComUserPreference.getInstance(context).setUserDeactivated(AlMessageEvent.ActionType.USER_DEACTIVATED.equals(action));
        postEventData(context, new AlMessageEvent().setAction(action));
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(context, intent);
    }

    public static void sendNotificationBroadcast(Context context, Message message, int index) {
        if (message != null) {
            if (ALSpecificSettings.getInstance(context).isAllNotificationMuted() || message.getMetadata() != null && message.getMetadata().containsKey("NO_ALERT") && "true".equals(message.getMetadata().get("NO_ALERT"))) {
                return;
            }

            int notificationId = Utils.getLauncherIcon(context.getApplicationContext());
            final NotificationService notificationService =
                    new NotificationService(notificationId, context, 0, 0, 0);

            if (MobiComUserPreference.getInstance(context).isLoggedIn()) {
                Channel channel = ChannelService.getInstance(context).getChannelInfo(message.getGroupId());
                Contact contact = new AppContactService(context).getContactById(message.getContactIds());

                //Do not send BOT and Agent message notification to agents, roletype 3 = user
                //TODO: Notification should be handled from server side. Change this code when server side changes is done
                if(MobiComUserPreference.getInstance(context).getUserRoleType() != 3) {
                        if(contact.getRoleType() != 3) {
                            return;
                        }
                        if(!channel.getConversationAssignee().equals(MobiComUserPreference.getInstance(context).getUserId())) {
                             return;
                        }
                }

                if (message.getConversationId() != null) {
                    ConversationService.getInstance(context).getConversation(message.getConversationId());
                }

                if (ApplozicClient.getInstance(context).isNotificationStacking()) {
                    notificationService.notifyUser(contact, channel, message, index);
                } else {
                    notificationService.notifyUserForNormalMessage(contact, channel, message, index);
                }
            }
        }
    }

    public static void sendUpdateLastSeenAtTimeBroadcast(Context context, String action, String contactId) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.UPDATE_LAST_SEEN).setUserId(contactId));

        Utils.printLog(context, TAG, "Sending lastSeenAt broadcast....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateTypingBroadcast(Context context, String action, String applicationId, String userId, String isTyping) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.UPDATE_TYPING_STATUS).setUserId(userId).setTyping(isTyping));

        Utils.printLog(context, TAG, "Sending typing Broadcast.......");
        Intent intentTyping = new Intent();
        intentTyping.setAction(action);
        intentTyping.putExtra("applicationId", applicationId);
        intentTyping.putExtra("userId", userId);
        intentTyping.putExtra("isTyping", isTyping);
        intentTyping.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentTyping);
    }


    public static void sendUpdate(Context context, boolean isMetadataUpdate, final String action) {
        if (INTENT_ACTIONS.MQTT_CONNECTED.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MQTT_CONNECTED));
        } else if (INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.MQTT_DISCONNECTED));
        } else if (INTENT_ACTIONS.USER_ONLINE.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.CURRENT_USER_ONLINE));
        } else if (INTENT_ACTIONS.USER_OFFLINE.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.CURRENT_USER_OFFLINE));
        } else if (INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.CHANNEL_UPDATED));
        }

        Utils.printLog(context, TAG, action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("isMetadataUpdate", isMetadataUpdate);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendUpdate(Context context, String action) {
        sendUpdate(context, false, action);
    }

    public static void updateMessageMetadata(Context context, String messageKey, String action, String userId, Integer groupId, Boolean isOpenGroup, Map<String, String> metadata) {

        try {
            AlMessageEvent messageEvent = new AlMessageEvent().setAction(AlMessageEvent.ActionType.MESSAGE_METADATA_UPDATED).setMessageKey(messageKey);
            Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra("keyString", messageKey);

            if (groupId != null) {
                messageEvent.setGroupId(groupId);
                intent.putExtra("groupId", groupId);
                intent.putExtra("openGroup", isOpenGroup);
            } else if (!TextUtils.isEmpty(userId)) {
                messageEvent.setUserId(userId);
                intent.putExtra("userId", userId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                intent.putExtra("messageMetadata", GsonUtils.getJsonFromObject(metadata, Map.class));
            }
            messageEvent.setGroup(groupId != null);
            postEventData(context, messageEvent);

            Utils.printLog(context, TAG, "Sending Message Metadata Update Broadcast for message key : " + messageKey);
            sendBroadcast(context, intent);
        } catch (Exception e) {
            Utils.printLog(context, TAG, e.getMessage());
        }
    }

    public static void sendConversationReadBroadcast(Context context, String action, String currentId, boolean isGroup) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.CONVERSATION_READ).setUserId(currentId).setGroup(isGroup));

        Utils.printLog(context, TAG, "Sending  Broadcast for conversation read ......");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("currentId", currentId);
        intent.putExtra("isGroup", isGroup);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendMuteUserBroadcast(Context context, String action, boolean mute, String userId) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.ON_USER_MUTE).setUserId(userId).setLoadMore(mute));

        Utils.printLog(context, TAG, "Sending Mute user Broadcast for user : " + userId + ", mute : " + mute);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("mute", mute);
        intent.putExtra("userId", userId);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateUserDetailBroadcast(Context context, String action, String contactId) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.USER_DETAILS_UPDATED).setUserId(contactId));

        Utils.printLog(context, TAG, "Sending profileImage update....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateGroupInfoBroadcast(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateGroupMuteForGroupId(Context context, Integer groupId, String action) {
        postEventData(context, new AlMessageEvent().setAction(AlMessageEvent.ActionType.GROUP_MUTE).setGroup(true).setGroupId(groupId));

        Utils.printLog(context, TAG, "Sending group mute update for groupId " + groupId);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("groupId", groupId);
        sendBroadcast(context, intent);
    }

    public static void sendContactProfileClickBroadcast(Context context, String contactUserId) {
        sendContactProfileClickBroadcast(context, INTENT_ACTIONS.CONTACT_PROFILE_CLICK.toString(), contactUserId);
    }

    public static void sendContactProfileClickBroadcast(Context context, String action, String contactUserId) {
        Utils.printLog(context, TAG, "Sending contact profile click broadcast for: " + contactUserId);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactUserId", contactUserId);
        sendBroadcast(context, intent);
    }

    public static void sendLoggedUserDeletedBroadcast(Context context) {
        sendLoggedUserDeletedBroadcast(context, INTENT_ACTIONS.LOGGED_USER_DELETE.toString());
    }

    public static void sendLoggedUserDeletedBroadcast(Context context, String action) {
        Utils.printLog(context, TAG, "Sending broadcast for logged user deleted.");
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(context, intent);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString());
        intentFilter.addAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString());
        intentFilter.addAction(INTENT_ACTIONS.SYNC_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_CONVERSATION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString());
        intentFilter.addAction(INTENT_ACTIONS.INSTRUCTION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString());
        intentFilter.addAction(INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString());
        intentFilter.addAction(INTENT_ACTIONS.CHANNEL_SYNC.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
        intentFilter.addAction(INTENT_ACTIONS.CONVERSATION_READ.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_USER_DETAIL.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString());
        intentFilter.addAction(INTENT_ACTIONS.MUTE_USER_CHAT.toString());
        intentFilter.addAction(INTENT_ACTIONS.MQTT_CONNECTED.toString());
        intentFilter.addAction(INTENT_ACTIONS.USER_ONLINE.toString());
        intentFilter.addAction(INTENT_ACTIONS.USER_OFFLINE.toString());
        intentFilter.addAction(INTENT_ACTIONS.GROUP_MUTE.toString());
        intentFilter.addAction(INTENT_ACTIONS.CONTACT_PROFILE_CLICK.toString());
        intentFilter.addAction(INTENT_ACTIONS.LOGGED_USER_DELETE.toString());
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        return intentFilter;
    }

    public static void sendBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void postEventData(Context context, AlMessageEvent messageEvent) {
        AlEventManager.getInstance().postEventData(messageEvent);
    }

    public enum INTENT_ACTIONS {
        LOAD_MORE, FIRST_TIME_SYNC_COMPLETE, MESSAGE_SYNC_ACK_FROM_SERVER,
        SYNC_MESSAGE, DELETE_MESSAGE, DELETE_CONVERSATION, MESSAGE_DELIVERY, MESSAGE_DELIVERY_FOR_CONTACT, INSTRUCTION, UPDATE_GROUP_INFO,
        UPLOAD_ATTACHMENT_FAILED, MESSAGE_ATTACHMENT_DOWNLOAD_DONE, MESSAGE_ATTACHMENT_DOWNLOAD_FAILD,
        UPDATE_LAST_SEEN_AT_TIME, UPDATE_TYPING_STATUS, MESSAGE_READ_AND_DELIVERED, MESSAGE_READ_AND_DELIVERED_FOR_CONTECT, CHANNEL_SYNC,
        CONTACT_VERIFIED, NOTIFY_USER, MQTT_DISCONNECTED, UPDATE_CHANNEL_NAME, UPDATE_TITLE_SUBTITLE, CONVERSATION_READ, UPDATE_USER_DETAIL,
        MESSAGE_METADATA_UPDATE, MUTE_USER_CHAT, MQTT_CONNECTED, USER_ONLINE, USER_OFFLINE, GROUP_MUTE, CONTACT_PROFILE_CLICK, LOGGED_USER_DELETE
    }
}
