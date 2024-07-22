package com.applozic.mobicomkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devashish on 8/21/2015.
 */
public class ApplozicClient {

    public static final String SERVER_SYNC = "SERVER_SYNC_[CONVERSATION]_[CONTACT]_[CHANNEL]";
    public static final String AL_MESSAGE_META_DATA_KEY = "AL_MESSAGE_META_DATA_KEY";
    private static final String HANDLE_DISPLAY_NAME = "CLIENT_HANDLE_DISPLAY_NAME";
    private static final String HANDLE_DIAL = "CLIENT_HANDLE_DIAL";
    private static final String CHAT_LIST_HIDE_ON_NOTIFICATION = "CHAT_LIST_HIDE_ON_NOTIFICATION";
    private static final String CONTEXT_BASED_CHAT = "CONTEXT_BASED_CHAT";
    private static final String NOTIFICATION_SMALL_ICON = "NOTIFICATION_SMALL_ICON";
    private static final String APP_NAME = "APP_NAME";
    private static final String NOTIFICATION_DISABLE = "NOTIFICATION_DISABLE";
    private static final String CONTACT_DEFAULT_IMAGE = "CONTACT_DEFAULT_IMAGE";
    private static final String GROUP_DEFAULT_IMAGE = "GROUP_DEFAULT_IMAGE";
    private static final String MESSAGE_META_DATA_SERVICE = "MESSAGE_META_DATA_SERVICE";
    private static final String ENABLE_IP_CALL = "ENABLE_IP_CALL";
    private static final String SHOW_MY_CONTACT_ONLY = "SHOW_MY_CONTACT_ONLY";
    private static final String START_GROUP_OF_TWO = "START_GROUP_OF_TWO";
    private static final String AL_SHOW_APP_ICON = "AL_SHOW_APP_ICON";
    private static String NOTIFICATION_STACKING = "NOTIFICATION_STACKING";
    private static final String BADGE_COUNT_ENABLE = "BADGE_COUNT_ENABLE";
    private static String vibration_notification = "vibration_notification";
    private static final String S3_STORAGE_SERVICE_ENABLED = "S3_STORAGE_SERVICE_ENABLED";
    private static final String STORAGE_SERVICE_ENABLE = "STORAGE_SERVICE_ENABLE";
    private static final String GOOGLE_CLOUD_SERVICE_ENABLE = "GOOGLE_CLOUD_SERVICE_ENABLE";
    private static final String CUSTOM_MESSAGE_TEMPLATE = "CUSTOM_MESSAGE_TEMPLATE";
    private static final String AL_SUBGROUP_SUPPORT = "AL_SUBGROUP_SUPPORT";
    private static final String HIDE_ACTION_MESSAGES = "HIDE_ACTION_MESSAGES";
    private static final String NOTIFICATION_MUTE_THRESHOLD = "NOTIFICATION_MUTE_THRESHOLD";
    private static final String SKIP_DELETED_GROUPS = "SKIP_DELETED_GROUPS";
    private static final String MIN_CREATED_AT_KEY = "mck.sms.createdAt.min";
    private static final String MAX_CREATED_AT_KEY = "mck.sms.createdAt.max";
    private static final String AL_CONVERSATION_LIST_PAGE_SIZE_KEY = "AL_CONVERSATION_LIST_PAGE_SIZE_KEY";
    private static final int conversationListDefaultMainPageSize = 60;

    public static ApplozicClient applozicClient;
    public SharedPreferences sharedPreferences;
    private Context context;

    private ApplozicClient(Context context) {
        this.context = ApplozicService.getContext(context);
        MobiComUserPreference.renameSharedPrefFile(context);
        sharedPreferences = ApplozicService.getContext(context).getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static ApplozicClient getInstance(Context context) {
        if (applozicClient == null) {
            applozicClient = new ApplozicClient(ApplozicService.getContext(context));
        }

        return applozicClient;
    }

    public boolean isHandleDisplayName() {
        return sharedPreferences.getBoolean(HANDLE_DISPLAY_NAME, true);
    }

    public ApplozicClient setHandleDisplayName(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DISPLAY_NAME, enable).commit();
        return this;
    }

    public boolean isHandleDial() {
        return sharedPreferences.getBoolean(HANDLE_DIAL, false);
    }

