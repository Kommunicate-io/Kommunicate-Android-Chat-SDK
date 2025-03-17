package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;
import io.kommunicate.utils.KmConstants;

import android.text.TextUtils;
import io.kommunicate.devkit.ApplozicClient;
import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.contact.Contact;

/**
 * Created by devashish on 4/2/15.
 */
public class MobiComKitBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MTBroadcastReceiver";
    private static final String HIDE_ASSIGNEE = "hideAssignee";
    private static final String PREFILLED = "preFilled";
    private static final String USERID = "userId";
    private static final String CONTACT_NUMBERS = "contactNumbers";
    private static final String CONTACT_ID = "contactId";
    private static final String CURR_ID = "currentId";
    private static final String IS_GROUP = "isGroup";
    private static final String RES_ID = "resId";
    private static final String IS_TYPING = "isTyping";
    private static final String LOAD_MORE = "loadMore";
    private ConversationUIService conversationUIService;
    private BaseContactService baseContactService;
    private boolean hideActionMessages;

    public MobiComKitBroadcastReceiver(FragmentActivity fragmentActivity) {
        this.conversationUIService = new ConversationUIService(fragmentActivity);
        this.baseContactService = new AppContactService(fragmentActivity);
        this.hideActionMessages = ApplozicClient.getInstance(fragmentActivity).isActionMessagesHidden();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = null;
        String keyString = intent.getStringExtra("keyString");
        if(!TextUtils.isEmpty(keyString)) {
            message = new MessageDatabaseService(context).getMessage(keyString);
        }
        if(message == null) {
            String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
            if (!TextUtils.isEmpty(messageJson)) {
                message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            }
        }
        if(message != null) {
            if ((hideActionMessages && message.isActionMessage()) || (message.isActionMessage() && TextUtils.isEmpty(message.getMessage()))) {
                message.setHidden(true);
            }
        }
        Utils.printLog(context, TAG, "Received broadcast, action: " + action + ", message: " + message);

        if (message != null && !message.isSentToMany()) {
            conversationUIService.addMessage(message);
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                conversationUIService.addMessage(message);
            }
        }

        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString().equals(action)) {
            InstructionUtil.showInstruction(context, intent.getIntExtra(RES_ID, -1), intent.getBooleanExtra("actionable", false), R.color.instruction_color);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString().equals(action)) {
            conversationUIService.updateChannelName();
        } else if (BroadcastService.INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString().equals(action)) {
            conversationUIService.downloadConversations(true);
        } else if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            conversationUIService.setLoadMore(intent.getBooleanExtra(LOAD_MORE, true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            conversationUIService.updateMessageKeyString(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            conversationUIService.syncMessages(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra(CONTACT_NUMBERS);
            conversationUIService.deleteMessage(keyString, userId, message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            conversationUIService.updateDeliveryStatus(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            conversationUIService.updateDeliveryStatusForContact(intent.getStringExtra(CONTACT_ID));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            conversationUIService.updateReadStatusForContact(intent.getStringExtra(CONTACT_ID));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra(CONTACT_NUMBERS);
            Integer channelKey = intent.getIntExtra("channelKey", 0);
            String response = intent.getStringExtra("response");
            Contact contact = null;
            if (contactNumber != null) {
                contact = baseContactService.getContactById(contactNumber);
            }
            conversationUIService.deleteConversation(contact, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString().equals(action) && message != null) {
            conversationUIService.updateUploadFailedStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString().equals(action) && message != null) {
            conversationUIService.updateDownloadStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString().equals(action) && message != null) {
            conversationUIService.updateDownloadFailed(message);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra(USERID);
            String isTyping = intent.getStringExtra(IS_TYPING);
            conversationUIService.updateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            conversationUIService.updateLastSeenStatus(intent.getStringExtra(CONTACT_ID));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            conversationUIService.reconnectMQTT();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            conversationUIService.updateChannelSync();
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString().equals(action)) {
            conversationUIService.updateTitleAndSubtitle();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra(CURR_ID);
            boolean isGroup = intent.getBooleanExtra(IS_GROUP, false);
            conversationUIService.updateConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            conversationUIService.updateUserInfo(intent.getStringExtra(CONTACT_ID));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            conversationUIService.updateMessageMetadata(keyString);
        } else if (BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString().equals(action)) {
            conversationUIService.muteUserChat(intent.getBooleanExtra("mute", false), intent.getStringExtra(USERID));
        } else if(BroadcastService.INTENT_ACTIONS.AGENT_STATUS.toString().equals(action)) {
            conversationUIService.updateAgentStatus(intent.getStringExtra(USERID), intent.getIntExtra("status", KmConstants.STATUS_ONLINE));
        } else if (BroadcastService.INTENT_ACTIONS.ACTION_POPULATE_CHAT_TEXT.toString().equals(action)) {
            String preFilledText = intent.getStringExtra(PREFILLED);
            conversationUIService.setAutoText(preFilledText);
        } else if (BroadcastService.INTENT_ACTIONS.HIDE_ASSIGNEE_STATUS.toString().equals(action)){
            Boolean hide = intent.getBooleanExtra(HIDE_ASSIGNEE,false);
            conversationUIService.hideAssigneeStatus(hide);
        }
    }
}
