package io.kommunicate.devkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.RemoteInput;
import android.text.TextUtils;

import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.MessageIntentService;
import io.kommunicate.devkit.api.notification.WearableNotificationWithVoice;

import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;

/**
 * Created by adarsh on 4/12/14.
 * This class should handle all notification types coming from server or some other client.
 * Depending upon actionType it should either do some bg service,task or open some view like activity.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static String LAUNCH_APP = "applozic.LAUNCH_APP";
    public static String TAG = "NotificationBroadcastReceiver";
    private static final String android_path = "vnd.android-dir/mms-sms";
    private static final String sms_body = "sms_body";
    private static final String ACTIVITY_OPEN = "activity.open.on.notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        String actionName = intent.getAction();

        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        String activityToOpen = Utils.getMetaDataValueForReceiver(context, NotificationBroadcastReceiver.class.getName(), ACTIVITY_OPEN);
        Intent newIntent;
        if (actionName.equals(LAUNCH_APP)) {
            String messageText = getMessageText(intent) == null ? null : getMessageText(intent).toString();
            if (!TextUtils.isEmpty(messageText)) {
                Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
                Message replyMessage = new Message();
                replyMessage.setTo(message.getTo());
                replyMessage.setStoreOnDevice(Boolean.TRUE);
                replyMessage.setSendToDevice(Boolean.FALSE);
                replyMessage.setType(Message.MessageType.MT_OUTBOX.getValue());
                replyMessage.setMessage(messageText);
                replyMessage.setDeviceKeyString(MobiComUserPreference.getInstance(context).getDeviceKeyString());
                replyMessage.setSource(Message.Source.MT_MOBILE_APP.getValue());

                newIntent = new Intent(context, MessageIntentService.class);
                newIntent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(replyMessage, Message.class));
                MessageIntentService.enqueueWork(context, newIntent, null);
                return;
            }
            //TODO: get activity name in intent...
            Class activity = null;
            try {
                activity = Class.forName(activityToOpen);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (activity == null) {

            }
            newIntent = new Intent(context, activity);
            newIntent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, messageJson);
            newIntent.putExtra(sms_body, "text");
            newIntent.setType(android_path);
            newIntent.setAction(NotificationBroadcastReceiver.LAUNCH_APP);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(newIntent);
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(WearableNotificationWithVoice.EXTRA_VOICE_REPLY);
        }
        return null;
    }


}