    public ApplozicClient setHandleDial(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DIAL, enable).commit();
        return this;
    }

    public ApplozicClient hideChatListOnNotification() {
        sharedPreferences.edit().putBoolean(CHAT_LIST_HIDE_ON_NOTIFICATION, true).commit();
        return this;
    }

    public boolean isChatListOnNotificationIsHidden() {
        return sharedPreferences.getBoolean(CHAT_LIST_HIDE_ON_NOTIFICATION, false);
    }

    public boolean isContextBasedChat() {
        return sharedPreferences.getBoolean(CONTEXT_BASED_CHAT, false);
    }

    public ApplozicClient setContextBasedChat(boolean enable) {
        sharedPreferences.edit().putBoolean(CONTEXT_BASED_CHAT, enable).commit();
        return this;
    }

    public ApplozicClient hideNotificationSmallIcon() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_SMALL_ICON, true).commit();
        return this;
    }

    public boolean isNotificationSmallIconHidden() {
        return sharedPreferences.getBoolean(NOTIFICATION_SMALL_ICON, false);
    }

    public boolean isNotAllowed() {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        return !isDebuggable && (pref.getPricingPackage() == RegistrationResponse.PricingType.CLOSED.getValue()
                || pref.getPricingPackage() == RegistrationResponse.PricingType.BETA.getValue());
    }

    public boolean isServiceDisconnected() {
        int pricingPackage = MobiComUserPreference.getInstance(context).getPricingPackage();
        return (pricingPackage == -1 || pricingPackage == 6 || (pricingPackage == 0 && !Utils.isDebugBuild(context)));
    }

    public boolean isAccountClosed() {
        return MobiComUserPreference.getInstance(context).getPricingPackage() == RegistrationResponse.PricingType.CLOSED.getValue() || MobiComUserPreference.getInstance(context).getPricingPackage() == RegistrationResponse.PricingType.UNSUBSCRIBED.getValue();
    }

    public String getAppName() {
        return sharedPreferences.getString(APP_NAME, "Applozic");
    }

    public ApplozicClient setAppName(String notficationAppName) {
        sharedPreferences.edit().putString(APP_NAME, notficationAppName).commit();
        return this;
    }

    public boolean isNotificationDisabled() {
        return sharedPreferences.getBoolean(NOTIFICATION_DISABLE, false);
    }

    public ApplozicClient enableNotification() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_DISABLE, false).commit();
        return this;
    }

    public ApplozicClient disableNotification() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_DISABLE, true).commit();
        return this;
    }

    public String getDefaultContactImage() {
        return sharedPreferences.getString(CONTACT_DEFAULT_IMAGE, "applozic_ic_contact_picture_holo_light");
    }

    public ApplozicClient setDefaultContactImage(String imageName) {
        sharedPreferences.edit().putString(CONTACT_DEFAULT_IMAGE, imageName).commit();
        return this;
    }

    public String getDefaultChannelImage() {
        return sharedPreferences.getString(GROUP_DEFAULT_IMAGE, "applozic_group_icon");
    }

    public ApplozicClient setDefaultChannelImage(String groupImageName) {
        sharedPreferences.edit().putString(GROUP_DEFAULT_IMAGE, groupImageName).commit();
        return this;
    }

    public String getMessageMetaDataServiceName() {
        return sharedPreferences.getString(MESSAGE_META_DATA_SERVICE, null);
    }

    public ApplozicClient setMessageMetaDataServiceName(String messageMetaDataServiceName) {
        sharedPreferences.edit().putString(MESSAGE_META_DATA_SERVICE, messageMetaDataServiceName).commit();
        return this;
    }

    public boolean isShowMyContacts() {
        return sharedPreferences.getBoolean(SHOW_MY_CONTACT_ONLY, false);
    }

    public ApplozicClient enableShowMyContacts() {
        sharedPreferences.edit().putBoolean(SHOW_MY_CONTACT_ONLY, true).commit();
        return this;
    }

    public ApplozicClient disableShowMyContacts() {
        sharedPreferences.edit().putBoolean(SHOW_MY_CONTACT_ONLY, false).commit();
        return this;
    }

    public boolean isIPCallEnabled() {
        return sharedPreferences.getBoolean(ENABLE_IP_CALL, false);
    }

    public void setIPCallEnabled(boolean iPCallEnabled) {
        sharedPreferences.edit().putBoolean(ENABLE_IP_CALL, iPCallEnabled).commit();
    }

    public String getMessageMetaData() {
        return sharedPreferences.getString(AL_MESSAGE_META_DATA_KEY, null);
    }

    public ApplozicClient setMessageMetaData(Map<String, String> messageMetaDataMap) {
        if (messageMetaDataMap != null) {
            sharedPreferences.edit().putString(AL_MESSAGE_META_DATA_KEY, new JSONObject(messageMetaDataMap).toString()).commit();
        }
        return this;
    }

    public ApplozicClient startGroupOfTwo() {
        sharedPreferences.edit().putBoolean(START_GROUP_OF_TWO, true).commit();
        return this;
    }

    public boolean isStartGroupOfTwo() {
        return sharedPreferences.getBoolean(START_GROUP_OF_TWO, false);
    }

    public ApplozicClient disableStartGroupOfTwo() {
        sharedPreferences.edit().putBoolean(START_GROUP_OF_TWO, false).commit();
        return this;
    }

    public ApplozicClient showAppIconInNotification(boolean showOrHide) {
        sharedPreferences.edit().putBoolean(AL_SHOW_APP_ICON, showOrHide).commit();
        return this;
    }

    public boolean isShowAppIconInNotification() {
        return sharedPreferences.getBoolean(AL_SHOW_APP_ICON, false);
    }


    public boolean isNotificationStacking() {
        return sharedPreferences.getBoolean(NOTIFICATION_STACKING, false);
    }

    public void setNotificationStacking(boolean enableOrDisable) {
        sharedPreferences.edit().putBoolean(NOTIFICATION_STACKING, enableOrDisable).commit();
    }


    public ApplozicClient enableShowUnreadCountBadge() {
        sharedPreferences.edit().putBoolean(BADGE_COUNT_ENABLE, true).commit();
        return this;
    }

    public boolean isUnreadCountBadgeEnabled() {
        return sharedPreferences.getBoolean(BADGE_COUNT_ENABLE, false);

    }

    public boolean getVibrationOnNotification() {
        return sharedPreferences.getBoolean(vibration_notification, false);
    }

    public void setVibrationOnNotification(boolean enable) {
        sharedPreferences.edit().putBoolean(vibration_notification, enable).commit();
    }


    public ApplozicClient enableS3StorageService() {
        sharedPreferences.edit().putBoolean(S3_STORAGE_SERVICE_ENABLED, true).commit();
        return this;
    }

    public boolean isS3StorageServiceEnabled() {
        return sharedPreferences.getBoolean(S3_STORAGE_SERVICE_ENABLED, false);
    }

    public ApplozicClient enableGoogleCloudService() {
        sharedPreferences.edit().putBoolean(GOOGLE_CLOUD_SERVICE_ENABLE, true).commit();
        return this;
    }

    public boolean isGoogleCloudServiceEnabled() {
        return sharedPreferences.getBoolean(GOOGLE_CLOUD_SERVICE_ENABLE, false);
    }

    public ApplozicClient setStorageServiceEnabled(boolean enable) {
        sharedPreferences.edit().putBoolean(STORAGE_SERVICE_ENABLE, enable).commit();
        return this;
    }

    public boolean isStorageServiceEnabled() {
        return sharedPreferences.getBoolean(STORAGE_SERVICE_ENABLE, false);
    }

    public Map<String, String> getMessageTemplates() {
        return (Map<String, String>) GsonUtils.getObjectFromJson(sharedPreferences.getString(CUSTOM_MESSAGE_TEMPLATE, null), Map.class);
    }

    public ApplozicClient setMessageTemplates(Map<String, String> messageTemplates) {
        sharedPreferences.edit().putString(CUSTOM_MESSAGE_TEMPLATE, GsonUtils.getJsonFromObject(messageTemplates, Map.class)).commit();
        return this;
    }

    public boolean isSubGroupEnabled() {
        return sharedPreferences.getBoolean(AL_SUBGROUP_SUPPORT, false);
    }

    public ApplozicClient setSubGroupSupport(boolean subgroup) {
        sharedPreferences.edit().putBoolean(AL_SUBGROUP_SUPPORT, subgroup).commit();
        return this;
    }

    public boolean isActionMessagesHidden() {
        return sharedPreferences.getBoolean(HIDE_ACTION_MESSAGES, false);
    }

    public ApplozicClient hideActionMessages(boolean hide) {
        sharedPreferences.edit().putBoolean(HIDE_ACTION_MESSAGES, hide).commit();
        return this;
    }

    public ApplozicClient setNotificationMuteThreashold(int threshold) {
        sharedPreferences.edit().putInt(NOTIFICATION_MUTE_THRESHOLD, threshold).commit();
        return this;
    }

    public int getNotificationMuteThreshold() {
        return sharedPreferences.getInt(NOTIFICATION_MUTE_THRESHOLD, 0);
    }

    public ApplozicClient skipDeletedGroups(boolean skip) {
        sharedPreferences.edit().putBoolean(SKIP_DELETED_GROUPS, skip).commit();
        return this;
    }

    public boolean isSkipDeletedGroups() {
        return sharedPreferences.getBoolean(SKIP_DELETED_GROUPS, false);
    }

    public boolean wasServerCallDoneBefore(Contact contact, Channel channel, Integer conversationId) {
        return sharedPreferences.getBoolean(getServerSyncCallKey(contact, channel, conversationId), false);
    }

    public void updateServerCallDoneStatus(Contact contact, Channel channel, Integer conversationId) {
        sharedPreferences.edit().putBoolean(getServerSyncCallKey(contact, channel, conversationId), true).commit();
    }

    public String getServerSyncCallKey(Contact contact, Channel channel, Integer conversationId) {

        if (contact == null && channel == null) {
            return SERVER_SYNC + "LIST_CALL";
        }

        return SERVER_SYNC.replace("[CONVERSATION]", (conversationId != null && conversationId != 0) ? String.valueOf(conversationId) : "")
                .replace("[CONTACT]", contact != null ? contact.getContactIds() : "")
                .replace("[CHANNEL]", channel != null ? String.valueOf(channel.getKey()) : "");
    }

    public long getMaxCreatedAtTime() {
        return sharedPreferences.getLong(MAX_CREATED_AT_KEY, Long.MAX_VALUE);
    }

    public ApplozicClient setMaxCreatedAtTime(long createdAtTime) {
        sharedPreferences.edit().putLong(MAX_CREATED_AT_KEY, createdAtTime).commit();
        return this;
    }

    public long getMinCreatedAtTime() {
        return sharedPreferences.getLong(MIN_CREATED_AT_KEY, 0);
    }

    public ApplozicClient setMinCreatedAtTime(long createdAtTime) {
        sharedPreferences.edit().putLong(MIN_CREATED_AT_KEY, createdAtTime).commit();
        return this;
    }

    public ApplozicClient disableChatForUser(final boolean disable, final AlCallback callback) {
        Map<String, String> userMetadata;
        Contact contact = new AppContactService(context).getContactById(MobiComUserPreference.getInstance(context).getUserId());
        if (contact != null && contact.getMetadata() != null) {
            userMetadata = contact.getMetadata();
            userMetadata.putAll(contact.getMetadata());
        } else {
            userMetadata = new HashMap<>();
        }
        userMetadata.put(Contact.DISABLE_CHAT_WITH_USER, String.valueOf(disable));

        User user = new User();
        user.setMetadata(userMetadata);
        UserService.getInstance(context).updateUser(user, true, new AlCallback() {
            @Override
            public void onSuccess(Object response) {
                sharedPreferences.edit().putBoolean(Contact.DISABLE_CHAT_WITH_USER, disable).commit();
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(Object error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
        return this;
    }

    public ApplozicClient setChatDisabled(boolean disable) {
        sharedPreferences.edit().putBoolean(Contact.DISABLE_CHAT_WITH_USER, disable).commit();
        return this;
    }

    public boolean isChatForUserDisabled() {
        return sharedPreferences.getBoolean(Contact.DISABLE_CHAT_WITH_USER, false);
    }

    public ApplozicClient setFetchConversationListMainPageSize(int mainPageSize) {
        sharedPreferences.edit().putInt(AL_CONVERSATION_LIST_PAGE_SIZE_KEY, mainPageSize).commit();
        return this;
    }

    public int getFetchConversationListMainPageSize() {
        int mainPageSize = sharedPreferences.getInt(AL_CONVERSATION_LIST_PAGE_SIZE_KEY, conversationListDefaultMainPageSize);
        if (mainPageSize < 0) {
            return conversationListDefaultMainPageSize;
        }
        return mainPageSize;
    }
}
