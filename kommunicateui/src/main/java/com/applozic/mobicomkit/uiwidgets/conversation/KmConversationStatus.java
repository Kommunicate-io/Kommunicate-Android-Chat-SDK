package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelUpdateTask;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class KmConversationStatus {
    private static final String TAG = "KmConversationStatus";
    public static final String OPEN_STATUS_NAME = getString(R.string.km_resolve_conversation);
    public static final String RESOLVED_STATUS_NAME = getString(R.string.km_reopen_conversation);
    //public static final String SPAM_STATUS_NAME = getString(R.string.km_conversation_marked_as_spam);
    public static final String MARK_AS_SPAM = getString(R.string.km_status_mark_as_spam);
    public static final int STATUS_OPEN = 0;
    public static final int STATUS_RESOLVED = 2;
    public static final int STATUS_SPAM = 3;

    public static String getStatusText(int status) {
        switch (status) {
            case 2:
            case 3:
                return RESOLVED_STATUS_NAME;
            default:
                return OPEN_STATUS_NAME;
        }
    }

    public static String getStatusName(int status) {
        switch (status) {
            case 2:
                return getString(R.string.km_status_resolved);
            case 3:
                return getString(R.string.km_status_spam);
            default:
                return getString(R.string.km_status_open);
        }
    }

    public static int getIconId(int status) {
        return R.drawable.ic_spam;
    }

    public static int getColorId(int status) {
        switch (status) {
            case 2:
            case 3:
                return R.color.km_reopen_status_color;
            default:
                return R.color.km_resolve_status_color;
        }
    }

    public static int getStatusForUpdate(String name) {
        if (RESOLVED_STATUS_NAME.equals(name)) {
            return 0;
        } else if (MARK_AS_SPAM.equals(name)) {
            return 3;
        }
        return 2;
    }

    public static int getStatusFromName(String name) {
        if (getString(R.string.km_status_resolved).equals(name)) {
            return STATUS_RESOLVED;
        } else if (getString(R.string.km_status_spam).equals(name)) {
            return STATUS_SPAM;
        }
        return STATUS_OPEN;
    }

    private static String getString(int resId) {
        return Utils.getString(ApplozicService.getAppContext(), resId);
    }

    public static void updateConversationStatus(final KmResolve data, final Channel channel) {
        if (channel != null) {
            Map<String, String> metadata = new HashMap<>();

            metadata.put(Channel.CONVERSATION_STATUS, String.valueOf(KmConversationStatus.getStatusForUpdate(data.getStatusName())));
            channel.setMetadata(metadata);

            GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(metadata, channel.getKey());

            new AlChannelUpdateTask(ApplozicService.getAppContext(), groupInfoUpdate, new AlChannelUpdateTask.AlChannelUpdateListener() {
                @Override
                public void onSuccess(Context context) {
                    sendConversationStatusUpdateMessage(context, channel, data);
                }

                @Override
                public void onFailure(Context context) {

                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static void sendConversationStatusUpdateMessage(Context context, Channel channel, KmResolve data) {
        if (channel != null) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(Message.KM_STATUS, KmConversationStatus.getStatusName(KmConversationStatus.getStatusForUpdate(data.getStatusName())));
            metadata.put(Message.KM_SKIP_BOT, "true");
            metadata.put(NotificationService.NO_ALERT, "false");
            metadata.put(NotificationService.BADGE_COUNT, "false");
            metadata.put(Message.MetaDataType.KEY.getValue(), Message.MetaDataType.ARCHIVE.getValue());

            new MessageBuilder(context)
                    .setContentType(Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue())
                    .setMessage(Utils.getString(context, R.string.km_change_status_to_message) + " " + KmConversationStatus.getStatusName(KmConversationStatus.getStatusForUpdate(data.getStatusName())))
                    .setGroupId(channel.getKey())
                    .setMetadata(metadata)
                    .send();
        }
    }
}
