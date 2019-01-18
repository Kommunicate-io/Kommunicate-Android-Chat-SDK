package io.kommunicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;


import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;

import io.kommunicate.activities.LeadCollectionActivity;

import com.applozic.mobicomkit.uiwidgets.async.AlChannelCreateAsyncTask;
import com.applozic.mobicomkit.uiwidgets.async.AlGroupInformationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.kommunicate.activities.KMConversationActivity;
import io.kommunicate.async.GetUserListAsyncTask;
import io.kommunicate.async.KMFaqTask;
import io.kommunicate.async.KMHelpDocsKeyTask;
import io.kommunicate.async.KmGetAgentListTask;
import io.kommunicate.async.KmUserLoginTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.callbacks.KmPrechatCallback;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.models.KmAgentModel;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 23/01/18.
 */

public class Kommunicate {

    private static final String KM_BOT = "bot";
    public static final String START_NEW_CHAT = "startNewChat";
    public static final String LOGOUT_CALL = "logoutCall";
    public static final String PRECHAT_LOGIN_CALL = "prechatLogin";
    private static final String TAG = "KommunicateTag";

    public static void init(Context context, String applicationKey) {
        Applozic.init(context, applicationKey);
    }

    public static void login(Context context, KMUser kmUser, KMLoginHandler handler) {
        new KmUserLoginTask(kmUser, false, handler, context).execute();
    }

    public static void loginAsVisitor(Context context, KMLoginHandler handler) {
        login(context, getVisitor(), handler);
    }

    public static void logout(Context context, KMLogoutHandler logoutHandler) {
        Applozic.logoutUser(context, logoutHandler);
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

    public static void openConversation(Context context, Integer chatId, KmCallback callback) {
        try {
            KmConversationHelper.openConversation(context, true, chatId, callback);
        } catch (KmException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    @Deprecated
    public static void openConversation(Context context, KmCallback callback) {
        Intent intent = new Intent(context, KMConversationActivity.class);
        context.startActivity(intent);
        if (callback != null) {
            callback.onSuccess("Successfully launched chat list");
        }
    }

    @Deprecated
    public static void openConversation(Context context, boolean prechatLeadCollection) {
        Intent intent = new Intent(context, (prechatLeadCollection && !KMUser.isLoggedIn(context)) ? LeadCollectionActivity.class : KMConversationActivity.class);
        context.startActivity(intent);
    }

    public static void launchPrechatWithResult(Context context, final KmPrechatCallback callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException("This method needs Activity context");
        }

        ResultReceiver resultReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (LeadCollectionActivity.PRECHAT_RESULT_CODE == resultCode) {
                    KMUser user = (KMUser) GsonUtils.getObjectFromJson(resultData.getString(LeadCollectionActivity.KM_USER_DATA), KMUser.class);
                    if (callback != null) {
                        callback.onReceive(user);
                    }
                }
            }
        };

        Intent intent = new Intent(context, LeadCollectionActivity.class);
        intent.putExtra(LeadCollectionActivity.PRECHAT_RESULT_RECEIVER, resultReceiver);
        context.startActivity(intent);
    }

