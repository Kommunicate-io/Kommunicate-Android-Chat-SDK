package io.kommunicate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.List;

import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMStartChatHandler;

/**
 * Created by ashish on 01/06/18.
 */

public class KmHelper {

    public static void performLogout(Context context, final Object object) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.logging_out_wait));
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

    public static void setStartNewUniqueChat(Context context, final List<String> agentIds, List<String> botIds) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.start_chat_wait));
        dialog.setCancelable(false);
        dialog.show();
        try {
            Kommunicate.startOrGetConversation(context, null, agentIds, botIds, new KMStartChatHandler() {
                @Override
                public void onSuccess(Channel channel, Context context) {
                    dialog.dismiss();
                    Kommunicate.openParticularConversation(context, channel.getKey());
                }
                @Override
                public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                    dialog.dismiss();
                    Toast.makeText(context, "Unable to create conversation : " + channelFeedApiResponse, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (KmException e) {
            dialog.dismiss();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void setStartNewChat(Context context, final List<String> agentIds, List<String> botIds) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.start_chat_wait));
        dialog.setCancelable(false);
        dialog.show();
        try {
            Kommunicate.startNewConversation(context, null, agentIds, botIds, false, new KMStartChatHandler() {
                @Override
                public void onSuccess(Channel channel, Context context) {
                    dialog.dismiss();
                    if (channel != null) {
                        Kommunicate.openParticularConversation(context, channel.getKey());
                    }
                }
                @Override
                public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                    dialog.dismiss();
                    Toast.makeText(context, context.getString(R.string.start_chat_unable) + channelFeedApiResponse, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (KmException e) {
            dialog.dismiss();
            e.printStackTrace();
        }
    }
}
