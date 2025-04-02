package io.kommunicate.devkit.api.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.text.TextUtils;

import io.kommunicate.devkit.Applozic;
import io.kommunicate.devkit.ApplozicClient;
import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.attachment.FileClientService;
import io.kommunicate.devkit.api.attachment.FileMeta;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.listners.ConstantsHandler;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.channel.ChannelUtils;
import io.kommunicate.commons.people.contact.Contact;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static io.kommunicate.devkit.api.notification.VideoCallNotificationHelper.CALL_AUDIO_ONLY;
import static io.kommunicate.devkit.api.notification.VideoCallNotificationHelper.CALL_ID;

import io.kommunicate.R;

/**
 * Created with IntelliJ IDEA.
 * User: devashish
 * Date: 17/3/13
 * Time: 7:36 PM
 */
public class NotificationService {
    public static final String NOTIFICATION_TEXT_NOT_AVAILABLE = "You received a message";
    public static final int NOTIFICATION_ID = 1000;
    private static final String TAG = "NotificationService";
    private static final String NOTIFICATION_SMALL_ICON_METADATA = "io.kommunicate.devkit.notification.smallIcon";
    private static final String NOTIFICATION_SMALL_ICON_COLOR = "io.kommunicate.devkit.notification.iconColor";
    private static String GROUP_KEY = "applozic_key";
    MessageDatabaseService messageDatabaseService;
    List<Message> unReadMessageList = new ArrayList<>();
    long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
    private Context context;
    private int iconResourceId;
    private int wearable_action_title;
    private int wearable_action_label;
    private int wearable_send_icon;
    private AppContactService appContactService;
    private ApplozicClient applozicClient;
    private String activityToOpen;
    private int notificationDisableThreshold = 0;
    private NotificationChannels notificationChannels;
    private String[] constArray = {MobiComKitConstants.LOCATION, MobiComKitConstants.AUDIO, MobiComKitConstants.VIDEO, MobiComKitConstants.ATTACHMENT};
    private String notificationFilePath;
    public static final String BADGE_COUNT = "BADGE_COUNT";
    public static final String NO_ALERT = "NO_ALERT";
    private final Integer notificationIconColor;
    private static final String KOMMUNICATE = "Kommunicate";
    private static final String CONTACT_ID = "CONTACT_ID";
    private static final String android_dir = "vnd.android-dir/mms-sms";
    private static final String contextBasedChat = "contextBasedChat";
    private static final String sent_from_notification = "sentFromNotification";
    private static final String key_String = "keyString";
    private static final String group_Id = "groupId";
    private static final String take_Order = "takeOrder";
    private static final String ACTIVITY_OPEN = "activity.open.on.notification";

    public NotificationService(int iconResourceID, Context context, int wearable_action_label, int wearable_action_title, int wearable_send_icon) {
        this.context = context;
        this.iconResourceId = iconResourceID;
        this.wearable_action_label = wearable_action_label;
        this.wearable_action_title = wearable_action_title;
        this.wearable_send_icon = wearable_send_icon;
        this.applozicClient = ApplozicClient.getInstance(context);
        this.appContactService = new AppContactService(context);
        this.activityToOpen = Utils.getMetaDataValue(context, ACTIVITY_OPEN);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.notificationDisableThreshold = applozicClient.getNotificationMuteThreshold();
        this.notificationFilePath = Applozic.getInstance(context).getCustomNotificationSound();
        this.notificationIconColor = Utils.getMetaDataValueForResources(context,NOTIFICATION_SMALL_ICON_COLOR);

        notificationChannels = new NotificationChannels(context, notificationFilePath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannels.prepareNotificationChannels();
        }
    }

