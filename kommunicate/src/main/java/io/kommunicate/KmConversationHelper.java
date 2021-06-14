package io.kommunicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.ApplozicConversation;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.kommunicate.async.KmConversationCreateTask;
import io.kommunicate.async.KmConversationInfoTask;
import io.kommunicate.async.KmGetAgentListTask;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmGetConversationInfoCallback;
import io.kommunicate.callbacks.KmPrechatCallback;
import io.kommunicate.callbacks.KmStartConversationHandler;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;

public class KmConversationHelper {

    public static final String CONVERSATION_ASSIGNEE = "CONVERSATION_ASSIGNEE";
    public static final String CONVERSATION_TITLE = "CONVERSATION_TITLE";
    public static final String SKIP_ROUTING = "SKIP_ROUTING";
    public static final String KM_ORIGINAL_TITLE = "KM_ORIGINAL_TITLE";
    public static final String KM_CONVERSATION_TITLE = "KM_CONVERSATION_TITLE";
    public static final String KM_BOT = "bot";
    public static final String CONVERSATION_STATUS = "CONVERSATION_STATUS";
    public static final String KM_TEAM_ID = "KM_TEAM_ID";
    private static final String TAG = "KmConversationHelper";

    public static void openConversation(final Context context, final boolean skipConversationList, final Integer conversationId, final KmCallback callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException(Utils.getString(context, R.string.km_method_needs_activity_context));
        }

