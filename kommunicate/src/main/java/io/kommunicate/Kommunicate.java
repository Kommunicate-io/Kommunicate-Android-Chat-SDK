package io.kommunicate;

import android.content.Context;
import android.content.Intent;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelCreateAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.activities.KMConversationActivity;
import io.kommunicate.async.GetUserListAsyncTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.users.KMGroupUser;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 23/01/18.
 */

public class Kommunicate {

    private static final String KM_BOT = "bot";
    public static final String APP_KEY = "22823b4a764f9944ad7913ddb3e43cae1";
    public static final String START_NEW_CHAT = "startNewChat";
    public static final String LOGOUT_CALL = "logoutCall";

    public static void init(Context context, String applicationKey) {
        Applozic.init(context, applicationKey);
    }

    public static void login(Context context, KMUser kmUser, KMLoginHandler handler) {
        Applozic.loginUser(context, kmUser, handler);
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

    public static void startNewConversation(Context context, String agentId, String botId, KMStartChatHandler handler) {
        List<KMGroupUser> users = new ArrayList<>();
        users.add(new KMGroupUser().setUserId(agentId).setGroupRole(1));
        users.add(new KMGroupUser().setUserId(botId != null ? botId : KM_BOT).setGroupRole(2));
        users.add(new KMGroupUser().setUserId(MobiComUserPreference.getInstance(context).getUserId()).setGroupRole(3));

        KMGroupInfo channelInfo = new KMGroupInfo("Kommunicate Support", new ArrayList<String>());
        channelInfo.setType(10);
        channelInfo.setUsers(users);
        channelInfo.setAdmin(agentId);

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

        new AlChannelCreateAsyncTask(context, channelInfo, handler).execute();
    }

    public static void getAgents(Context context, int startIndex, int pageSize, KMGetContactsHandler handler) {
        List<String> roleName = new ArrayList<>();
        roleName.add(KMUser.RoleName.APPLICATION_ADMIN.getValue());
        roleName.add(KMUser.RoleName.APPLICATION_WEB_ADMIN.getValue());

        new GetUserListAsyncTask(context, roleName, startIndex, pageSize, handler).execute();
    }
}
