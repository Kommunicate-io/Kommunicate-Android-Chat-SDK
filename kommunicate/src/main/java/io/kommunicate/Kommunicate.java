package io.kommunicate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelCreateAsyncTask;
import com.applozic.mobicomkit.uiwidgets.async.AlGroupInformationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.activities.KMConversationActivity;
import io.kommunicate.async.GetUserListAsyncTask;
import io.kommunicate.async.KmCreateConversationTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmCreateConversationHandler;
import io.kommunicate.users.KMGroupUser;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 23/01/18.
 */

public class Kommunicate {

    private static final String KM_BOT = "bot";
    //public static final String APP_KEY = "kommunicate-support";
    public static final String APP_KEY = "22823b4a764f9944ad7913ddb3e43cae1";
    //public static final String APP_KEY = "3c951e76437b755ce5ee8ad8a06703505";
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
        startNewConversation(context, null, null, agentId, botId, handler);
    }

    public static void startNewConversation(Context context, String clientGroupId, String groupName, String agentId, String botId, KMStartChatHandler handler) {
        List<KMGroupUser> users = new ArrayList<>();
        users.add(new KMGroupUser().setUserId(agentId).setGroupRole(1));
        users.add(new KMGroupUser().setUserId(KM_BOT).setGroupRole(2));
        if (botId != null && !KM_BOT.equals(botId)) {
            users.add(new KMGroupUser().setUserId(botId).setGroupRole(2));
        }
        users.add(new KMGroupUser().setUserId(MobiComUserPreference.getInstance(context).getUserId()).setGroupRole(3));

        KMGroupInfo channelInfo = new KMGroupInfo(TextUtils.isEmpty(groupName) ? "Kommunicate Support" : groupName, new ArrayList<String>());
        channelInfo.setType(10);
        channelInfo.setUsers(users);
        channelInfo.setAdmin(agentId);

        if (!TextUtils.isEmpty(clientGroupId)) {
            channelInfo.setClientGroupId(clientGroupId);
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

        new AlChannelCreateAsyncTask(context, channelInfo, handler).execute();
    }

    public static void getAgents(Context context, int startIndex, int pageSize, KMGetContactsHandler handler) {
        List<String> roleName = new ArrayList<>();
        roleName.add(KMUser.RoleName.APPLICATION_ADMIN.getValue());
        roleName.add(KMUser.RoleName.APPLICATION_WEB_ADMIN.getValue());

        new GetUserListAsyncTask(context, roleName, startIndex, pageSize, handler).execute();
    }

    public static void performLogout(Context context, final Object object) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Logging out, please wait...");
        dialog.setCancelable(false);
        dialog.show();
        Kommunicate.logout(context, new KMLogoutHandler() {
            @Override
            public void onSuccess(Context context) {
                dialog.dismiss();
                Toast.makeText(context, context.getString(com.applozic.mobicomkit.uiwidgets.R.string.user_logout_info), Toast.LENGTH_SHORT).show();
                Intent intent = null;
                try {
                    intent = new Intent(context, Class.forName((String) object));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                    ((FragmentActivity) context).finish();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                dialog.dismiss();
            }
        });
    }

    public static void setStartNewChat(Context context, final String agentId, String botId) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Creating conversation, please wait...");
        dialog.setCancelable(false);
        dialog.show();

        startNewConversation(context, agentId, botId, new KMStartChatHandler() {
            @Override
            public void onSuccess(final Channel channel, Context context) {

                KmCreateConversationHandler handler = new KmCreateConversationHandler() {
                    @Override
                    public void onSuccess(Context context, KmConversationResponse response) {
                        dialog.dismiss();
                        Kommunicate.openParticularConversation(context, channel.getKey());
                    }

                    @Override
                    public void onFailure(Context context, Exception e, String error) {
                        dialog.dismiss();
                        Toast.makeText(context, "Unable to create Conversation : " + (e == null ? error : e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                };
                new KmCreateConversationTask(context, channel.getKey(), MobiComUserPreference.getInstance(context).getUserId(), MobiComKitClientService.getApplicationKey(context), agentId, handler).execute();
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                dialog.dismiss();
                Toast.makeText(context, "Unable to create conversation : " + channelFeedApiResponse, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void startOrGetConversation(Context context, final String clientGroupId, final String agentId, final String botId, final String groupName) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage("Looking for conversation , please wait...");
        dialog.show();

        AlGroupInformationAsyncTask.GroupMemberListener groupMemberListener = new AlGroupInformationAsyncTask.GroupMemberListener() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                dialog.dismiss();
                openParticularConversation(context, channel.getKey());
            }

            @Override
            public void onFailure(Channel channel, Exception e, Context context) {

                dialog.setMessage("Creating Conversation , please wait...");

                startNewConversation(context, clientGroupId, groupName, agentId, botId, new KMStartChatHandler() {

                    @Override
                    public void onSuccess(final Channel channel, Context context) {

                        KmCreateConversationHandler handler = new KmCreateConversationHandler() {
                            @Override
                            public void onSuccess(Context context, KmConversationResponse response) {

                                dialog.dismiss();
                                Kommunicate.openParticularConversation(context, channel.getKey());
                            }

                            @Override
                            public void onFailure(Context context, Exception e, String error) {

                                dialog.dismiss();
                                Toast.makeText(context, "Unable to create Conversation : " + (e == null ? error : e.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        };
                        new KmCreateConversationTask(context, channel.getKey(), MobiComUserPreference.getInstance(context).getUserId(), MobiComKitClientService.getApplicationKey(context), agentId, handler).execute();
                    }

                    @Override
                    public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {

                        dialog.dismiss();
                        Toast.makeText(context, "Unable to create conversation : " + channelFeedApiResponse, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        new AlGroupInformationAsyncTask(context, clientGroupId, groupMemberListener).execute();
    }
}
