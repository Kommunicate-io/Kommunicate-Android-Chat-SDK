package io.kommunicate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.conversation.ApplozicConversation;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicomkit.uiwidgets.async.AlGroupInformationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.List;

import io.kommunicate.activities.LeadCollectionActivity;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KMStartChatHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.callbacks.KmPrechatCallback;
import io.kommunicate.users.KMUser;

public class KmConversationHelper {

    public static void openConversation(final Context context, final boolean skipChatList, final Integer chatId, final KmCallback callback) throws KmException {
        if (!(context instanceof Activity)) {
            throw new KmException("This method needs Activity context");
        }

        if (chatId == null) {
            ApplozicConversation.getLatestMessageList(context, false, new MessageListHandler() {
                @Override
                public void onResult(List<Message> messageList, ApplozicException e) {
                    if (messageList != null) {
                        if (messageList.size() == 1) {
                            Message message = messageList.get(0);
                            if (message.getGroupId() != null && message.getGroupId() != 0) {
                                AlGroupInformationAsyncTask.GroupMemberListener memberListener = new AlGroupInformationAsyncTask.GroupMemberListener() {
                                    @Override
                                    public void onSuccess(Channel channel, Context context) {
                                        if (channel != null) {
                                            openParticularConversation(context, skipChatList, channel.getKey(), callback);
                                        } else {
                                            Kommunicate.openConversation(context, callback);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Channel channel, Exception e, Context context) {
                                        Kommunicate.openConversation(context, callback);
                                    }
                                };

                                new AlGroupInformationAsyncTask(context, message.getGroupId(), memberListener).execute();
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
            openParticularConversation(context, skipChatList, chatId, callback);
        }
    }

    private static void openParticularConversation(Context context, boolean skipChatList, Integer chatId, KmCallback callback) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(ConversationUIService.GROUP_ID, chatId);
        intent.putExtra(ConversationUIService.TAKE_ORDER, skipChatList);
        context.startActivity(intent);
        if (callback != null) {
            callback.onSuccess("Successfully launched chat with ChatId : " + chatId);
        }
    }

    public static void launchChat(final KmChatBuilder launchChat, final KmCallback callback) {
        if (launchChat == null) {
            if (callback != null) {
                callback.onFailure("Chat Builder cannot be null");
            }
            return;
        }

        if (launchChat.getContext() == null) {
            if (callback != null) {
                callback.onFailure("Context cannot be null");
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
                        callback.onFailure("Application ID cannot be empty");
                    }
                }
            }
            if (launchChat.isWithPreChat()) {
                try {
                    Kommunicate.launchPrechatWithResult(launchChat.getContext(), new KmPrechatCallback() {
                        @Override
                        public void onReceive(KMUser user, ResultReceiver resultReceiver) {
                            Kommunicate.login(launchChat.getContext(), user, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), true, resultReceiver, callback), callback));
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

                Kommunicate.login(launchChat.getContext(), kmUser, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), true, null, callback), callback));
            }
        }
    }

    public static void createChat(final KmChatBuilder launchChat, final KmCallback callback) {
        if (launchChat == null) {
            if (callback != null) {
                callback.onFailure("Chat Builder cannot be null");
            }
            return;
        }

        if (launchChat.getContext() == null) {
            if (callback != null) {
                callback.onFailure("Context cannot be null");
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
                        callback.onFailure("Application ID cannot be empty");
                    }
                }
            }
            if (launchChat.isWithPreChat()) {
                try {
                    Kommunicate.launchPrechatWithResult(launchChat.getContext(), new KmPrechatCallback() {
                        @Override
                        public void onReceive(KMUser user, ResultReceiver resultReceiver) {
                            Kommunicate.login(launchChat.getContext(), user, getLoginHandler(launchChat, getStartChatHandler(launchChat.isSkipChatList(), false, resultReceiver, callback), callback));
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

    private static KMStartChatHandler getStartChatHandler(final boolean isSkipChatList, final boolean launchChat, final ResultReceiver resultReceiver, final KmCallback callback) {
        return new KMStartChatHandler() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                try {
                    if (resultReceiver != null) {
                        resultReceiver.send(LeadCollectionActivity.PRECHAT_RESULT_CODE, null);
                    }
                    if (callback != null) {
                        if (launchChat) {
                            openConversation(context, isSkipChatList, channel.getKey(), callback);
                        } else {
                            callback.onSuccess(channel.getKey());
                        }
                    }
                } catch (KmException e) {
                    if (resultReceiver != null) {
                        resultReceiver.send(LeadCollectionActivity.PRECHAT_RESULT_CODE, null);
                    }
                    if (callback != null) {
                        e.getMessage();
                    }
                }
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                if (resultReceiver != null) {
                    resultReceiver.send(LeadCollectionActivity.PRECHAT_RESULT_CODE, null);
                }
                if (callback != null) {
                    callback.onFailure(channelFeedApiResponse);
                }
            }
        };
    }

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
                callback.onFailure(registrationResponse);
            }
        };
    }
}