        if (conversationId == null) {
            ApplozicConversation.getLatestMessageList(context, false, new MessageListHandler() {
                @Override
                public void onResult(List<Message> messageList, ApplozicException e) {
                    if (messageList != null) {
                        if (messageList.size() == 1) {
                            Message message = messageList.get(0);
                            if (message.getGroupId() != null && message.getGroupId() != 0) {
                                KmGetConversationInfoCallback memberListener = new KmGetConversationInfoCallback() {
                                    @Override
                                    public void onSuccess(Channel channel, Context context) {
                                        if (channel != null) {
                                            openParticularConversation(context, skipConversationList, channel.getKey(), null, callback);
                                        } else {
                                            Kommunicate.openConversation(context, callback);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e, Context context) {
                                        Kommunicate.openConversation(context, callback);
                                    }
                                };
                                new KmConversationInfoTask(context, message.getGroupId(), memberListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                Kommunicate.openConversation(context, callback);
                            }
                        } else {
                            Kommunicate.openConversation(context, callback);
                        }
                    } else {
                        Kommunicate.openConversation(context, callback);
                    }
                }
            });
        } else {
            openParticularConversation(context, skipConversationList, conversationId, null, callback);
        }
    }

    private static void openParticularConversation(Context context, boolean skipConversationList, Integer conversationId, String preFilledMessage, KmCallback callback) {
        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.CONVERSATION_ACTIVITY_NAME));
            intent.putExtra(KmConstants.GROUP_ID, conversationId);
            intent.putExtra(KmConstants.TAKE_ORDER, skipConversationList);
            if (!TextUtils.isEmpty(preFilledMessage)) {
                intent.putExtra(KmConstants.KM_PREFILLED_MESSAGE, preFilledMessage);
            }
            context.startActivity(intent);
            if (callback != null) {
                callback.onSuccess(conversationId);
            }
        } catch (ClassNotFoundException e) {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    @Deprecated
    public static void launchChat(final KmChatBuilder launchChat, final KmCallback callback) {
        if (launchChat == null) {
            if (callback != null) {
                callback.onFailure(Utils.getString(null, R.string.km_chat_builder_cannot_be_null));
            }
            return;
        }

        if (launchChat.getContext() == null) {
            if (callback != null) {
                callback.onFailure(Utils.getString(launchChat.getContext(), R.string.km_context_cannot_be_null));
            }
            return;
        }

        if (Kommunicate.isLoggedIn(launchChat.getContext())) {
            try {
                Kommunicate.startConversation(launchChat,
                        getStartChatHandler(launchChat.isSkipChatList(), true, null, callback));
            } catch (KmException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        } else {
            if (!TextUtils.isEmpty(launchChat.getApplicationId())) {
                Kommunicate.init(launchChat.getContext(), launchChat.getApplicationId());
            } else {
                if (TextUtils.isEmpty(Applozic.getInstance(launchChat.getContext()).getApplicationKey())) {
                    if (callback != null) {
                        callback.onFailure(Utils.getString(launchChat.getContext(), R.string.km_app_id_cannot_be_null));
                    }
                }
            }
            if (launchChat.isWithPreChat()) {
                try {
                    Kommunicate.launchPrechatWithResult(launchChat.getContext(), new KmPrechatCallback<KMUser>() {
                        @Override
                        public void onReceive(KMUser user, Context context, ResultReceiver finishActivityReceiver) {
                            Kommunicate.login(launchChat.getContext(), user, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), true, finishActivityReceiver, callback), callback));
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                } catch (KmException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            } else {
                KMUser kmUser;

                if (launchChat.getKmUser() != null) {
                    kmUser = launchChat.getKmUser();
                } else if (!TextUtils.isEmpty(launchChat.getUserId())) {
                    kmUser = getKmUser(launchChat);
                } else {
                    kmUser = Kommunicate.getVisitor();
                }

                Kommunicate.login(launchChat.getContext(), kmUser, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), true, null, callback), callback));
            }
        }
    }

    @Deprecated
    public static void createChat(final KmChatBuilder launchChat, final KmCallback callback) {
        if (launchChat == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_chat_builder_cannot_be_null));
                callback.onFailure(Utils.getString(null, R.string.km_chat_builder_cannot_be_null));
            }
            return;
        }

        if (launchChat.getContext() == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_context_cannot_be_null));
                callback.onFailure(Utils.getString(launchChat.getContext(), R.string.km_context_cannot_be_null));
            }
            return;
        }

        if (Kommunicate.isLoggedIn(launchChat.getContext())) {
            try {
                Kommunicate.startConversation(launchChat,
                        getStartChatHandler(launchChat.isSkipChatList(), false, null, callback));
            } catch (KmException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        } else {
            if (!TextUtils.isEmpty(launchChat.getApplicationId())) {
                Kommunicate.init(launchChat.getContext(), launchChat.getApplicationId());
            } else {
                if (TextUtils.isEmpty(Applozic.getInstance(launchChat.getContext()).getApplicationKey())) {
                    if (callback != null) {
                        Utils.printLog(null, TAG, Utils.getString(null, R.string.km_app_id_cannot_be_null));
                        callback.onFailure(Utils.getString(launchChat.getContext(), R.string.km_app_id_cannot_be_null));
                    }
                }
            }
            if (launchChat.isWithPreChat()) {
                try {
                    Kommunicate.launchPrechatWithResult(launchChat.getContext(), new KmPrechatCallback<KMUser>() {
                        @Override
                        public void onReceive(KMUser user, Context context, ResultReceiver finishActivityReceiver) {
                            Kommunicate.login(launchChat.getContext(), user, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), false, finishActivityReceiver, callback), callback));
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                } catch (KmException e) {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            } else {
                KMUser kmUser;

                if (launchChat.getKmUser() != null) {
                    kmUser = launchChat.getKmUser();
                } else if (!TextUtils.isEmpty(launchChat.getUserId())) {
                    kmUser = getKmUser(launchChat);
                } else {
                    kmUser = Kommunicate.getVisitor();
                }

                Kommunicate.login(launchChat.getContext(), kmUser, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), false, null, callback), callback));
            }
        }
    }

    @Deprecated
    private static KMUser getKmUser(KmChatBuilder launchChat) {
        KMUser user = new KMUser();
        user.setUserId(launchChat.getUserId());

        if (!TextUtils.isEmpty(launchChat.getPassword())) {
            user.setPassword(launchChat.getPassword());
        }

        if (!TextUtils.isEmpty(launchChat.getImageUrl())) {
            user.setImageLink(launchChat.getImageUrl());
        }

        if (!TextUtils.isEmpty(launchChat.getDisplayName())) {
            user.setDisplayName(launchChat.getDisplayName());
        }
        return user;
    }

    @Deprecated
    private static KMStartChatHandler getStartChatHandler(final boolean isSkipChatList, final boolean launchChat, final ResultReceiver resultReceiver, final KmCallback callback) {
        return new KMStartChatHandler() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                try {
                    if (resultReceiver != null) {
                        resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                    }
                    if (isSkipChatList) {
                        ApplozicClient.getInstance(context).hideChatListOnNotification();
                    }
                    if (callback != null) {
                        if (launchChat) {
                            openConversation(context, isSkipChatList, channel.getKey(), callback);
                        } else {
                            callback.onSuccess(channel.getKey());
                        }
                    }
                } catch (KmException e) {
                    e.printStackTrace();
                    if (resultReceiver != null) {
                        resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                    }
                    if (callback != null) {
                        e.getMessage();
                    }
                }
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                if (resultReceiver != null) {
                    resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                }
                if (callback != null) {
                    callback.onFailure(channelFeedApiResponse);
                }
                Utils.printLog(context, TAG, "Failed to start chat : " + channelFeedApiResponse);
            }
        };
    }

    @Deprecated
    private static KMLoginHandler getLoginHandler(final KmChatBuilder launchChat, final KMStartChatHandler startChatHandler, final KmCallback callback) {
        return new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {

                String deviceToken = launchChat.getDeviceToken() != null ? launchChat.getDeviceToken() : Kommunicate.getDeviceToken(context);
                if (!TextUtils.isEmpty(deviceToken)) {
                    Kommunicate.registerForPushNotification(context, deviceToken, null);
                }

                if (launchChat.getMetadata() != null) {
                    ApplozicClient.getInstance(context).setMessageMetaData(launchChat.getMetadata());
                }

                try {
                    Kommunicate.startConversation(launchChat, startChatHandler);
                } catch (KmException e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                Utils.printLog(null, TAG, "Failed to login : " + (registrationResponse != null ? registrationResponse : exception));
                callback.onFailure(registrationResponse);
            }
        };
    }

    //meant to be used from the conversation screen start new conversation button
    public static void launchConversationIfLoggedIn(Context context, KmCallback callback) {
        if (Kommunicate.isLoggedIn(context)) {
            KmConversationBuilder conversationBuilder = new KmConversationBuilder(context);
            try {
                startConversation(true, conversationBuilder,
                        getStartConversationHandler(conversationBuilder.isSkipConversationList(), true, null, null, callback));
            } catch (KmException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        }
    }

    public static void createOrLaunchConversation(final KmConversationBuilder conversationBuilder, final boolean launchConversation, final KmCallback callback) {
        if (conversationBuilder == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_conversation_builder_cannot_be_null));
                callback.onFailure(Utils.getString(null, R.string.km_conversation_builder_cannot_be_null));
            }
            return;
        }

        if (conversationBuilder.getContext() == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_context_cannot_be_null));
                callback.onFailure(Utils.getString(conversationBuilder.getContext(), R.string.km_context_cannot_be_null));
            }
            return;
        }

        if (Kommunicate.isLoggedIn(conversationBuilder.getContext())) {
            try {
                startConversation(false, conversationBuilder,
                        getStartConversationHandler(conversationBuilder.isSkipConversationList(), launchConversation, conversationBuilder.getPreFilledMessage(), null, callback));
            } catch (KmException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        } else {
            if (!TextUtils.isEmpty(conversationBuilder.getAppId())) {
                Kommunicate.init(conversationBuilder.getContext(), conversationBuilder.getAppId());
            } else {
                if (TextUtils.isEmpty(Applozic.getInstance(conversationBuilder.getContext()).getApplicationKey())) {
                    if (callback != null) {
                        Utils.printLog(null, TAG, Utils.getString(null, R.string.km_app_id_cannot_be_null));
                        callback.onFailure(Utils.getString(conversationBuilder.getContext(), R.string.km_app_id_cannot_be_null));
                    }
                }
            }
            if (conversationBuilder.isWithPreChat()) {
                try {
                    Kommunicate.launchPrechatWithResult(conversationBuilder.getContext(), new KmPrechatCallback<KMUser>() {
                        @Override
                        public void onReceive(KMUser user, Context context, ResultReceiver finishActivityReceiver) {
                            Kommunicate.login(conversationBuilder.getContext(), user, getLoginHandler(conversationBuilder, getStartConversationHandler(conversationBuilder.isSkipConversationList(), launchConversation, conversationBuilder.getPreFilledMessage(), finishActivityReceiver, callback), callback));
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                } catch (KmException e) {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            } else {
                KMUser kmUser;

                if (conversationBuilder.getKmUser() != null) {
                    kmUser = conversationBuilder.getKmUser();
                } else {
                    kmUser = Kommunicate.getVisitor();
                }

                Kommunicate.login(conversationBuilder.getContext(), kmUser, getLoginHandler(conversationBuilder, getStartConversationHandler(conversationBuilder.isSkipConversationList(), launchConversation, conversationBuilder.getPreFilledMessage(), null, callback), callback));
            }
        }
    }

    public static void launchAndCreateIfEmpty(final KmConversationBuilder conversationBuilder, final KmCallback callback) {
        if (conversationBuilder == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_conversation_builder_cannot_be_null));
                callback.onFailure(Utils.getString(null, R.string.km_conversation_builder_cannot_be_null));
            }
            return;
        }

        if (conversationBuilder.getContext() == null) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_context_cannot_be_null));
                callback.onFailure(Utils.getString(conversationBuilder.getContext(), R.string.km_context_cannot_be_null));
            }
            return;
        }

        if (!(conversationBuilder.getContext() instanceof Activity)) {
            if (callback != null) {
                Utils.printLog(null, TAG, Utils.getString(null, R.string.km_method_needs_activity_context));
                callback.onFailure(Utils.getString(conversationBuilder.getContext(), R.string.km_method_needs_activity_context));
            }
            return;
        }

        ApplozicConversation.getLatestMessageList(conversationBuilder.getContext(), false, new MessageListHandler() {
            @Override
            public void onResult(List<Message> messageList, ApplozicException e) {
                if (e == null) {
                    if (messageList.isEmpty()) {
                        conversationBuilder.setSkipConversationList(false);
                        conversationBuilder.launchConversation(callback);
                    } else if (messageList.size() == 1) {
                        openParticularConversation(conversationBuilder.getContext(), false, messageList.get(0).getGroupId(), conversationBuilder.getPreFilledMessage(), callback);
                    } else {
                        Kommunicate.openConversation(conversationBuilder.getContext(), callback);
                    }
                }
            }
        });
    }

    private static KmStartConversationHandler getStartConversationHandler(final boolean isSkipConversationList, final boolean launchConversation, final String preFilledMessage, final ResultReceiver resultReceiver, final KmCallback callback) {
        return new KmStartConversationHandler() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                try {
                    if (resultReceiver != null) {
                        resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                    }
                    if (isSkipConversationList) {
                        ApplozicClient.getInstance(context).hideChatListOnNotification();
                    }
                    if (callback != null) {
                        if (launchConversation) {
                            openParticularConversation(context, isSkipConversationList, channel.getKey(), preFilledMessage, callback);
                        } else {
                            callback.onSuccess(channel.getKey());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (resultReceiver != null) {
                        resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                    }
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                if (resultReceiver != null) {
                    resultReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                }
                if (callback != null) {
                    callback.onFailure(channelFeedApiResponse);
                }
                Utils.printLog(null, TAG, "Error while creating conversation : " + channelFeedApiResponse);
            }
        };
    }

    private static KMLoginHandler getLoginHandler(final KmConversationBuilder conversationBuilder, final KmStartConversationHandler startConversationHandler, final KmCallback callback) {
        return new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {

                String deviceToken = conversationBuilder.getFcmDeviceToken() != null ? conversationBuilder.getFcmDeviceToken() : Kommunicate.getDeviceToken(context);
                if (!TextUtils.isEmpty(deviceToken)) {
                    Kommunicate.registerForPushNotification(context, deviceToken, null);
                }

                if (conversationBuilder.getMessageMetadata() != null) {
                    ApplozicClient.getInstance(context).setMessageMetaData(conversationBuilder.getMessageMetadata());
                }

                try {
                    startConversation(false, conversationBuilder, startConversationHandler);
                } catch (KmException e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                callback.onFailure(registrationResponse);
                Utils.printLog(null, TAG, "Error while logging in user : " + (registrationResponse != null ? registrationResponse : exception));
            }
        };
    }

    private static void startOrGetConversation(final KmConversationBuilder conversationBuilder, final KmStartConversationHandler callback) throws KmException {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(final Channel channel, Context context) {
                if (callback != null) {
                    callback.onSuccess(channel, context);
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                try {
                    createConversation(conversationBuilder, callback);
                } catch (KmException e1) {
                    callback.onFailure(null, context);
                }
            }
        };

        new KmConversationInfoTask(conversationBuilder.getContext(), conversationBuilder.getClientConversationId(), conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static void createConversation(KmConversationBuilder conversationBuilder, KmStartConversationHandler handler) throws KmException {
        List<KMGroupInfo.GroupUser> users = new ArrayList<>();
        String loginUserId = MobiComUserPreference.getInstance(conversationBuilder.getContext()).getUserId();

        KMGroupInfo channelInfo = new KMGroupInfo(Utils.getString(conversationBuilder.getContext(), R.string.km_default_support_group_name), new ArrayList<String>());

        if (conversationBuilder.getAgentIds() == null || conversationBuilder.getAgentIds().isEmpty()) {
            throw new KmException(Utils.getString(conversationBuilder.getContext(), R.string.km_agent_list_empty_error));
        }
        for (String agentId : conversationBuilder.getAgentIds()) {
            users.add(channelInfo.new GroupUser().setUserId(agentId).setGroupRole(1));
        }

        users.add(channelInfo.new GroupUser().setUserId(KM_BOT).setGroupRole(2));

        if (conversationBuilder.getUserIds() == null || conversationBuilder.getUserIds().isEmpty()) {
            List<String> userIds = new ArrayList<>();
            userIds.add(loginUserId);
            conversationBuilder.setUserIds(userIds);
        } else if (!conversationBuilder.getUserIds().contains(loginUserId)) {
            conversationBuilder.getUserIds().add(loginUserId);
        }

        for (String userId : conversationBuilder.getUserIds()) {
            users.add(channelInfo.new GroupUser().setUserId(userId).setGroupRole(3));
        }

        if (conversationBuilder.getBotIds() != null) {
            for (String botId : conversationBuilder.getBotIds()) {
                if (botId != null && !KM_BOT.equals(botId)) {
                    users.add(channelInfo.new GroupUser().setUserId(botId).setGroupRole(2));
                }
            }
        }

        channelInfo.setType(10);
        channelInfo.setUsers(users);

        if (!conversationBuilder.getAgentIds().isEmpty()) {
            channelInfo.setAdmin(conversationBuilder.getAgentIds().get(0));
        }

        if (!TextUtils.isEmpty(conversationBuilder.getClientConversationId())) {
            channelInfo.setClientGroupId(conversationBuilder.getClientConversationId());
        } else if (conversationBuilder.isSingleConversation()) {
            channelInfo.setClientGroupId(getClientGroupId(conversationBuilder.getUserIds(), conversationBuilder.getAgentIds(), conversationBuilder.getBotIds(), conversationBuilder.getContext()));
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

        if (!TextUtils.isEmpty(conversationBuilder.getConversationAssignee())) {
            metadata.put(CONVERSATION_ASSIGNEE, conversationBuilder.getConversationAssignee());
            metadata.put(SKIP_ROUTING, "true");
        }

        if (conversationBuilder.isSkipConversationRoutingRules()) {
            metadata.put(SKIP_ROUTING, String.valueOf(conversationBuilder.isSkipConversationRoutingRules()));
        }

        if (!TextUtils.isEmpty(conversationBuilder.getConversationTitle())) {
            channelInfo.setGroupName(conversationBuilder.getConversationTitle());
            metadata.put(KM_CONVERSATION_TITLE, conversationBuilder.getConversationTitle());
            metadata.put(KmConversationHelper.KM_ORIGINAL_TITLE, "true");
        }

        if (!TextUtils.isEmpty(conversationBuilder.getTeamId())) {
            metadata.put(KM_TEAM_ID, conversationBuilder.getTeamId());
        }

        if (conversationBuilder.isUseOriginalTitle()) {
            metadata.put(KmConversationHelper.KM_ORIGINAL_TITLE, String.valueOf(conversationBuilder.isUseOriginalTitle()));
        }

        if (conversationBuilder.getConversationInfo() != null) {
            metadata.put(KmSettings.KM_CONVERSATION_METADATA, GsonUtils.getJsonFromObject(conversationBuilder.getConversationInfo(), Map.class));
        }

        if (!TextUtils.isEmpty(ApplozicClient.getInstance(conversationBuilder.getContext()).getMessageMetaData())) {
            Map<String, String> defaultMetadata = (Map<String, String>) GsonUtils.getObjectFromJson(ApplozicClient.getInstance(conversationBuilder.getContext()).getMessageMetaData(), Map.class);
            if (defaultMetadata != null) {
                metadata.putAll(defaultMetadata);
            }
        }

        channelInfo.setMetadata(metadata);

        Utils.printLog(conversationBuilder.getContext(), TAG, "ChannelInfo : " + GsonUtils.getJsonFromObject(channelInfo, ChannelInfo.class));

        if (handler == null) {
            handler = new KmStartConversationHandler() {
                @Override
                public void onSuccess(Channel channel, Context context) {

                }

                @Override
                public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {

                }
            };
        }

        new KmConversationCreateTask(conversationBuilder.getContext(), channelInfo, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static KmCallback getCallbackWithAppSettingsToCreateConversation(final boolean useSingleThreadedSettingFromServer, final KmConversationBuilder conversationBuilder, final KmStartConversationHandler handler) {
        return new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                KmAppSettingModel.KmResponse kmAppSettings = (KmAppSettingModel.KmResponse) message;
                if (kmAppSettings != null) {
                    List<String> agents = new ArrayList<>();
                    agents.add(kmAppSettings.getAgentId());
                    conversationBuilder.setAgentIds(agents);
                    if (useSingleThreadedSettingFromServer) {
                        conversationBuilder.setSingleConversation(kmAppSettings.getChatWidget().isSingleThreaded());
                    }
                    try {
                        final String clientChannelKey = !TextUtils.isEmpty(conversationBuilder.getClientConversationId()) ? conversationBuilder.getClientConversationId() : (conversationBuilder.isSingleConversation() ? getClientGroupId(conversationBuilder.getUserIds(), agents, conversationBuilder.getBotIds(), conversationBuilder.getContext()) : null);
                        if (!TextUtils.isEmpty(clientChannelKey)) {
                            conversationBuilder.setClientConversationId(clientChannelKey);
                            startOrGetConversation(conversationBuilder, handler);
                        } else {
                            createConversation(conversationBuilder, handler);
                        }
                    } catch (KmException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Object error) {
                if (handler != null) {
                    handler.onFailure(null, conversationBuilder.getContext());
                }
            }
        };
    }

    private static void startConversation(boolean useSingleThreadedSettingFromServer, final KmConversationBuilder conversationBuilder, final KmStartConversationHandler handler) throws KmException {
        if (conversationBuilder == null) {
            throw new KmException(Utils.getString(conversationBuilder.getContext(), R.string.km_conversation_builder_cannot_be_null));
        }
        if (conversationBuilder.getAgentIds() == null || conversationBuilder.getAgentIds().isEmpty()) {
            new KmGetAgentListTask(conversationBuilder.getContext(),
                    MobiComKitClientService.getApplicationKey(conversationBuilder.getContext()),
                    getCallbackWithAppSettingsToCreateConversation(useSingleThreadedSettingFromServer,
                            conversationBuilder,
                            handler)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            final String clientChannelKey = !TextUtils.isEmpty(conversationBuilder.getClientConversationId()) ? conversationBuilder.getClientConversationId() : (conversationBuilder.isSingleConversation() ? getClientGroupId(conversationBuilder.getUserIds(), conversationBuilder.getAgentIds(), conversationBuilder.getBotIds(), conversationBuilder.getContext()) : null);
            if (!TextUtils.isEmpty(clientChannelKey)) {
                conversationBuilder.setClientConversationId(clientChannelKey);
                startOrGetConversation(conversationBuilder, handler);
            } else {
                createConversation(conversationBuilder, handler);
            }
        }
    }

    public static void getConversationById(Context context, String conversationId, final KmCallback callback) {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                if (callback != null) {
                    callback.onSuccess(channel);
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        };

        new KmConversationInfoTask(context, conversationId, conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public static void getConversationMetadata(Context context, String conversationId, final KmCallback callback) {
        KmGetConversationInfoCallback conversationInfoCallback = new KmGetConversationInfoCallback() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                if (callback != null) {
                    if (channel != null && channel.getMetadata() != null) {
                        Map<String, String> channelMetadata = channel.getMetadata();
                        if (channelMetadata != null) {
                            channelMetadata.remove("CREATE_GROUP_MESSAGE");
                            channelMetadata.remove("REMOVE_MEMBER_MESSAGE");
                            channelMetadata.remove("ADD_MEMBER_MESSAGE");
                            channelMetadata.remove("JOIN_MEMBER_MESSAGE");
                            channelMetadata.remove("GROUP_NAME_CHANGE_MESSAGE");
                            channelMetadata.remove("GROUP_ICON_CHANGE_MESSAGE");
                            channelMetadata.remove("GROUP_LEFT_MESSAGE");
                            channelMetadata.remove("DELETED_GROUP_MESSAGE");
                            channelMetadata.remove("GROUP_USER_ROLE_UPDATED_MESSAGE");
                            channelMetadata.remove("GROUP_META_DATA_UPDATED_MESSAGE");
                            channelMetadata.remove("HIDE");
                            channelMetadata.remove(CONVERSATION_ASSIGNEE);
                            channelMetadata.remove(SKIP_ROUTING);
                            channelMetadata.remove(CONVERSATION_TITLE);
                            channelMetadata.remove(KM_CONVERSATION_TITLE);
                            channelMetadata.remove(CONVERSATION_STATUS);

                            callback.onSuccess(channelMetadata);
                        }
                    } else {
                        callback.onSuccess("No Metadata found in conversation");
                    }
                }
            }

            @Override
            public void onFailure(Exception e, Context context) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        };

        new KmConversationInfoTask(context, conversationId, conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static String getClientGroupId(List<String> userIds, List<String> agentIds, List<String> botIds, Context context) throws KmException {

        if (agentIds == null || agentIds.isEmpty()) {
            throw new KmException("Please add at-least one Agent");
        }
        if (userIds == null || userIds.isEmpty()) {
            userIds = new ArrayList<>();
        }

        Collections.sort(agentIds);

        List<String> tempList = new ArrayList<>(agentIds);

        String loginUserId = MobiComUserPreference.getInstance(context).getUserId();

        if (!userIds.contains(loginUserId)) {
            userIds.add(loginUserId);
        }

        Collections.sort(userIds);
        tempList.addAll(userIds);

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
}
