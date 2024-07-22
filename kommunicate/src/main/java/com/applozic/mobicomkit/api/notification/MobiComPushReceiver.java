package com.applozic.mobicomkit.api.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.broadcast.AlMessageEvent;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.InstantMessageResponse;
import com.applozic.mobicomkit.feed.GcmMessageResponse;
import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class
MobiComPushReceiver {

    public static final String MTCOM_PREFIX = "APPLOZIC_";
    public static final List<String> notificationKeyList = new ArrayList<String>();
    public static final String BLOCKED_TO = "BLOCKED_TO";
    public static final String UNBLOCKED_TO = "UNBLOCKED_TO";
    private static final String TAG = "MobiComPushReceiver";
    private static Queue<String> notificationIdList = new LinkedList<String>();

    static {
        notificationKeyList.add("APPLOZIC_01"); // 0 for MESSAGE_RECEIVED //done
        notificationKeyList.add("APPLOZIC_02");// 1 for MESSAGE_SENT
        notificationKeyList.add("APPLOZIC_03");// 2 for MESSAGE_SENT_UPDATE
        notificationKeyList.add("APPLOZIC_04"); //3 for MESSAGE_DELIVERED//done
        notificationKeyList.add("APPLOZIC_05"); //4 for MESSAGE_DELETED
        notificationKeyList.add("APPLOZIC_06");// 5 for CONVERSATION_DELETED//done
        notificationKeyList.add("APPLOZIC_07"); // 6 for MESSAGE_READ
        notificationKeyList.add("APPLOZIC_08"); // 7 for MESSAGE_DELIVERED_AND_READ//done
        notificationKeyList.add("APPLOZIC_09"); // 8 for CONVERSATION_READ
        notificationKeyList.add("APPLOZIC_10"); // 9 for CONVERSATION_DELIVERED_AND_READ
        notificationKeyList.add("APPLOZIC_11");// 10 for USER_CONNECTED//done
        notificationKeyList.add("APPLOZIC_12");// 11 for USER_DISCONNECTED//done
        notificationKeyList.add("APPLOZIC_13");// 12 for GROUP_DELETED
        notificationKeyList.add("APPLOZIC_14");// 13 for GROUP_LEFT
        notificationKeyList.add("APPLOZIC_15");// 14 for group_sync
        notificationKeyList.add("APPLOZIC_16");//15 for blocked
        notificationKeyList.add("APPLOZIC_17");//16 for unblocked
        notificationKeyList.add("APPLOZIC_18");//17 ACTIVATED
        notificationKeyList.add("APPLOZIC_19");//18 DEACTIVATED
        notificationKeyList.add("APPLOZIC_20");//19 REGISTRATION
        notificationKeyList.add("APPLOZIC_21");//20 GROUP_CONVERSATION_READ
        notificationKeyList.add("APPLOZIC_22");//21 GROUP_MESSAGE_DELETED
        notificationKeyList.add("APPLOZIC_23");//22 GROUP_CONVERSATION_DELETED
        notificationKeyList.add("APPLOZIC_24");//23 APPLOZIC_TEST
        notificationKeyList.add("APPLOZIC_25");//24 USER_ONLINE_STATUS
        notificationKeyList.add("APPLOZIC_26");//25 CONTACT_SYNC
        notificationKeyList.add("APPLOZIC_27");//26 CONVERSATION_DELETED_NEW
        notificationKeyList.add("APPLOZIC_28");//27 CONVERSATION_DELIVERED_AND_READ_NEW
        notificationKeyList.add("APPLOZIC_29");//28 CONVERSATION_READ_NEW
        notificationKeyList.add("APPLOZIC_30");//29 for user detail changes
        notificationKeyList.add("APPLOZIC_33");//30 for Meta data update changes
        notificationKeyList.add("APPLOZIC_34");//31 for user delete notification
        notificationKeyList.add("APPLOZIC_37");//32 for user mute notification
        notificationKeyList.add("APPLOZIC_38");//33 for mute all notifications
        notificationKeyList.add("APPLOZIC_39");//34 for group mute notification
    }

    public static boolean isMobiComPushNotification(Intent intent) {
        Log.d(TAG, "checking for Applozic notification.");
        if (intent == null) {
            return false;
        }
        return isMobiComPushNotification(intent.getExtras());
    }

    public static boolean isMobiComPushNotification(Bundle bundle) {
        //This is to identify collapse key sent in notification..
        if (bundle == null) {
            return false;
        }
        String payLoad = bundle.getString("collapse_key");
        Log.d(TAG, "Received notification");

        if (payLoad != null && payLoad.contains(MTCOM_PREFIX) || notificationKeyList.contains(payLoad)) {
            return true;
        } else {
            for (String key : notificationKeyList) {
                payLoad = bundle.getString(key);
                if (payLoad != null) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isMobiComPushNotification(Map<String, String> data) {

        //This is to identify collapse key sent in notification..
        if (data == null || data.isEmpty()) {
            return false;
        }

        String payLoad = data.toString();
        Log.d(TAG, "Received notification");

        if (payLoad != null && payLoad.contains(MTCOM_PREFIX) || notificationKeyList.contains(payLoad)) {
            return true;
        } else {
            for (String key : notificationKeyList) {
                if (data.containsKey(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized static boolean processPushNotificationId(String id) {
        if (id != null && notificationIdList != null && notificationIdList.contains(id)) {
            if (notificationIdList.size() > 0) {
                notificationIdList.remove(id);
            }
            return true;
        }
        return false;
    }

    public synchronized static void addPushNotificationId(String notificationId) {

        try {
            if (notificationIdList != null && notificationIdList.size() < 20) {
                notificationIdList.add(notificationId);
            }
            if (notificationIdList != null && notificationIdList.size() == 20) {
                for (int i = 1; i <= 14; i++) {
                    if (notificationIdList.size() > 0) {
                        notificationIdList.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static boolean isPushMessageForLoggedUserDelete(Context context, String userDeleteNotification, MqttMessageResponse response) {
        String userIdFromResponse = response.getMessage().toString();
        return !TextUtils.isEmpty(userDeleteNotification) && !TextUtils.isEmpty(userIdFromResponse) && userIdFromResponse.equals(MobiComUserPreference.getInstance(context).getUserId());
    }

    //the MqttMessageResponse objects used in this class are not actual MQTT responses, but just their data model is used
    public static void processMessage(final Context context, Bundle bundle, Map<String, String> data) {

        try {
            String payloadForDelivered = null, userConnected = null,
                    userDisconnected = null, payloadDeliveredAndRead = null, messageKey = null,
                    messageSent = null, deleteConversationForContact = null, deleteConversationForChannel = null,
                    deleteMessage = null, conversationReadResponse = null,
                    userBlockedResponse = null, userUnBlockedResponse = null, conversationReadForContact = null, conversationReadForChannel = null, conversationReadForSingleMessage = null,
                    userDetailChanged = null, userDeleteNotification = null, messageMetadataUpdate = null, mutedUserListResponse = null, contactSync = null, muteAllNotificationResponse = null,
                    groupMuteNotificationResponse = null, userActivated = null, userDeactivated = null, notificationTest = null;
            SyncCallService syncCallService = SyncCallService.getInstance(context);

            if (bundle != null) {
                deleteConversationForContact = bundle.getString(notificationKeyList.get(5));
                deleteMessage = bundle.getString(notificationKeyList.get(4));
                payloadForDelivered = bundle.getString(notificationKeyList.get(3));
                userConnected = bundle.getString(notificationKeyList.get(10));
                userDisconnected = bundle.getString(notificationKeyList.get(11));
                payloadDeliveredAndRead = bundle.getString(notificationKeyList.get(7));
                messageSent = bundle.getString(notificationKeyList.get(1));
                messageKey = bundle.getString(notificationKeyList.get(0));
                conversationReadResponse = bundle.getString(notificationKeyList.get(9));
                userBlockedResponse = bundle.getString(notificationKeyList.get(15));
                userUnBlockedResponse = bundle.getString(notificationKeyList.get(16));
                conversationReadForContact = bundle.getString(notificationKeyList.get(8));
                conversationReadForChannel = bundle.getString(notificationKeyList.get(20));
                deleteConversationForChannel = bundle.getString(notificationKeyList.get(22));
                userDetailChanged = bundle.getString(notificationKeyList.get(29));
                userDeleteNotification = bundle.getString(notificationKeyList.get(31));
                messageMetadataUpdate = bundle.getString(notificationKeyList.get(30));
                mutedUserListResponse = bundle.getString(notificationKeyList.get(32));
                muteAllNotificationResponse = bundle.getString(notificationKeyList.get(33));
                groupMuteNotificationResponse = bundle.getString(notificationKeyList.get(34));
                userActivated = bundle.getString(notificationKeyList.get(17));
                userDeactivated = bundle.getString(notificationKeyList.get(18));
                notificationTest = data.get(notificationKeyList.get(23));
            } else if (data != null) {
                deleteConversationForContact = data.get(notificationKeyList.get(5));
                deleteMessage = data.get(notificationKeyList.get(4));
                payloadForDelivered = data.get(notificationKeyList.get(3));
                userConnected = data.get(notificationKeyList.get(10));
                userDisconnected = data.get(notificationKeyList.get(11));
                payloadDeliveredAndRead = data.get(notificationKeyList.get(7));
                messageSent = data.get(notificationKeyList.get(1));
                messageKey = data.get(notificationKeyList.get(0));
                conversationReadResponse = data.get(notificationKeyList.get(9));
                userBlockedResponse = data.get(notificationKeyList.get(15));
                userUnBlockedResponse = data.get(notificationKeyList.get(16));
                conversationReadForContact = data.get(notificationKeyList.get(8));
                conversationReadForChannel = data.get(notificationKeyList.get(20));
                deleteConversationForChannel = data.get(notificationKeyList.get(22));
                userDetailChanged = data.get(notificationKeyList.get(29));
                userDeleteNotification = data.get(notificationKeyList.get(31));
                messageMetadataUpdate = data.get(notificationKeyList.get(30));
                mutedUserListResponse = data.get(notificationKeyList.get(32));
                muteAllNotificationResponse = data.get(notificationKeyList.get(33));
                groupMuteNotificationResponse = data.get(notificationKeyList.get(34));
                userActivated = data.get(notificationKeyList.get(17));
                userDeactivated = data.get(notificationKeyList.get(18));
                notificationTest = data.get(notificationKeyList.get(23));
            }

            if (!TextUtils.isEmpty(payloadForDelivered)) {
                MqttMessageResponse messageResponseForDelivered = (MqttMessageResponse) GsonUtils.getObjectFromJson(payloadForDelivered, MqttMessageResponse.class);
                if (processPushNotificationId(messageResponseForDelivered.getId())) {
                    return;
                }
                addPushNotificationId(messageResponseForDelivered.getId());
                String splitKeyString[] = (messageResponseForDelivered.getMessage()).toString().split(",");
                String keyString = splitKeyString[0];
                // String userId = splitKeyString[1];
                syncCallService.updateDeliveryStatus(keyString);
            }

            if (!TextUtils.isEmpty(payloadDeliveredAndRead)) {
                MqttMessageResponse messageResponseForDeliveredAndRead = (MqttMessageResponse) GsonUtils.getObjectFromJson(payloadDeliveredAndRead, MqttMessageResponse.class);
                if (processPushNotificationId(messageResponseForDeliveredAndRead.getId())) {
                    return;
                }
                addPushNotificationId(messageResponseForDeliveredAndRead.getId());
                String splitKeyString[] = (messageResponseForDeliveredAndRead.getMessage()).toString().split(",");
                String keyString = splitKeyString[0];
                // String userId = splitKeyString[1];
                syncCallService.updateReadStatus(keyString);
            }

            if (!TextUtils.isEmpty(deleteConversationForContact)) {
                MqttMessageResponse deleteConversationResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(deleteConversationForContact, MqttMessageResponse.class);
                if (processPushNotificationId(deleteConversationResponse.getId())) {
                    return;
                }
                addPushNotificationId(deleteConversationResponse.getId());
                MobiComConversationService conversationService = new MobiComConversationService(context);
                conversationService.deleteConversationFromDevice(deleteConversationResponse.getMessage().toString());
                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), deleteConversationResponse.getMessage().toString(), 0, "success");
            }

            if (!TextUtils.isEmpty(deleteConversationForChannel)) {
                InstantMessageResponse instantMessageResponse = (InstantMessageResponse) GsonUtils.getObjectFromJson(deleteConversationForChannel, InstantMessageResponse.class);
                if (processPushNotificationId(instantMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(instantMessageResponse.getId());
                syncCallService.deleteChannelConversationThread(instantMessageResponse.getMessage());
                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, Integer.valueOf(instantMessageResponse.getMessage()), "success");
            }

            if (!TextUtils.isEmpty(userConnected)) {
                MqttMessageResponse userConnectedResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(userConnected, MqttMessageResponse.class);
                if (processPushNotificationId(userConnectedResponse.getId())) {
                    return;
                }
                addPushNotificationId(userConnectedResponse.getId());
                syncCallService.updateConnectedStatus(userConnectedResponse.getMessage().toString(), new Date(), true);
            }

            if (!TextUtils.isEmpty(userDisconnected)) {
                MqttMessageResponse userDisconnectedResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(userDisconnected, MqttMessageResponse.class);
                if (processPushNotificationId(userDisconnectedResponse.getId())) {
                    return;
                }
                addPushNotificationId(userDisconnectedResponse.getId());
                String[] parts = userDisconnectedResponse.getMessage().toString().split(",");
                String userId = parts[0];
                Date lastSeenAt = new Date();
                if (parts.length >= 2 && !parts[1].equals("null")) {
                    lastSeenAt = new Date(Long.valueOf(parts[1]));
                }
                syncCallService.updateConnectedStatus(userId, lastSeenAt, false);
            }

            if (!TextUtils.isEmpty(deleteMessage)) {
                MqttMessageResponse deleteSingleMessageResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(deleteMessage, MqttMessageResponse.class);
                if (processPushNotificationId(deleteSingleMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(deleteSingleMessageResponse.getId());
                String deleteMessageKeyAndUserId = deleteSingleMessageResponse.getMessage().toString();
                //String contactNumbers = deleteMessageKeyAndUserId.split(",").length > 1 ? deleteMessageKeyAndUserId.split(",")[1] : null;
                syncCallService.deleteMessage(deleteMessageKeyAndUserId.split(",")[0], null);
            }

            if (!TextUtils.isEmpty(messageSent)) {
                GcmMessageResponse syncSentMessageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageSent, GcmMessageResponse.class);
                if (processPushNotificationId(syncSentMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(syncSentMessageResponse.getId());
                syncCallService.syncMessages(null);
            }

            GcmMessageResponse syncMessageResponse = null;
            //APPLOZIC_01 -> Handle message we receive from FCM without calling Sync Message API.
            // Sync Message API is to be called when notification is clicked or when app is opened.
            if (!TextUtils.isEmpty(messageKey)) {
                syncMessageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageKey, GcmMessageResponse.class);
                if (processPushNotificationId(syncMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(syncMessageResponse.getId());
                final Message messageObj = syncMessageResponse.getMessage();

                if (!TextUtils.isEmpty(messageObj.getKeyString())) {
                    MobiComMessageService mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);
                    mobiComMessageService.processFirebaseMessages(messageObj);
                    Thread bgThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MessageClientService messageClientService = new MessageClientService(context);
                            messageClientService.updateDeliveryStatus(messageObj.getKeyString(), MobiComUserPreference.getInstance(context).getUserId(), null);
                        }
                    });
                    bgThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    bgThread.start();
                    if (messageObj.getGroupId() != null && messageObj.isGroupDeleteAction()) {
                        syncCallService.deleteChannelConversationThread(messageObj.getGroupId());
                        BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), null, messageObj.getGroupId(), "success");
                    }
                } else {
                    syncCallService.syncMessages(null);
                }
            }

            if (!TextUtils.isEmpty(conversationReadResponse)) {
                MqttMessageResponse updateDeliveryStatusForContactResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(conversationReadResponse, MqttMessageResponse.class);
                if (notificationKeyList.get(9).equals(updateDeliveryStatusForContactResponse.getType())) {
                    if (processPushNotificationId(updateDeliveryStatusForContactResponse.getId())) {
                        return;
                    }
                    addPushNotificationId(updateDeliveryStatusForContactResponse.getId());
                    syncCallService.updateDeliveryStatusForContact(updateDeliveryStatusForContactResponse.getMessage().toString(), true);
                }
            }

            if (!TextUtils.isEmpty(userBlockedResponse)) {
                MqttMessageResponse syncUserBlock = (MqttMessageResponse) GsonUtils.getObjectFromJson(userBlockedResponse, MqttMessageResponse.class);
                if (processPushNotificationId(syncUserBlock.getId())) {
                    return;
                }
                addPushNotificationId(syncUserBlock.getId());
                SyncCallService.getInstance(context).syncBlockUsers();
            }


            if (!TextUtils.isEmpty(userUnBlockedResponse)) {
                MqttMessageResponse syncUserUnBlock = (MqttMessageResponse) GsonUtils.getObjectFromJson(userUnBlockedResponse, MqttMessageResponse.class);
                if (processPushNotificationId(syncUserUnBlock.getId())) {
                    return;
                }
                addPushNotificationId(syncUserUnBlock.getId());
                SyncCallService.getInstance(context).syncBlockUsers();
            }

            if (!TextUtils.isEmpty(conversationReadForContact)) {
                MqttMessageResponse conversationReadForContactResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(conversationReadForContact, MqttMessageResponse.class);
                if (processPushNotificationId(conversationReadForContactResponse.getId())) {
                    return;
                }
                addPushNotificationId(conversationReadForContactResponse.getId());
                syncCallService.updateConversationReadStatus(conversationReadForContactResponse.getMessage().toString(), false);
            }

            if (!TextUtils.isEmpty(conversationReadForChannel)) {
                InstantMessageResponse conversationReadForChannelResponse = (InstantMessageResponse) GsonUtils.getObjectFromJson(conversationReadForChannel, InstantMessageResponse.class);
                if (processPushNotificationId(conversationReadForChannelResponse.getId())) {
                    return;
                }
                addPushNotificationId(conversationReadForChannelResponse.getId());
                syncCallService.updateConversationReadStatus(conversationReadForChannelResponse.getMessage(), true);
            }

            if (!TextUtils.isEmpty(userDetailChanged) || !TextUtils.isEmpty(userDeleteNotification)) {
                MqttMessageResponse response = null;
                if (!TextUtils.isEmpty(userDetailChanged)) {
                    response = (MqttMessageResponse) GsonUtils.getObjectFromJson(userDetailChanged, MqttMessageResponse.class);
                } else if (!TextUtils.isEmpty(userDeleteNotification)) {
                    response = (MqttMessageResponse) GsonUtils.getObjectFromJson(userDeleteNotification, MqttMessageResponse.class);
                }
                if (processPushNotificationId(response.getId())) {
                    return;
                }
                addPushNotificationId(response.getId());
                String userId = response.getMessage().toString();
                syncCallService.syncUserDetail(userId);

                if (isPushMessageForLoggedUserDelete(context, userDeleteNotification, response)) {
                    syncCallService.processLoggedUserDelete();
                }
            }

            if (!TextUtils.isEmpty(messageMetadataUpdate)) {
                String keyString = null;
                String id = null;
                try {
                    GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageMetadataUpdate, GcmMessageResponse.class);
                    keyString = messageResponse.getMessage().getKeyString();
                    Message messageObject = messageResponse.getMessage();
                    id = messageResponse.getId();
                    if (processPushNotificationId(id)) {
                        return;
                    }
                    addPushNotificationId(id);
                    syncCallService.syncMessageMetadataUpdate(keyString, true, messageObject);

                } catch (Exception e) {
                    Utils.printLog(context, TAG, e.getMessage());
                }
            }

            if (!TextUtils.isEmpty(mutedUserListResponse)) {
                try {
                    InstantMessageResponse response = (InstantMessageResponse) GsonUtils.getObjectFromJson(mutedUserListResponse, InstantMessageResponse.class);
                    if (processPushNotificationId(response.getId())) {
                        return;
                    }

                    addPushNotificationId(response.getId());

                    if (response.getMessage() != null) {
                        String muteFlag = String.valueOf(response.getMessage().charAt(response.getMessage().length() - 1));
                        if ("1".equals(muteFlag)) {
                            syncCallService.syncMutedUserList(true, null);
                        } else if ("0".equals(muteFlag)) {
                            String userId = response.getMessage().substring(0, response.getMessage().length() - 2);
                            syncCallService.syncMutedUserList(true, userId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(muteAllNotificationResponse)) {
                try {
                    GcmMessageResponse messageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(muteAllNotificationResponse, GcmMessageResponse.class);

                    if (processPushNotificationId(messageResponse.getId())) {
                        return;
                    }

                    addPushNotificationId(messageResponse.getId());

                    if (messageResponse.getMessage() != null && messageResponse.getMessage().getMessage() != null) {
                        long notificationAfterTime = Long.parseLong(messageResponse.getMessage().getMessage());
                        ALSpecificSettings.getInstance(context).setNotificationAfterTime(notificationAfterTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(groupMuteNotificationResponse)) {
                try {
                    InstantMessageResponse response = (InstantMessageResponse) GsonUtils.getObjectFromJson(groupMuteNotificationResponse, InstantMessageResponse.class);

                    if (processPushNotificationId(response.getId())) {
                        return;
                    }

                    addPushNotificationId(response.getId());
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

            if (!TextUtils.isEmpty(userActivated)) {
                BroadcastService.sendUserActivatedBroadcast(context, AlMessageEvent.ActionType.USER_ACTIVATED);
            }

            if (!TextUtils.isEmpty(userDeactivated)) {
                BroadcastService.sendUserActivatedBroadcast(context, AlMessageEvent.ActionType.USER_DEACTIVATED);
            }
            if(!TextUtils.isEmpty(notificationTest)) {

                int notificationId = Utils.getLauncherIcon(context.getApplicationContext());
                InstantMessageResponse messageResponse = (InstantMessageResponse) GsonUtils.getObjectFromJson(notificationTest, InstantMessageResponse.class);
                if (messageResponse != null && !messageResponse.isSendAlert()) {
                    return;
                }
                NotificationService notificationService = new NotificationService(notificationId, context, 0, 0, 0);
                notificationService.sendTestNotification();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void processMessageAsync(final Context context, final Bundle bundle) {
        try {
            if (MobiComUserPreference.getInstance(context).isLoggedIn()) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processMessage(context, bundle);
                    }
                });
                t.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                t.start();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void processMessageAsync(final Context context, final Intent intent) {
        processMessageAsync(context, intent.getExtras());
    }

    public static void processMessageAsync(final Context context, final Map<String, String> data) {
        try {
            if (MobiComUserPreference.getInstance(context).isLoggedIn()) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processMessage(context, data);
                    }
                });
                t.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                t.start();
            }
        } catch (Throwable w) {
            w.printStackTrace();
        }
    }

    public static void processMessage(Context context, Map<String, String> data) {
        processMessage(context, null, data);
    }

    public static void processMessage(Context context, Bundle bundle) {
        processMessage(context, bundle, null);
    }

}
