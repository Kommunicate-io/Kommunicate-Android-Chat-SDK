package io.kommunicate.ui.kommunicate.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import android.widget.Toast;

import io.kommunicate.devkit.KommunicateSettings;
import io.kommunicate.devkit.api.account.register.RegistrationResponse;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.broadcast.EventManager;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.utils.RichMessageSharedPreference;
import io.kommunicate.commons.commons.core.utils.Utils;

import java.util.List;

import io.kommunicate.KmChatBuilder;
import io.kommunicate.KmConversationHelper;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.users.KMUser;

/**
 * Created by ashish on 01/06/18.
 */


public class KmHelper {

    public static void performLogout(Context context, final Object object) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(Utils.getString(context, R.string.logout_info_text));
        dialog.setCancelable(false);
        dialog.show();
        Kommunicate.logout(context, new KMLogoutHandler() {
            @Override
            public void onSuccess(Context context) {
                dialog.dismiss();
                RichMessageSharedPreference.clearPreference();
                KmToast.success(context, Utils.getString(context, R.string.user_logout_info), Toast.LENGTH_SHORT).show();
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

    public static void setStartNewUniqueChat(final Context context, final List<String> agentIds, List<String> botIds) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.create_conv_msg));
        dialog.setCancelable(false);
        dialog.show();

        try {
            new KmChatBuilder(context).setAgentIds(agentIds).setBotIds(botIds).launchChat(new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Object error) {
                    dialog.dismiss();
                    KmToast.error(context, Utils.getString(context, R.string.unable_to_create_conversation) + ": " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
            KmToast.error(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void setStartNewChat(final Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(Utils.getString(context, R.string.create_conversation_info));
        dialog.setCancelable(false);
        dialog.show();

        try {
            KmConversationHelper.launchConversationIfLoggedIn(context, new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    EventManager.getInstance().sendOnStartNewConversation((Integer) message);

                    dialog.dismiss();
                }

                @Override
                public void onFailure(Object error) {
                    dialog.dismiss();
                    KmToast.error(context, Utils.getString(context, R.string.unable_to_create_conversation) + ": " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
        }
    }

    public static void performLogin(final Context context, User user) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.please_wait));
        dialog.setCancelable(false);
        dialog.show();

        KMUser kmUser = new KMUser();
        kmUser.setUserId(user.getUserId());
        kmUser.setEmail(user.getEmail());
        kmUser.setContactNumber(user.getContactNumber());
        kmUser.setDisplayName(user.getDisplayName());
        kmUser.setApplicationId(KommunicateSettings.getInstance(context).getApplicationKey());

        try {
            Kommunicate.login(context, kmUser, new KMLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    Kommunicate.registerForPushNotification(context, new KmPushNotificationHandler() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                        }
                    });
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Kommunicate.openConversation(context);
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    KmToast.error(context, Utils.getString(context, R.string.km_unable_to_start_conversation_error) + registrationResponse, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
        }
    }
}
