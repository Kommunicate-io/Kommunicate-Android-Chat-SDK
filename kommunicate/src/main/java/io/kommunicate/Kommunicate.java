package io.kommunicate;

import static io.kommunicate.utils.SentryUtils.configureSentryWithKommunicate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import io.kommunicate.usecase.KMUserLoginUseCase;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.data.AlPrefSettings;
import com.applozic.mobicommons.data.SecureSharedPreferences;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

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
import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.async.KmAwayMessageTask;
import io.kommunicate.async.KmConversationCreateTask;
import io.kommunicate.async.KmConversationInfoTask;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KMGetContactsHandler;
import io.kommunicate.callbacks.KMLogoutHandler;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmChatWidgetCallback;
import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.callbacks.KmGetConversationInfoCallback;
import io.kommunicate.callbacks.KmPrechatCallback;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.database.KmDatabaseHelper;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.models.KmPrechatInputModel;
import io.kommunicate.preference.KmPreference;
import io.kommunicate.usecase.PushNotificationUseCase;
import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KMAgentStatusHelper;
import io.kommunicate.utils.KmAppSettingPreferences;
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
    public static final String PLACEHOLDER_APP_ID = "<Your-APP-ID>";
    static private String faqPageName;
    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String ALPHA_NUMS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String REDUCE_AGENTS_BOTS = "Please reduce the number of agents or bots";
    private static final String USERID_CANT_BE_NULL = "UserId cannot be null";
    private static final String ADD_ATLEAST_ONE_AGENT = "Please add at-least one Agent";
    private static final String PUSH_TOKEN_CANTBE_NULL = "Push token cannot be null or empty";
    private static final String GET_ARTICLES = "getArticles";
    private static final String GET_SELECTED_ARTICLES = "getSelectedArticles";
    private static final String GET_ANSWERS = "getAnswers";
    private static final String GET_DASHBOARD_FAQ = "getDashboardFaq";
    private static final String CREATE_GROUP_MSG = "CREATE_GROUP_MESSAGE";
    private static final String REMOVE_MEMBER_MSG = "REMOVE_MEMBER_MESSAGE";
    private static final String ADD_MEMBER_MSG = "ADD_MEMBER_MESSAGE";
    private static final String JOIN_MEMBER_MSG = "JOIN_MEMBER_MESSAGE";
    private static final String GROUP_NAME_CHANGE_MSG = "GROUP_NAME_CHANGE_MESSAGE";
    private static final String GROUP_ICON_CHANGE_MSG = "GROUP_ICON_CHANGE_MESSAGE";
    private static final String GROUP_LEFT_MSG = "GROUP_LEFT_MESSAGE";
    private static final String DELETED_GROUP_MESSAGE = "DELETED_GROUP_MESSAGE";
    private static final String USER_ROLE_UPDATED_MESSAGE = "GROUP_USER_ROLE_UPDATED_MESSAGE";
    private static final String META_DATA_UPDATED_MESSAGE = "GROUP_META_DATA_UPDATED_MESSAGE";
    private static final String AGENTID_CANTBE_NULL = "Agent Id list cannot be null or empty";
    private static final String KM_CHAT_BUILDER_CANTBE_NULL = "KmChatBuilder cannot be null";
    private static final String NEEDS_ACTIVITY_CONTEXT = "This method needs Activity context";
    private static final String LAUNCHED_CHAT_LIST = "Successfully launched chat list";

    public static void setFaqPageName(String faqPageName) {
        Kommunicate.faqPageName = faqPageName;
    }

    public static String getFaqPageName() {
        return faqPageName;
    }

    public static void init(Context context, String applicationKey, Boolean enableDeviceRootDetection) {
        if (TextUtils.isEmpty(applicationKey) || PLACEHOLDER_APP_ID.equals(Applozic.getInstance(context).getApplicationKey())) {
            KmUtils.showToastAndLog(context, R.string.km_app_id_cannot_be_null);
        } else {
            Applozic.init(context, applicationKey);
        }
        KmAppSettingPreferences.setRootDetection(enableDeviceRootDetection);
        configureSentryWithKommunicate(context);
    }

    public static void init(Context context, String applicationKey) {
        init(context, applicationKey, true);
    }

    public static void enableSSLPinning(boolean isEnable) {
        KmAppSettingPreferences.setSSLPinningEnabled(isEnable);
    }

    public static void login(final Context context, final KMUser kmUser, final KMLoginHandler handler) {
        configureSentryWithKommunicate(context);
        if(KmUtils.isDeviceRooted() && handler != null) {
            handler.onFailure(null, new IllegalStateException(Utils.getString(context, R.string.km_device_rooted)));
            return;
        }

        if (isLoggedIn(context)) {
            String loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
            if (loggedInUserId.equals(kmUser.getUserId())) {
                RegistrationResponse registrationResponse = new RegistrationResponse();
                registrationResponse.setMessage(KM_ALREADY_LOGGED_IN_STATUS);
                Contact contact = new ContactDatabase(context).getContactById(loggedInUserId);
                if (contact != null) {
                    registrationResponse.setUserId(contact.getUserId());
                    registrationResponse.setContactNumber(contact.getContactNumber());
                    registrationResponse.setRoleType(contact.getRoleType());
                    registrationResponse.setImageLink(contact.getImageURL());
                    registrationResponse.setDisplayName(contact.getDisplayName());
                    registrationResponse.setStatusMessage(contact.getStatus());
                    registrationResponse.setMetadata(contact.getMetadata());
                }
                handler.onSuccess(registrationResponse, context);
            } else {
                logout(context, new KMLogoutHandler() {
                    @Override
                    public void onSuccess(Context context) {
                        login(context, kmUser, getKmLoginHandlerWithPush(handler), null);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        handler.onFailure(null, exception);
                    }
                });
            }
        } else {

            login(context, kmUser, getKmLoginHandlerWithPush(handler), null);
        }
    }

    private static KMLoginHandler getKmLoginHandlerWithPush(final KMLoginHandler handler) {
        return new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                if (handler != null) {
                    handler.onSuccess(registrationResponse, context);
                }
                Kommunicate.registerForPushNotification(context, new KmPushNotificationHandler() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {
                        Utils.printLog(context, TAG, "Registered for push notifications : " + registrationResponse);
                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        Utils.printLog(context, TAG, "Failed to register for push notifications : " + registrationResponse + " \n\n " + exception);
                    }
                });
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                if (handler != null) {
                    handler.onFailure(registrationResponse, exception);
                }
            }
        };
    }

    public static void login(final Context context, final KMUser kmUser, final KMLoginHandler handler, ResultReceiver prechatReceiver) {
        if (kmUser != null) {
            kmUser.setHideActionMessages(true);
            kmUser.setSkipDeletedGroups(true);
        }

        KMUserLoginUseCase.Companion.executeWithExecutor(context, kmUser, false, prechatReceiver, handler);
    }

    /**
     * To Launch the Pre Chat Lead Collection and get result from the Lead Collection Screen
     *
     * @param context          the context
     * @param progressDialog   the loading progressbar if needed otherwise can pass null
     * @param inputModelList   input model list to create the form in Lead Collection Screen
     * @param preChatGreetings To set the Greetings Label on Lead Collection Screen.
     * @param callback         To update the status
     * @throws KmException
     */
    public static void launchLeadCollection(final Context context, final ProgressDialog progressDialog, List<KmPrechatInputModel> inputModelList, final String preChatGreetings, final KmPrechatCallback<KMUser> callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException(NEEDS_ACTIVITY_CONTEXT);
        }

        ResultReceiver resultReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (KmConstants.PRECHAT_RESULT_CODE == resultCode) {
                    KMUser user = (KMUser) GsonUtils.getObjectFromJson(resultData.getString(KmConstants.KM_USER_DATA), KMUser.class);
                    if (callback != null) {
                        callback.onReceive(user, context, (ResultReceiver) resultData.getParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER));
                    }
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        };


        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.PRECHAT_ACTIVITY_NAME));
            if (!TextUtils.isEmpty(preChatGreetings)) {
                intent.putExtra(KmAppSettingModel.PRE_CHAT_GREETINGS, preChatGreetings);
            }
            intent.putExtra(KmPrechatInputModel.KM_PRECHAT_MODEL_LIST, GsonUtils.getJsonFromObject(inputModelList, List.class));
            intent.putExtra(KmConstants.PRECHAT_RESULT_RECEIVER, resultReceiver);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new KmException(e.getMessage());
        }
    }

    public static void loginAsVisitor(@NotNull Context context, @NotNull KMLoginHandler handler) {
        loginAsVisitor(context, User.Platform.ANDROID, handler);
    }

    public static void loginAsVisitor(@NotNull Context context, @NotNull User.Platform platform, KMLoginHandler handler) {
        getVisitor(new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                KMUser user = (KMUser) message;
                user.setPlatform(platform.getValue());
                login(context, user, handler);
            }

            @Override
            public void onFailure(Object error) {
                handler.onFailure(null, new ApplozicException("unable to create user."));
            }
        });
    }

    /**
     * To Check the Login status & launch the Pre Chat Lead Collection Screen
     *
     * @param context        the context
     * @param progressDialog progress dialog if needed otherwise can pass null
     * @param callback       the callback to update status
     */

    public static void launchConversationWithPreChat(final Context context, final ProgressDialog progressDialog, final KmCallback callback) throws KmException  {
        if (!(context instanceof Activity)) {
            throw new KmException("This method needs Activity context");
        }

        if(KmUtils.isDeviceRooted() && callback != null) {
            callback.onFailure(new IllegalStateException(Utils.getString(context, R.string.km_device_rooted)));
            return;
        }

        configureSentryWithKommunicate(context);
        Kommunicate.getVisitor(new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                final KMUser kmUser = (KMUser) message;

                if (isLoggedIn(context)) {
                    String loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();

                    if (loggedInUserId.equals(kmUser.getUserId())) {
                        launchChatDirectly(context, callback);
                    } else {
                        logout(context, new KMLogoutHandler() {
                            @Override
                            public void onSuccess(Context context) {
                                loginUserWithKmCallBack(context, kmUser, callback);
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                callback.onFailure(exception);
                            }
                        });
                    }
                } else {
                    checkForLeadCollection(context, progressDialog, kmUser, callback);
                }
            }

            @Override
            public void onFailure(Object error) {
                if (callback != null) {
                    callback.onFailure(new Exception("Failed to create user as visitor", (Throwable) error));
                }
            }
        });
    }

    /**
     * To Check Lead Collection enabled or not by fetching the appsetting.
     *
     * @param context        the context
     * @param progressDialog the progressDialog if needed otherwise can pass null
     * @param kmUser         Randomly created KMUser Object for registration if lead collection is disabled
     * @param callback       callback to update the status
     */
    public static void checkForLeadCollection(final Context context, final ProgressDialog progressDialog, final KMUser kmUser, final KmCallback callback) {
        new KmAppSettingTask(context, Applozic.getInstance(context).getApplicationKey(), new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                final KmAppSettingModel appSettingModel = (KmAppSettingModel) message;
                if (appSettingModel != null && appSettingModel.getResponse() != null && appSettingModel.getChatWidget() != null) {
                    if (appSettingModel.getResponse().isCollectLead() && appSettingModel.getResponse().getLeadCollection() != null) {
                        Utils.printLog(context, TAG, "Lead Collection is enabled..Launching Lead Collection");
                        loginLeadUserAndOpenChat(context, appSettingModel, progressDialog, callback);
                    } else {
                        Utils.printLog(context, TAG, "Lead Collection is Disabled..Launching Random Login");
                        loginUserWithKmCallBack(context, kmUser, callback);
                    }
                } else {
                    Utils.printLog(context, TAG, "Failed to fetch App setting..Launching Random Login");
                    loginUserWithKmCallBack(context, kmUser, callback);
                }
            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Failed to fetch AppSetting");
                loginUserWithKmCallBack(context, kmUser, callback);

            }
        }).execute();
    }

    /**
     * To Login the user from Lead Collection Details.
     *
     * @param context         the context
     * @param appSettingModel appsetting model to get lead collection screen's input field & Greeting Message
     * @param progressDialog  progressbar used in previous activities to close the progressbar if coming back from lead collection screeen
     * @param callback        To update the status.
     **/
    public static void loginLeadUserAndOpenChat(final Context context, KmAppSettingModel appSettingModel, ProgressDialog progressDialog, final KmCallback callback) {
        try {
            launchLeadCollection(context, progressDialog, appSettingModel.getResponse().getLeadCollection(), appSettingModel.getChatWidget().getPreChatGreetingMsg(), new KmPrechatCallback<KMUser>() {
                @Override
                public void onReceive(KMUser data, final Context context, ResultReceiver finishActivityReceiver) {
                    loginUserWithKmCallBack(context, data, callback);
                    finishActivityReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
                }

                @Override
                public void onError(String error) {
                    Utils.printLog(context, TAG, "Failed to load Pre Chat Screen" + error);
                    callback.onFailure(new Exception(error));
                }
            });
        } catch (Exception e) {
            Utils.printLog(context, TAG, "Failed to launch the Lead Collection Screen");

            callback.onFailure(e);
        }
    }

    /**
     * To Log in the user with given KmUser object
     *
     * @param context  the context
     * @param kmUser   kmUser to log in
     * @param callback to update the status
     */
    public static void loginUserWithKmCallBack(final Context context, KMUser kmUser, final KmCallback callback) {
        KMLoginHandler handler = new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                launchChatDirectly(context, callback);
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                Utils.printLog(context, TAG, "Registration Failure" + exception.getMessage());
                callback.onFailure(exception);
            }
        };

        KMUserLoginUseCase.Companion.executeWithExecutor(context, kmUser, false, null, handler);
    }

    /**
     * To launch the chat screen directly if the user is new or user has only one previous conversation.Otherwise widget will open Conversation List Screen.
     *
     * @param context  the context
     * @param callback callback to update status
     */
    public static void launchChatDirectly(final Context context, final KmCallback callback) {
        KmConversationBuilder conversationBuilder = new KmConversationBuilder(context);
        conversationBuilder.launchAndCreateIfEmpty(new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                callback.onSuccess(message);
                Utils.printLog(context, TAG, message.toString());
            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Failed to open chat" + error.toString());
                callback.onFailure(error);

            }
        });
    }


    /**
     * To update the assignee status(online/offline/away) dynamically
     *
     * @param assigneeId  conversation assignee id
     * @param status to update the assignee status
     */
    public static void updateAssigneeStatus(String assigneeId , KMAgentStatusHelper.KMAgentStatus status) {
        KMAgentStatusHelper.updateAssigneeStatus(assigneeId,status);
    }


    public static void logout(Context context, final KMLogoutHandler logoutHandler) {
        KMLogoutHandler handler = new KMLogoutHandler() {
            @Override
            public void onSuccess(Context context) {
                KmDatabaseHelper.getInstance(context).deleteDatabase();
                KmPreference.getInstance(context).setFcmRegistrationCallDone(false);
                removeApplicationKey(context);
                logoutHandler.onSuccess(context);
            }

            @Override
            public void onFailure(Exception exception) {
                logoutHandler.onFailure(exception);
            }
        };

        Applozic.logoutUser(context, handler);
    }

    public static void closeConversationScreen(Context context) {
        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.CONVERSATION_ACTIVITY_NAME));
            intent.putExtra(KmConstants.CLOSE_CONVERSATION_SCREEN, true);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
                callback.onSuccess(LAUNCHED_CHAT_LIST);
            }
        } catch (ClassNotFoundException e) {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    public static void launchPrechatWithResult(final Context context, final KmPrechatCallback<KMUser> callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException(NEEDS_ACTIVITY_CONTEXT);
        }

        ResultReceiver resultReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (KmConstants.PRECHAT_RESULT_CODE == resultCode) {
                    KMUser user = (KMUser) GsonUtils.getObjectFromJson(resultData.getString(KmConstants.KM_USER_DATA), KMUser.class);
                    configureSentryWithKommunicate(context);
                    if (callback != null) {
                        callback.onReceive(user, context, (ResultReceiver) resultData.getParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER));
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

    public static void launchPrechatWithResult(final Context context, List<KmPrechatInputModel> inputModelList, final KmPrechatCallback<Map<String, String>> callback) {
        if (!(context instanceof Activity) && callback != null) {
            callback.onError(NEEDS_ACTIVITY_CONTEXT);
        }

        ResultReceiver resultReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (KmConstants.PRECHAT_RESULT_CODE == resultCode) {
                    Map<String, String> dataMap = (Map<String, String>) GsonUtils.getObjectFromJson(resultData.getString(KmConstants.KM_USER_DATA), new TypeToken<Map<String, String>>() {
                    }.getType());

                    if (callback != null) {
                        callback.onReceive(dataMap, context, (ResultReceiver) resultData.getParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER));
                    }
                }
            }
        };

        try {
            Intent intent = new Intent(context, KmUtils.getClassFromName(KmConstants.PRECHAT_ACTIVITY_NAME));
            intent.putExtra(KmPrechatInputModel.KM_PRECHAT_MODEL_LIST, GsonUtils.getJsonFromObject(inputModelList, List.class));
            intent.putExtra(KmConstants.PRECHAT_RETURN_DATA_MAP, true);
            intent.putExtra(KmConstants.PRECHAT_RESULT_RECEIVER, resultReceiver);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }


    public static void notifiyPrechatActivity(ResultReceiver finishActivityReceiver) {
        if (finishActivityReceiver != null) {
            finishActivityReceiver.send(KmConstants.PRECHAT_RESULT_CODE, null);
        }
    }

    public static void setNotificationSoundPath(Context context, String path) {
        Applozic.getInstance(context).setCustomNotificationSound(path);
    }

    public  static  void setServerConfiguration(Context context,KMServerConfiguration configuration){
        if (configuration == KMServerConfiguration.EUCONFIGURATION) {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(BuildConfig.EU_CHAT_SERVER_URL);
            ALSpecificSettings.getInstance(context).setKmBaseUrl(BuildConfig.EU_API_SERVER_URL);
            MobiComUserPreference.getInstance(context).setMqttBrokerUrl(BuildConfig.MQTT_URL_EU);
        } else {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(BuildConfig.CHAT_SERVER_URL);
            ALSpecificSettings.getInstance(context).setKmBaseUrl(BuildConfig.API_SERVER_URL);
            MobiComUserPreference.getInstance(context).setMqttBrokerUrl(BuildConfig.MQTT_URL);

        }
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
            throw new KmException(KM_CHAT_BUILDER_CANTBE_NULL);
        }

        if (chatBuilder.getAgentIds() == null || chatBuilder.getAgentIds().isEmpty()) {
            KmCallback callback = new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    KmAppSettingModel.KmResponse agent = (KmAppSettingModel.KmResponse) message;
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

            new KmAppSettingTask(chatBuilder.getContext(), MobiComKitClientService.getApplicationKey(chatBuilder.getContext()), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            throw new KmException(AGENTID_CANTBE_NULL);
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
        metadata.put(CREATE_GROUP_MSG, "");
        metadata.put(REMOVE_MEMBER_MSG, "");
        metadata.put(ADD_MEMBER_MSG, "");
        metadata.put(JOIN_MEMBER_MSG, "");
        metadata.put(GROUP_NAME_CHANGE_MSG, "");
        metadata.put(GROUP_ICON_CHANGE_MSG, "");
        metadata.put(GROUP_LEFT_MSG, "");
        metadata.put(DELETED_GROUP_MESSAGE, "");
        metadata.put(USER_ROLE_UPDATED_MESSAGE, "");
        metadata.put(META_DATA_UPDATED_MESSAGE, "");
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

        new KmConversationCreateTask(chatBuilder.getContext(), channelInfo, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void fetchAgentList(Context context, int startIndex, int pageSize, int orderBy, KMGetContactsHandler handler) {
        List<String> roleName = new ArrayList<>();
        roleName.add(KMUser.RoleName.APPLICATION_ADMIN.getValue());
        roleName.add(KMUser.RoleName.APPLICATION_WEB_ADMIN.getValue());
        fetchUserList(context, roleName, startIndex, pageSize, orderBy, handler);
    }

    public static void fetchBotList(Context context, int startIndex, int pageSize, int orderBy, KMGetContactsHandler handler) {
        List<String> roleName = new ArrayList<>();
        roleName.add(KMUser.RoleName.BOT.getValue());
        fetchUserList(context, roleName, startIndex, pageSize, orderBy, handler);
    }

    public static void fetchUserList(Context context, List<String> roleNameList, int startIndex, int pageSize, int orderBy, KMGetContactsHandler handler) {
        new GetUserListAsyncTask(context, roleNameList, startIndex, pageSize, orderBy, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getFaqs(Context context, String type, String helpDocsKey, String data, KmFaqTaskListener listener) {
        KMFaqTask task = new KMFaqTask(context, helpDocsKey, data, listener);
        if (GET_ARTICLES.equals(type)) {
            task.forArticleRequest();
        } else if (GET_SELECTED_ARTICLES.equals(type)) {
            task.forSelectedArticles();
        } else if (GET_ANSWERS.equals(type)) {
            task.forAnswerRequest();
        } else if (GET_DASHBOARD_FAQ.equals(type)) {
            task.forDashboardFaq();
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getHelpDocsKey(Context context, String type, KmFaqTaskListener listener) {
        new KMHelpDocsKeyTask(context, type, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static boolean isLoggedIn(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    public static void registerForPushNotification(final Context context, String token, final KmPushNotificationHandler listener) {
        if (TextUtils.isEmpty(token)) {
            if (listener != null) {
                listener.onFailure(null, new KmException(PUSH_TOKEN_CANTBE_NULL));
            }
            return;
        }

        if (!token.equals(getDeviceToken(context)) || !KmPreference.getInstance(context).isFcmRegistrationCallDone()) {
            setDeviceToken(context, token);
            PushNotificationUseCase.executeWithExecutor(context, token, new KmPushNotificationHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse) {
                    KmPreference.getInstance(context).setFcmRegistrationCallDone(true);
                    if (listener != null) {
                        listener.onSuccess(registrationResponse);
                    }
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    if (listener != null) {
                        listener.onFailure(registrationResponse, exception);
                    }
                }
            });
        }
    }

    public static void updateDeviceToken(Context context, String deviceToken) {
        if (TextUtils.isEmpty(deviceToken)) {
            return;
        }
        if (MobiComUserPreference.getInstance(context).isRegistered() && !deviceToken.equals(getDeviceToken(context))) {
            try {
                new RegisterUserClientService(context).updatePushNotificationId(deviceToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setDeviceToken(context, deviceToken);
    }

    public static void registerForPushNotification(Context context, KmPushNotificationHandler listener) {
        registerForPushNotification(context, getDeviceToken(context), listener);
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

        new KmConversationInfoTask(chatBuilder.getContext(), chatBuilder.getClientConversationId(), conversationInfoCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static String getClientGroupId(String userId, List<String> agentIds, List<String> botIds) throws KmException {

        if (agentIds == null || agentIds.isEmpty()) {
            throw new KmException(ADD_ATLEAST_ONE_AGENT);
        }

        if (TextUtils.isEmpty(userId)) {
            throw new KmException(USERID_CANT_BE_NULL);
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
            throw new KmException(REDUCE_AGENTS_BOTS);
        }

        return sb.toString();
    }

    public static void getVisitor(KmCallback callback) {
        final KMUser user = new KMUser();
        user.setUserId(generateUserId());
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());

        new KmAppSettingTask(
                ApplozicService.getAppContext(),
                MobiComKitClientService.getApplicationKey(ApplozicService.getAppContext()),
                new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        KmAppSettingModel appSettingModel = (KmAppSettingModel) message;
                        if (appSettingModel != null && appSettingModel.getResponse() != null && appSettingModel.getChatWidget() != null) {
                            if (appSettingModel.getChatWidget().isPseudonymsEnabled() &&
                                    !TextUtils.isEmpty(appSettingModel.getResponse().getUserName())) {
                                user.setDisplayName(appSettingModel.getResponse().getUserName());
                                updateMetadataForAnonymousUser(user);
                            }
                        }
                        callback.onSuccess(user);
                    }

                    @Override
                    public void onFailure(Object error) {
                        callback.onFailure(error);
                    }
                }
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static void updateMetadataForAnonymousUser(KMUser user){
        Map<String,String> visitorMetadata = new HashMap<>();
        visitorMetadata.put(KmConstants.HIDDEN,"true");
        visitorMetadata.put(KmConstants.PSEUDONAME,"true");
        String visitorMetadataString = GsonUtils.getJsonFromObject(visitorMetadata, Map.class);
        Map<String,String> userMetadata = new HashMap<>();
        userMetadata.put(KmConstants.KM_PSEUDO_USER,visitorMetadataString);
        user.setMetadata(userMetadata);
    }

    private static String generateUserId() {
        StringBuilder text = new StringBuilder("");
        SecureRandom random = new SecureRandom();
        String possible = ALPHA_NUMS;

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
        new KmAwayMessageTask(context, groupId, handler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void removeApplicationKey(Context context) {
        new SecureSharedPreferences(AlPrefSettings.AL_PREF_SETTING_KEY, ApplozicService.getContext(context)).edit().remove(APPLICATION_KEY).commit();
    }

    public static void setChatText(Context context, String PreFilledText) {
        BroadcastService.onAutoText(context,PreFilledText);
    }

    public static void hideAssigneeStatus(Context context,Boolean hide) {
        BroadcastService.hideAssignee(context,hide);
    }

    public static void isChatWidgetDisabled(final KmChatWidgetCallback callback) {
        final KmAppSettingModel appSettingModel = new KmAppSettingModel();

        new KmAppSettingTask(ApplozicService.getAppContext(),
                MobiComKitClientService.getApplicationKey(ApplozicService.getAppContext()),
                new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        final KmAppSettingModel appSettingModel = (KmAppSettingModel) message;
                        boolean isDisabled = false;
                        if (appSettingModel != null && appSettingModel.getResponse() != null && appSettingModel.getChatWidget() != null) {
                            isDisabled =   appSettingModel.getChatWidget().isDisableChatWidget();

                        }
                        if (callback != null) {
                            callback.onResult(isDisabled);
                        }
                    }
                    @Override
                    public void onFailure(Object error) {
                        if (callback != null) {
                            callback.onResult(false);
                        }
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
