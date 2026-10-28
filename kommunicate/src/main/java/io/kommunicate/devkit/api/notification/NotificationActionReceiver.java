package io.kommunicate.devkit.api.notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

import io.kommunicate.R;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.conversation.ConversationHelper;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.MessageClientService;

/**
 * Handles the Reply and Mark as read actions of a chat notification.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";
    private static final String ACTION_REPLY ="io.kommunicate.devkit.notification.REPLY";
    private static final String ACTION_MARK_AS_READ = "io.kommunicate.devkit.notification.MARK_AS_READ";
    private static final String KEY_REPLY_TEXT = "KEY_REPLY_TEXT";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String USER_ID = "USER_ID";

    static void addActions(Context context, NotificationCompat.Builder builder, Message message, int notificationId) {
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY_TEXT)
                .setLabel(context.getString(R.string.km_notification_reply_hint))
                .build();

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(0,
                context.getString(R.string.km_notification_reply), getActionIntent(context, ACTION_REPLY, message, notificationId, true))
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(false)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .setShowsUserInterface(false)
                .build();

        NotificationCompat.Action markAsReadAction = new NotificationCompat.Action.Builder(0,
                context.getString(R.string.km_notification_mark_as_read), getActionIntent(context, ACTION_MARK_AS_READ, message, notificationId, false))
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                .setShowsUserInterface(false)
                .build();

        builder.addAction(replyAction).addAction(markAsReadAction);
    }

    private static PendingIntent getActionIntent(Context context, String action, Message message, int notificationId, boolean mutable) {
        Intent intent = new Intent(context, NotificationActionReceiver.class)
                .setAction(action)
                .putExtra(NOTIFICATION_ID, notificationId);
        if (message.getGroupId() != null) {
            intent.putExtra(GROUP_ID, message.getGroupId());
        } else {
            intent.putExtra(USER_ID, message.getContactIds());
        }
        // RemoteInput needs a mutable PendingIntent to receive the typed reply.
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (mutable
                ? (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0)
                : PendingIntent.FLAG_IMMUTABLE);
        return PendingIntent.getBroadcast(context, notificationId, intent, flags);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isReply = ACTION_REPLY.equals(intent.getAction());
        if (!isReply && !ACTION_MARK_AS_READ.equals(intent.getAction())) {
            return;
        }

        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        Integer groupId = intent.hasExtra(GROUP_ID) ? intent.getIntExtra(GROUP_ID, 0) : null;
        String userId = intent.getStringExtra(USER_ID);
        String replyText = isReply ? getReplyText(intent) : null;
        if (isReply && TextUtils.isEmpty(replyText)) {
            return;
        }

        final Context appContext = context.getApplicationContext();
        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                if (isReply) {
                    sendReply(appContext, replyText, groupId, userId);
                }
                ConversationHelper.markAsRead(appContext, null, userId, groupId);
                NotificationManagerCompat.from(appContext).cancel(notificationId);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private static String getReplyText(Intent intent) {
        Bundle results = RemoteInput.getResultsFromIntent(intent);
        CharSequence replyText = results != null ? results.getCharSequence(KEY_REPLY_TEXT) : null;
        return replyText != null ? replyText.toString().trim() : null;
    }

    private static void sendReply(Context context, String replyText, Integer groupId, String userId) {
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(context);
        Message message = new Message();
        if (groupId != null) {
            message.setGroupId(groupId);
        } else {
            message.setTo(userId);
            message.setContactIds(userId);
        }
        message.setMessage(replyText);
        message.setContentType(Message.ContentType.DEFAULT.getValue());
        message.setRead(Boolean.TRUE);
        message.setStoreOnDevice(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setDeviceKeyString(userPreference.getDeviceKeyString());
        message.setCreatedAtTime(System.currentTimeMillis() + userPreference.getDeviceTimeOffset());

        String defaultMetadata = SettingsSharedPreference.getInstance(context).getMessageMetaData();
        if (!TextUtils.isEmpty(defaultMetadata)) {
            Map<String, String> metadata = GsonUtils.getObjectFromJson(defaultMetadata, new TypeToken<Map<String, String>>() {
            }.getType());
            message.setMetadata(metadata);
        }

        // Sent directly on this thread: MessageIntentService runs through JobScheduler, which can defer it while the app is in the background.
        // If sending fails, the message stays stored as pending and the SDK sends it on its next pending-message sync.
        try {
            new MessageClientService(context).sendMessageToServer(message, null);
        } catch (Exception e) {
            Utils.printLog(context, TAG, "Unable to send notification reply");
        }
    }
}