    @Deprecated
    public static void launchSingleChat(final Context context, final String groupName, KMUser kmUser, boolean withPreChat, final boolean isUnique, final List<String> agents, final List<String> bots, final KmCallback callback) {
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

        if (isLoggedIn(context)) {
            try {
                startConversation(context, groupName, agents, bots, isUnique, startChatHandler);
            } catch (KmException e) {
                callback.onFailure(e);
            }
        } else {
            final KMLoginHandler loginHandler = new KMLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    try {
                        startConversation(context, groupName, agents, bots, isUnique, startChatHandler);
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
                        public void onReceive(KMUser user) {
                            login(context, user, loginHandler);
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
        Intent intent = new Intent(context, KMConversationActivity.class);
        intent.putExtra(ConversationUIService.GROUP_ID, groupId);
        intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
        context.startActivity(intent);
    }

    @Deprecated
    public static void startNewConversation(Context context, String groupName, String agentId, String botId, boolean isUniqueChat, KMStartChatHandler handler) throws KmException {
        ArrayList<String> agentIds = null;
        ArrayList<String> botIds = null;
        if (agentId != null) {
            agentIds = new ArrayList<>();
            agentIds.add(agentId);
        }
        if (botId != null) {
            botIds = new ArrayList<>();
            botIds.add(botId);
        }
        startConversation(context, groupName, agentIds, botIds, isUniqueChat, handler);
    }

    public static void startConversation(final Context context, final String groupName, final List<String> agentIds, final List<String> botIds, final boolean isUniqueChat, final KMStartChatHandler handler) throws KmException {
        if (agentIds == null || agentIds.isEmpty()) {
            KmCallback callback = new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    KmAgentModel.KmResponse agent = (KmAgentModel.KmResponse) message;
                    if (agent != null) {
                        List<String> agents = new ArrayList<>();
                        agents.add(agent.getAgentId());
                        try {
                            if (isUniqueChat) {
                                startOrGetConversation(context, groupName, agents, botIds, handler);
                            } else {
                                createConversation(context, groupName, agents, botIds, false, handler);
                            }
                        } catch (KmException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Object error) {
                    if (handler != null) {
                        handler.onFailure(null, context);
                    }
                }
            };

            new KmGetAgentListTask(context, MobiComKitClientService.getApplicationKey(context), callback).execute();
        } else {
            if (isUniqueChat) {
                startOrGetConversation(context, groupName, agentIds, botIds, handler);
            } else {
                createConversation(context, groupName, agentIds, botIds, false, handler);
            }
        }
    }

    @Deprecated
    private static void createConversation(Context context, String groupName, List<String> agentIds, List<String> botIds, boolean isUniqueChat, KMStartChatHandler handler) throws KmException {
        List<KMGroupInfo.GroupUser> users = new ArrayList<>();

        KMGroupInfo channelInfo = new KMGroupInfo(TextUtils.isEmpty(groupName) ? "Kommunicate Support" : groupName, new ArrayList<String>());

        if (agentIds == null || agentIds.isEmpty()) {
            throw new KmException("Agent Id list cannot be null or empty");
        }
        for (String agentId : agentIds) {
            users.add(channelInfo.new GroupUser().setUserId(agentId).setGroupRole(1));
        }

        users.add(channelInfo.new GroupUser().setUserId(KM_BOT).setGroupRole(2));
        users.add(channelInfo.new GroupUser().setUserId(MobiComUserPreference.getInstance(context).getUserId()).setGroupRole(3));

        if (botIds != null) {
            for (String botId : botIds) {
                if (botId != null && !KM_BOT.equals(botId)) {
                    users.add(channelInfo.new GroupUser().setUserId(botId).setGroupRole(2));
                }
            }
        }

        channelInfo.setType(10);
        channelInfo.setUsers(users);

        if (!agentIds.isEmpty()) {
            channelInfo.setAdmin(agentIds.get(0));
        }

        if (isUniqueChat) {
            channelInfo.setClientGroupId(getClientGroupId(MobiComUserPreference.getInstance(context).getUserId(), agentIds, botIds));
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

        channelInfo.setMetadata(metadata);

        Utils.printLog(context, TAG, "ChannelInfo : " + GsonUtils.getJsonFromObject(channelInfo, ChannelInfo.class));

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

        new AlChannelCreateAsyncTask(context, channelInfo, handler).execute();
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
        new PushNotificationTask(context, token, listener).execute();
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
    public static void startOrGetConversation(Context context, final String groupName, final List<String> agentIds, final List<String> botIds, final KMStartChatHandler handler) throws KmException {

        final String clientGroupId = getClientGroupId(MobiComUserPreference.getInstance(context).getUserId(), agentIds, botIds);

        AlGroupInformationAsyncTask.GroupMemberListener groupMemberListener = new AlGroupInformationAsyncTask.GroupMemberListener() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                if (handler != null) {
                    handler.onSuccess(channel, context);
                }
            }

            @Override
            public void onFailure(Channel channel, Exception e, Context context) {
                try {
                    createConversation(context, groupName, agentIds, botIds, true, handler);
                } catch (KmException e1) {
                    handler.onFailure(null, context);
                }
            }
        };

        new AlGroupInformationAsyncTask(context, clientGroupId, groupMemberListener).execute();
    }

    private static String getClientGroupId(String userId, List<String> agentIds, List<String> botIds) throws KmException {

        if (agentIds == null || agentIds.isEmpty()) {
            throw new KmException("Please add at-least one Agent");
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
}
