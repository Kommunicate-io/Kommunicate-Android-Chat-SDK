package io.kommunicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;

import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.kommunicate.async.GetUserListAsyncTask;
import io.kommunicate.async.KMFaqTask;
import io.kommunicate.async.KMHelpDocsKeyTask;
import io.kommunicate.async.KmAwayMessageTask;
import io.kommunicate.async.KmConversationCreateTask;
import io.kommunicate.async.KmConversationInfoTask;
import io.kommunicate.async.KmGetAgentListTask;
import io.kommunicate.async.KmUserLoginTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.callbacks.KmGetConversationInfoCallback;
import io.kommunicate.callbacks.KmPrechatCallback;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.database.KmDatabaseHelper;
import io.kommunicate.models.KmAgentModel;
import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;

/**
 * Created by ashish on 23/01/18.
 */

public class Kommunicate {

    private static final String KM_BOT = "bot";
    private static final String TAG = "KommunicateTag";
    private static final String CONVERSATION_ASSIGNEE = "CONVERSATION_ASSIGNEE";
    private static final String SKIP_ROUTING = "SKIP_ROUTING";
    public static final String KM_CHAT_CONTEXT = "KM_CHAT_CONTEXT";
    public static final String KM_ALREADY_LOGGED_IN_STATUS = "ALREADY_LOGGED_IN";

    public static void init(Context context, String applicationKey) {
        Applozic.init(context, applicationKey);
    }

    public static void login(Context context, final KMUser kmUser, final KMLoginHandler handler) {
        if (isLoggedIn(context)) {
            String loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
            if (loggedInUserId.equals(kmUser.getUserId())) {
                RegistrationResponse registrationResponse = new RegistrationResponse();
                registrationResponse.setStatusMessage(KM_ALREADY_LOGGED_IN_STATUS);
                Contact contact = new ContactDatabase(context).getContactById(loggedInUserId);
                if (contact != null) {
                    registrationResponse.setUserId(contact.getUserId());
                    registrationResponse.setContactNumber(contact.getContactNumber());
                    registrationResponse.setRoleType(contact.getRoleType());
                    registrationResponse.setImageLink(contact.getImageURL());
                    registrationResponse.setDisplayName(contact.getDisplayName());
                    registrationResponse.setStatusMessage(contact.getStatus());
                }
                handler.onSuccess(registrationResponse, context);
            } else {
                logout(context, new KMLogoutHandler() {
                    @Override
                    public void onSuccess(Context context) {
                        login(context, kmUser, handler, null);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        handler.onFailure(null, exception);
                    }
                });
            }
        } else {
            login(context, kmUser, handler, null);
        }
    }

    public static void login(Context context, KMUser kmUser, KMLoginHandler handler, ResultReceiver prechatReceiver) {
        if (kmUser != null) {
            kmUser.setHideActionMessages(true);
            kmUser.setSkipDeletedGroups(true);
        }
        new KmUserLoginTask(kmUser, false, handler, context, prechatReceiver).execute();
    }

    public static void loginAsVisitor(Context context, KMLoginHandler handler) {
        login(context, getVisitor(), handler);
    }

    public static void logout(Context context, final KMLogoutHandler logoutHandler) {
        KMLogoutHandler handler = new KMLogoutHandler() {
            @Override
            public void onSuccess(Context context) {
                KmDatabaseHelper.getInstance(context).deleteDatabase();
                Kommunicate.setDeviceToken(context, null);
                logoutHandler.onSuccess(context);
            }

            @Override
            public void onFailure(Exception exception) {
                logoutHandler.onFailure(exception);
            }
        };

        Applozic.logoutUser(context, handler);
    }

    public static void setDeviceToken(Context context, String deviceToken) {
        Applozic.getInstance(context).setDeviceRegistrationId(deviceToken);
    }

    public static String getDeviceToken(Context context) {
        return Applozic.getInstance(context).getDeviceRegistrationId();
    }

