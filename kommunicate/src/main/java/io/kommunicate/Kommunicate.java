package io.kommunicate;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelCreateAsyncTask;
import com.applozic.mobicomkit.uiwidgets.async.AlGroupInformationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.people.channel.Channel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import io.kommunicate.activities.KMConversationActivity;
import io.kommunicate.async.GetUserListAsyncTask;
import io.kommunicate.async.KMFaqTask;
import io.kommunicate.async.KMHelpDocsKeyTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 23/01/18.
 */

public class Kommunicate {

    private static final String KM_BOT = "bot";
    public static final String START_NEW_CHAT = "startNewChat";
    public static final String LOGOUT_CALL = "logoutCall";

    public static void init(Context context, String applicationKey) {
        Applozic.init(context, applicationKey);
    }

    public static void login(Context context, KMUser kmUser, KMLoginHandler handler) {
        Applozic.loginUser(context, kmUser, handler);
    }

    public static void loginAsVisitor(Context context, KMLoginHandler handler) {
        login(context, getVisitor(), handler);
    }

    public static void logout(Context context, KMLogoutHandler logoutHandler) {
        Applozic.logoutUser(context, logoutHandler);
    }

    public static void openConversation(Context context) {
        Intent intent = new Intent(context, KMConversationActivity.class);
        context.startActivity(intent);
    }

    public static void openParticularConversation(Context context, Integer groupId) {
        Intent intent = new Intent(context, KMConversationActivity.class);
        intent.putExtra(ConversationUIService.GROUP_ID, groupId);
        intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
        context.startActivity(intent);
    }

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
        startNewConversation(context, groupName, agentIds, botIds, isUniqueChat, handler);
    }

    public static void startNewConversation(Context context, String groupName, List<String> agentIds, List<String> botIds, boolean isUniqueChat, KMStartChatHandler handler) throws KmException {
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

        if (handler == null) {
            handler = new KMStartChatHandler() {
                @Override
                public void onSuccess(Channel channel, Context context) { }

                @Override
                public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) { }
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
        registerForPushNotification(context, Applozic.getInstance(context).getDeviceRegistrationId(), listener);
    }

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
                    startNewConversation(context, groupName, agentIds, botIds, true, handler);
                } catch (KmException e1) {
                    handler.onFailure(null, context);
                }
            }
        };

        new AlGroupInformationAsyncTask(context, clientGroupId, groupMemberListener).execute();
    }

    private static String getClientGroupId(String userId, List<String> agentIds, List<String> botIds) throws KmException {

        if (botIds != null && !botIds.contains(KM_BOT)) {
            botIds.add(KM_BOT);
        }

        if (botIds == null) {
            botIds = new ArrayList<>();
            botIds.add(KM_BOT);
        }

        List<String> tempList = new ArrayList<>();

        tempList.add(userId);
        tempList.addAll(botIds);
        tempList.addAll(agentIds);

        SortedSet<String> userIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        userIds.addAll(tempList);

        StringBuilder sb = new StringBuilder("");

        Iterator<String> iterator = userIds.iterator();
        while (iterator.hasNext()) {
            String temp = iterator.next();
            sb.append(temp);

            if (!temp.equals(userIds.last())) {
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