    public void notifyUser(Contact contact, Channel channel, Message message, int index) {
        if (ApplozicClient.getInstance(context).isNotificationDisabled()) {
            Utils.printLog(context, TAG, "Notification is disabled !!");
            return;
        }
        Bitmap notificationIconBitmap = null;
        unReadMessageList = messageDatabaseService.getUnreadMessages();
        int count = appContactService.getChatConversationCount() + appContactService.getGroupConversationCount();
        int totalCount = messageDatabaseService.getTotalUnreadCount();

        Class activity = null;
        try {
            activity = Class.forName(activityToOpen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (message.getGroupId() != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                }
            } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                String userId = message.getTo();
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                }
            } else {
                notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
            }
        } else {
            notificationIconBitmap = appContactService.downloadContactImage(context, contact);
        }

        Integer smallIconResourceId = Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) != null ? Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) : iconResourceId;
        Intent intent;
        intent = new Intent(context, activity);
        if (count < 2) {
            intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        } else {
            intent.putExtra(MobiComKitConstants.QUICK_LIST, true);
        }
        if (applozicClient.isChatListOnNotificationIsHidden()) {
            intent.putExtra(take_Order, true);
        }
        if (applozicClient.isContextBasedChat()) {
            intent.putExtra(contextBasedChat, true);
        }
        intent.putExtra("sms_body", "text");
        intent.setType(android_dir);

        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context,
                    (int) (System.currentTimeMillis() & 0xfffffff),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );
        }
        else {
            pendingIntent = PendingIntent.getActivity(context,
                    (int) (System.currentTimeMillis() & 0xfffffff),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, notificationChannels.getDefaultChannelId(muteNotifications(index)))
                        .setSmallIcon(smallIconResourceId)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(muteNotifications(index) ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_HIGH)
                        .setWhen(System.currentTimeMillis());

        if(notificationIconColor != null)
            mBuilder.setColor(ContextCompat.getColor(context,notificationIconColor));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mBuilder.setGroup(GROUP_KEY);
            mBuilder.setGroupSummary(true);
        } else {
            if (totalCount != 0) {
                mBuilder.setNumber(totalCount);
            }
        }

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        if (ApplozicClient.getInstance(context).getVibrationOnNotification() && !muteNotifications(index)) {
            mBuilder.setVibrate(pattern);
        }
        if (!muteNotifications(index)) {
            mBuilder.setSound(TextUtils.isEmpty(notificationFilePath) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(notificationFilePath));
        }

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        // Sets a title for the Inbox in expanded layout

        inboxStyle.setBigContentTitle(getNotificationTitle(count, contact, channel, message));

        // Moves events into the expanded layout
        try {
            if (unReadMessageList != null) {
                for (Message messageString : unReadMessageList) {
                    if (messageString.getGroupId() != null) {
                        Channel unreadChannel = ChannelService.getInstance(context).getChannelByChannelKey(messageString.getGroupId());
                        if (unreadChannel != null && unreadChannel.getUnreadCount() == 0) {
                            continue;
                        }
                    } else {
                        Contact unreadCount = appContactService.getContactById(messageString.getContactIds());
                        if (unreadCount != null && unreadCount.getUnreadCount() == 0) {
                            continue;
                        }
                    }
                    inboxStyle.addLine(getSpannedText(getMessageBody(messageString, count, channel, contact)));
                }
            }
            // Moves the expanded layout object into the notification object.

        } catch (Exception e) {
            e.printStackTrace();
        }
        String summaryText = "";
        if (count < 1) {
            summaryText = "";
            mBuilder.setLargeIcon(notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(message.getGroupId() != null ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())));
            mBuilder.setContentText(getSpannedText(getMessageBody(message, count, channel, contact)));
        } else if (count >= 1 && count < 2) {
            summaryText = totalCount < 2 ? totalCount + " new message " : totalCount + " new messages ";
            mBuilder.setLargeIcon(notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(message.getGroupId() != null ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())));
            mBuilder.setContentText(summaryText);
        } else {
            summaryText = totalCount + " messages from " + count + " chats";
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconResourceId));
            mBuilder.setContentText(summaryText);
        }
        inboxStyle.setSummaryText(summaryText);
        mBuilder.setContentTitle(getNotificationTitle(count, contact, channel, message));
        mBuilder.setStyle(inboxStyle);

        // Issue the notification here.

        if (message.hasAttachment()) {
            try {
                InputStream in;
                FileMeta fileMeta = message.getFileMetas();
                HttpURLConnection httpConn = null;
                if (fileMeta.getThumbnailBlobKey() != null) {
                    Bitmap bitmap = new FileClientService(context).loadThumbnailImage(context, message, 200, 200);
                    mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        WearableNotificationWithVoice notificationWithVoice =
                new WearableNotificationWithVoice(mBuilder, wearable_action_title,
                        wearable_action_label, wearable_send_icon, NOTIFICATION_ID);
        notificationWithVoice.setCurrentContext(context);
        notificationWithVoice.setPendingIntent(pendingIntent);

        try {
            if (unReadMessageList != null && unReadMessageList.size() > 0) {
                notificationWithVoice.sendNotification();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CharSequence getNotificationTitle(int conversationCount, Contact contact, Channel channel, Message message) {
        if (conversationCount < 2) {
            String notificationTitle = null;
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Contact receiverContact = appContactService.getContactById(userId);
                        if (receiverContact != null) {
                            notificationTitle = receiverContact.getDisplayName();
                        }
                    }
                } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                    String userId = message.getTo();
                    if (!TextUtils.isEmpty(userId)) {
                        Contact receiverContact = appContactService.getContactById(userId);
                        if (receiverContact != null) {
                            notificationTitle = receiverContact.getDisplayName();
                        }
                    }
                } else {
                    notificationTitle = channel.getName().trim();
                }
            } else if (contact != null) {
                notificationTitle = contact.getDisplayName().trim();
            }
            return Utils.getStyleString(notificationTitle);
        } else {
            return Utils.getStyleString(ApplozicClient.getInstance(context).getAppName());
        }
    }


    public CharSequence getMessageBody(Message message, int count, Channel channel, Contact contact) {
        String notificationText;
        if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
            notificationText = getText(0);
        } else if (message.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            notificationText = getText(1);
        } else if (message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            notificationText = getText(2);
        } else if (message.hasAttachment() && TextUtils.isEmpty(message.getMessage())) {
            notificationText = getText(3);
        } else {
            notificationText = message.getMessage();
        }
        CharSequence messageBody;
        Contact messageContactDisplayName = contact != null ? contact : appContactService.getContactById(message.getTo());
        if (message.getGroupId() != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                messageBody = Utils.getStyleStringForMessage(notificationText);
            } else {
                messageBody = Utils.getStyledStringForChannel(messageContactDisplayName.getDisplayName(), channel.getName(), notificationText);
            }
        } else {
            if (count < 2) {
                messageBody = Utils.getStyleStringForMessage(notificationText);
            } else {
                messageBody = Utils.getStyledStringForContact(messageContactDisplayName.getDisplayName(), notificationText);
            }
        }
        return messageBody;
    }

    private NotificationInfo getNotificationInfo(Contact contact, Channel channel, Message message) {
        if (ApplozicClient.getInstance(context).isNotificationDisabled()) {
            Utils.printLog(context, TAG, "Notification is disabled");
            return null;
        }
        String title = null;
        Bitmap notificationIconBitmap = null;
        Contact displayNameContact = null;
        if (message.getGroupId() != null) {
            if (channel == null) {
                return null;
            }
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                    title = newContact.getDisplayName();
                }
            } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                String userId = message.getTo();
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
                    title = newContact.getDisplayName();
                }
            } else {
                displayNameContact = appContactService.getContactById(message.getTo());
                title = ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(context).getUserId());
                notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
            }
        } else {
            title = contact.getDisplayName();
            notificationIconBitmap = appContactService.downloadContactImage(context, contact);
        }

        NotificationInfo notificationInfo = new NotificationInfo();
        notificationInfo.displayNameContact = displayNameContact;
        notificationInfo.notificationIconBitmap = notificationIconBitmap;
        notificationInfo.smallIconResourceId = Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) != null ? Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) : iconResourceId;
        notificationInfo.title = title;
        notificationInfo.notificationIconColor = notificationIconColor;

        return notificationInfo;
    }

    public void notifyUserForNormalMessage(Contact contact, Channel channel, Message message, int index) {
        String notificationText;
        NotificationInfo notificationInfo = getNotificationInfo(contact, channel, message);
        if(notificationInfo == null) {
            return;
        }
        Bitmap notificationIconBitmap = notificationInfo.notificationIconBitmap;
        Contact displayNameContact = notificationInfo.displayNameContact;

        if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
            notificationText = getText(0);
        } else if (message.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            notificationText = getText(1);
        } else if (message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            notificationText = getText(2);
        } else if (message.hasAttachment() && TextUtils.isEmpty(message.getMessage())) {
            notificationText = getText(3);
        } else {
            notificationText = message.getMessage();
        }

        if(TextUtils.isEmpty(notificationText))
        {
            List<Message> unreadMessages = messageDatabaseService.getUnreadMessages();
            for (Message unreadMessage : unreadMessages)
            {
                if (!TextUtils.isEmpty(unreadMessage.getMessage()))
                {
                    notificationText = unreadMessage.getMessage();
                    break;
                }
            }
        }

        if(TextUtils.isEmpty(notificationText))
        {
            notificationText = NOTIFICATION_TEXT_NOT_AVAILABLE;
        }

        Class activity = null;
        try {
            activity = Class.forName(activityToOpen);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, activity);
        intent.putExtra(key_String, message.getKeyString());
        intent.putExtra(group_Id, message.getGroupId());
        if (applozicClient.isChatListOnNotificationIsHidden()) {
            intent.putExtra(take_Order, true);
        }
        if (applozicClient.isContextBasedChat()) {
            intent.putExtra(contextBasedChat, true);
        }
        intent.putExtra(sent_from_notification, true);
        intent.putExtra("sms_body", "text");
        intent.setType(android_dir);

        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context,
                    (int) (System.currentTimeMillis() & 0xfffffff),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );
        }
        else {
            pendingIntent = PendingIntent.getActivity(context,
                    (int) (System.currentTimeMillis() & 0xfffffff),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, notificationChannels.getDefaultChannelId(muteNotifications(index)));

        mBuilder.setSmallIcon(notificationInfo.smallIconResourceId)
                .setLargeIcon(ApplozicClient.getInstance(context).isShowAppIconInNotification() ? BitmapFactory.decodeResource(context.getResources(), iconResourceId) : notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(channel != null && !(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(muteNotifications(index) ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(notificationInfo.title)
                .setContentText(channel != null && !(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) ? (displayNameContact != null ? (displayNameContact.getDisplayName() + ": " + getSpannedText(notificationText)) : "" + getSpannedText(notificationText)) : getSpannedText(notificationText));

        if(notificationInfo.notificationIconColor != null)
            mBuilder.setColor(ContextCompat.getColor(context,notificationInfo.notificationIconColor));

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        if (ApplozicClient.getInstance(context).isUnreadCountBadgeEnabled()) {
            int totalCount = messageDatabaseService.getTotalUnreadCount();
            if (totalCount != 0) {
                mBuilder.setNumber(totalCount);
            }
        }
        if (!muteNotifications(index)) {
            mBuilder.setSound(TextUtils.isEmpty(notificationFilePath) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(notificationFilePath));
        }
        if (message.hasAttachment()) {
            try {
                FileMeta fileMeta = message.getFileMetas();
                HttpURLConnection httpConn = null;
                if (fileMeta.getThumbnailBlobKey() != null) {
                    Bitmap bitmap = new FileClientService(context).loadThumbnailImage(context, message, 200, 200);
                    mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        WearableNotificationWithVoice notificationWithVoice =
                new WearableNotificationWithVoice(mBuilder, wearable_action_title,
                        wearable_action_label, wearable_send_icon, message.getGroupId() != null ? String.valueOf(message.getGroupId()).hashCode() : message.getContactIds().hashCode());
        notificationWithVoice.setCurrentContext(context);
        notificationWithVoice.setPendingIntent(pendingIntent);

        try {
            notificationWithVoice.sendNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCallNotification(Contact contact, Message message, String isAudioCallOnly, String callId) {
        NotificationInfo notificationInfo = getNotificationInfo(contact, null, message);
        if(notificationInfo == null) {
            return;
        }

        Intent fullScreenIntent = null;
        try {
            fullScreenIntent = new Intent(context, Class.forName(VideoCallNotificationHelper.NOTIFICATION_ACTIVITY_NAME));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fullScreenIntent.putExtra(CONTACT_ID, message.getTo());
        fullScreenIntent.putExtra(CALL_ID, callId);
        if (!TextUtils.isEmpty(isAudioCallOnly) && "true".equals(isAudioCallOnly)) {
            fullScreenIntent.putExtra(CALL_AUDIO_ONLY, true);
        }

        PendingIntent fullScreenPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, notificationChannels.getCallChannelId())
                        .setSmallIcon(notificationInfo.smallIconResourceId)
                        .setContentTitle(context.getString(R.string.incomming_call) + notificationInfo.title + ".")
                        .setContentText(context.getString(R.string.open_call))
                        .setVibrate(new long[] {2000L, 1000L, 2000L, 1000L})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        if(notificationInfo.notificationIconColor != null)
            notificationBuilder.setColor(notificationInfo.notificationIconColor);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(message.getGroupId() != null ? String.valueOf(message.getGroupId()).hashCode() : message.getContactIds().hashCode(), incomingCallNotification);
    }

    public String getSpannedText(CharSequence message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message.toString(), Html.FROM_HTML_MODE_COMPACT).toString();
        } else {
            return Html.fromHtml(message.toString()).toString();
        }
    }

    public String getText(int index) {
        if (context.getApplicationContext() instanceof ConstantsHandler) {
            return getTextFromIndex(((ConstantsHandler) context.getApplicationContext()).getNotificationTexts(), index);
        }

        return constArray[index];
    }

    public String getTextFromIndex(String[] texts, int index) {
        if (texts != null && texts.length == 4) {
            return texts[index];
        }
        return null;
    }

    public boolean muteNotifications(int index) {
        return !(notificationDisableThreshold == 0 || (notificationDisableThreshold > 0 && index < notificationDisableThreshold));
    }

    public void sendTestNotification() {
        Integer smallIconResourceId = Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) != null ? Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) : iconResourceId;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannels.getDefaultChannelId(false))
                .setSmallIcon(smallIconResourceId)
                .setContentTitle(KOMMUNICATE)
                .setContentText(context.getString(R.string.notification_testing))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(123, builder.build());
    }

    static class NotificationInfo {
        String title;
        Contact displayNameContact;
        Integer smallIconResourceId;
        Bitmap notificationIconBitmap;
        Integer notificationIconColor;
    }
}