    public static void openConversation(Context context) {
        openConversation(context, null, null);
    }

    public static void openConversation(Context context, Integer conversationId, KmCallback callback) {
        try {
            KmConversationHelper.openConversation(context, true, conversationId, callback);
        } catch (KmException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    public static void openConversation(Context context, KmCallback callback) {
        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.CONVERSATION_ACTIVITY_NAME));
            context.startActivity(intent);
            if (callback != null) {
                callback.onSuccess("Successfully launched chat list");
            }
        } catch (ClassNotFoundException e) {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    public static void launchPrechatWithResult(Context context, final KmPrechatCallback callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException("This method needs Activity context");
        }

        ResultReceiver resultReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (KmConstants.PRECHAT_RESULT_CODE == resultCode) {
                    KMUser user = (KMUser) GsonUtils.getObjectFromJson(resultData.getString(KmConstants.KM_USER_DATA), KMUser.class);
                    if (callback != null) {
                        callback.onReceive(user, (ResultReceiver) resultData.getParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER));
                    }
                }
            }
        };

        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.PRECHAT_ACTIVITY_NAME));
            intent.putExtra(KmConstants.PRECHAT_RESULT_RECEIVER, resultReceiver);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new KmException(e.getMessage());
        }
    }

    @Deprecated
    public static void launchSingleChat(final Context context, final String groupName, KMUser kmUser, boolean withPreChat, final boolean isUnique, final List<String> agents, final List<String> bots, final String clientConversationId, final KmCallback callback) {
        if (callback == null) {
            return;
        }
        if (!(context instanceof Activity)) {
            callback.onFailure("This method needs Activity context");
        }

        final KMStartChatHandler startChatHandler = new KMStartChatHandler() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                callback.onSuccess(channel);
                openParticularConversation(context, channel.getKey());
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                callback.onFailure(channelFeedApiResponse);
            }
        };

        final KmChatBuilder chatBuilder = new KmChatBuilder(context);
        chatBuilder.setChatName(groupName)
                .setKmUser(kmUser)
                .setWithPreChat(withPreChat)
                .setSingleChat(isUnique)
                .setAgentIds(agents)
                .setBotIds(bots)
                .setClientConversationId(clientConversationId);

        if (isLoggedIn(context)) {
            try {
                startConversation(chatBuilder, startChatHandler);
            } catch (KmException e) {
                callback.onFailure(e);
            }
        } else {
            final KMLoginHandler loginHandler = new KMLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    try {
                        startConversation(chatBuilder, startChatHandler);
                    } catch (KmException e) {
                        e.printStackTrace();
                        callback.onFailure(e);
                    }
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    callback.onFailure(registrationResponse);
                }
            };

            if (withPreChat) {
                try {
                    launchPrechatWithResult(context, new KmPrechatCallback() {
                        @Override
                        public void onReceive(KMUser user, ResultReceiver finishActivityReceiver) {
                            login(context, user, loginHandler, finishActivityReceiver);
                        }
                    });
                } catch (KmException e) {
                    callback.onFailure(e);
                }
            } else {
                login(context, kmUser, loginHandler);
            }
        }
    }

    public static void setNotificationSoundPath(Context context, String path) {
        Applozic.getInstance(context).setCustomNotificationSound(path);
    }

    @Deprecated
    public static void openParticularConversation(Context context, Integer groupId) {
        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.CONVERSATION_ACTIVITY_NAME));
            intent.putExtra(KmConstants.GROUP_ID, groupId);
            intent.putExtra(KmConstants.TAKE_ORDER, true); //Skip chat list for showing on back press
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static void startConversation(final KmChatBuilder chatBuilder, final KMStartChatHandler handler) throws KmException {
        if (chatBuilder == null) {
            throw new KmException("KmChatBuilder cannot be null");
        }

        if (chatBuilder.getAgentIds() == null || chatBuilder.getAgentIds().isEmpty()) {
            KmCallback callback = new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    KmAgentModel.KmResponse agent = (KmAgentModel.KmResponse) message;
                    if (agent != null) {
                        List<String> agents = new ArrayList<>();
                        agents.add(agent.getAgentId());
                        chatBuilder.setAgentIds(agents);
                        try {
                            final String clientChannelKey = !TextUtils.isEmpty(chatBuilder.getClientConversationId()) ? chatBuilder.getClientConversationId() : (chatBuilder.isSingleChat() ? getClientGroupId(MobiComUserPreference.getInstance(chatBuilder.getContext()).getUserId(), agents, chatBuilder.getBotIds()) : null);
                            if (!TextUtils.isEmpty(clientChannelKey)) {
                                chatBuilder.setClientConversationId(clientChannelKey);
                                startOrGetConversation(chatBuilder, handler);
                            } else {
                                createConversation(chatBuilder, handler);
                            }
                        } catch (KmException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Object error) {
                    if (handler != null) {
                        handler.onFailure(null, chatBuilder.getContext());
                    }
                }
            };

            new KmGetAgentListTask(chatBuilder.getContext(), MobiComKitClientService.getApplicationKey(chatBuilder.getContext()), callback).execute();
        } else {
            final String clientChannelKey = !TextUtils.isEmpty(chatBuilder.getClientConversationId()) ? chatBuilder.getClientConversationId() : (chatBuilder.isSingleChat() ? getClientGroupId(MobiComUserPreference.getInstance(chatBuilder.getContext()).getUserId(), chatBuilder.getAgentIds(), chatBuilder.getBotIds()) : null);
            if (!TextUtils.isEmpty(clientChannelKey)) {
                startOrGetConversation(chatBuilder, handler);
            } else {
                createConversation(chatBuilder, handler);
            }
        }
    }

    @Deprecated
    private static void createConversation(KmChatBuilder chatBuilder, KMStartChatHandler handler) throws KmException {
        List<KMGroupInfo.GroupUser> users = new ArrayList<>();

        KMGroupInfo channelInfo = new KMGroupInfo(TextUtils.isEmpty(chatBuilder.getChatName()) ? Utils.getString(chatBuilder.getContext(), R.string.km_default_support_group_name) : chatBuilder.getChatName(), new ArrayList<String>());

        if (chatBuilder.getAgentIds() == null || chatBuilder.getAgentIds().isEmpty()) {
            throw new KmException("Agent Id list cannot be null or empty");
        }
        for (String agentId : chatBuilder.getAgentIds()) {
            users.add(channelInfo.new GroupUser().setUserId(agentId).setGroupRole(1));
        }

        users.add(channelInfo.new GroupUser().setUserId(KM_BOT).setGroupRole(2));
        users.add(channelInfo.new GroupUser().setUserId(MobiComUserPreference.getInstance(chatBuilder.getContext()).getUserId()).setGroupRole(3));

        if (chatBuilder.getBotIds() != null) {
            for (String botId : chatBuilder.getBotIds()) {
                if (botId != null && !KM_BOT.equals(botId)) {
                    users.add(channelInfo.new GroupUser().setUserId(botId).setGroupRole(2));
                }
            }
        }

        channelInfo.setType(10);
        channelInfo.setUsers(users);

        if (!chatBuilder.getAgentIds().isEmpty()) {
            channelInfo.setAdmin(chatBuilder.getAgentIds().get(0));
        }

        if (!TextUtils.isEmpty(chatBuilder.getClientConversationId())) {
            channelInfo.setClientGroupId(chatBuilder.getClientConversationId());
        } else if (chatBuilder.isSingleChat()) {
            channelInfo.setClientGroupId(getClientGroupId(MobiComUserPreference.getInstance(chatBuilder.getContext()).getUserId(), chatBuilder.getAgentIds(), chatBuilder.getBotIds()));
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("CREATE_GROUP_MESSAGE", "");
        metadata.put("REMOVE_MEMBER_MESSAGE", "");
        metadata.put("ADD_MEMBER_MESSAGE", "");
        metadata.put("JOIN_MEMBER_MESSAGE", "");
        metadata.put("GROUP_NAME_CHANGE_MESSAGE", "");
        metadata.put("GROUP_ICON_CHANGE_MESSAGE", "");
        metadata.put("GROUP_LEFT_MESSAGE", "");
        metadata.put("DELETED_GROUP_MESSAGE", "");
        metadata.put("GROUP_USER_ROLE_UPDATED_MESSAGE", "");
        metadata.put("GROUP_META_DATA_UPDATED_MESSAGE", "");
        metadata.put("HIDE", "true");

        if (!TextUtils.isEmpty(chatBuilder.getConversationAssignee())) {
            metadata.put(CONVERSATION_ASSIGNEE, chatBuilder.getConversationAssignee());
            metadata.put(SKIP_ROUTING, "true");
        }

        if (chatBuilder.isSkipRouting()) {
            metadata.put(SKIP_ROUTING, String.valueOf(chatBuilder.isSkipRouting()));
        }

        if (!TextUtils.isEmpty(ApplozicClient.getInstance(chatBuilder.getContext()).getMessageMetaData())) {
            Map<String, String> defaultMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(ApplozicClient.getInstance(chatBuilder.getContext()).getMessageMetaData(), Map.class);
            if (defaultMetadata != null) {
                metadata.putAll(defaultMetadata);
            }
        }

        channelInfo.setMetadata(metadata);

        Utils.printLog(chatBuilder.getContext(), TAG, "ChannelInfo : " + GsonUtils.getJsonFromObject(channelInfo, ChannelInfo.class));

        if (handler == null) {
            handler = new KMStartChatHandler() {
                @Override
                public void onSuccess(Channel channel, Context context) {

                }

                @Override
                public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {

                }
            };
        }

        new KmConversationCreateTask(chatBuilder.getContext(), channelInfo, handler).execute();
    }

    public static void getAgents(Context context, int startIndex, int pageSize, KMGetContactsHandler handler) {
        List<String> roleName = new ArrayList<>();
        roleName.add(KMUser.RoleName.APPLICATION_ADMIN.getValue());
        roleName.add(KMUser.RoleName.APPLICATION_WEB_ADMIN.getValue());

        new GetUserListAsyncTask(context, roleName, startIndex, pageSize, handler).execute();
    }

    public static void getFaqs(Context context, String type, String helpDocsKey, String data, KmFaqTaskListener listener) {
        KMFaqTask task = new KMFaqTask(context, helpDocsKey, data, listener);
        if ("getArticles".equals(type)) {
            task.forArticleRequest();
        } else if ("getSelectedArticles".equals(type)) {
            task.forSelectedArticles();
        } else if ("getAnswers".equals(type)) {
            task.forAnswerRequest();
        } else if ("getDashboardFaq".equals(type)) {
            task.forDashboardFaq();
        }
        task.execute();
    }

    public static void getHelpDocsKey(Context context, String type, KmFaqTaskListener listener) {
        new KMHelpDocsKeyTask(context, type, listener).execute();
    }

    public static boolean isLoggedIn(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    public static void registerForPushNotification(Context context, String token, KmPushNotificationHandler listener) {
        if (TextUtils.isEmpty(token)) {
            listener.onFailure(null, new KmException("Push token cannot be null or empty"));
            return;
        }
        setDeviceToken(context, token);

        if (!token.equals(getDeviceToken(context))) {
            new PushNotificationTask(context, token, listener).execute();
        }
    }

    public static void registerForPushNotification(Context context, KmPushNotificationHandler listener) {
        registerForPushNotification(context, Kommunicate.getDeviceToken(context), listener);
    }

    public static boolean isKmNotification(Context context, Map<String, String> data) {
        if (MobiComPushReceiver.isMobiComPushNotification(data)) {
            MobiComPushReceiver.processMessageAsync(context, data);
            return true;
        }
        return false;
    }

    @Deprecated
    private static void startOrGetConversation(final KmChatBuilder chatBuilder, final KMStartChatHandler handler) throws KmException {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                if (handler != null) {
                    handler.onSuccess(channel, context);
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                try {
                    createConversation(chatBuilder, handler);
                } catch (KmException e1) {
                    handler.onFailure(null, context);
                }
            }
        };

        new KmConversationInfoTask(chatBuilder.getContext(), chatBuilder.getClientConversationId(), conversationInfoCallback).execute();
    }

    private static String getClientGroupId(String userId, List<String> agentIds, List<String> botIds) throws KmException {

        if (agentIds == null || agentIds.isEmpty()) {
            throw new KmException("Please add at-least one Agent");
        }

        if (TextUtils.isEmpty(userId)) {
            throw new KmException("UserId cannot be null");
        }

        Collections.sort(agentIds);

        List<String> tempList = new ArrayList<>(agentIds);
        tempList.add(userId);

        if (botIds != null && !botIds.isEmpty()) {
            if (botIds.contains(KM_BOT)) {
                botIds.remove(KM_BOT);
            }
            Collections.sort(botIds);
            tempList.addAll(botIds);
        }

        StringBuilder sb = new StringBuilder();

        Iterator<String> iterator = tempList.iterator();

        while (iterator.hasNext()) {
            String temp = iterator.next();
            if (temp == null) {
                continue;
            }
            sb.append(temp);

            if (!temp.equals(tempList.get(tempList.size() - 1))) {
                sb.append("_");
            }
        }

        if (sb.toString().length() > 255) {
            throw new KmException("Please reduce the number of agents or bots");
        }

        return sb.toString();
    }

    public static KMUser getVisitor() {
        KMUser user = new KMUser();
        user.setUserId(generateUserId());
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());
        return user;
    }

    private static String generateUserId() {
        StringBuilder text = new StringBuilder("");
        SecureRandom random = new SecureRandom();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < 32; i++) {
            text.append(possible.charAt(random.nextInt(possible.length())));
        }
        return text.toString();
    }

    /**
     * will update the metadata object with the KM_CHAT_CONTEXT field
     *
     * @param context         the context
     * @param messageMetadata the map data to update the KM_CHAT_CONTEXT field with
     */
    @Deprecated
    public static void updateChatContext(Context context, Map<String, String> messageMetadata) {
        //converting the messageMetadata parameter passed to function (keyed by KM_CHAT_CONTEXT), to json string
        String messageMetaDataString = GsonUtils.getJsonFromObject(messageMetadata, Map.class);
        if (TextUtils.isEmpty(messageMetaDataString)) {
            return;
        }

        //getting the message metadata already in the applozic preferences
        String existingMetaDataString = ApplozicClient.getInstance(context).getMessageMetaData();
        Map<String, String> existingMetadata;

        if (TextUtils.isEmpty(existingMetaDataString)) { //case 1: no existing metadata
            existingMetadata = new HashMap<>();
        } else { //case 2: metadata already exists
            existingMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetaDataString, Map.class);

            if (existingMetadata.containsKey(KM_CHAT_CONTEXT)) { //case 2a: km_chat-context already exists
                Map<String, String> existingKmChatContext = (Map<String, String>) GsonUtils.getObjectFromJson(existingMetadata.get(KM_CHAT_CONTEXT), Map.class);

                for (Map.Entry<String, String> data : messageMetadata.entrySet()) {
                    existingKmChatContext.put(data.getKey(), data.getValue());
                }

                //update messageMetadataString
                messageMetaDataString = GsonUtils.getJsonFromObject(existingKmChatContext, Map.class);
            }
        }

        existingMetadata.put(KM_CHAT_CONTEXT, messageMetaDataString);
        ApplozicClient.getInstance(context).setMessageMetaData(existingMetadata);
    }

    public static void loadAwayMessage(Context context, Integer groupId, KmAwayMessageHandler handler) {
        new KmAwayMessageTask(context, groupId, handler).execute();
    }
}
