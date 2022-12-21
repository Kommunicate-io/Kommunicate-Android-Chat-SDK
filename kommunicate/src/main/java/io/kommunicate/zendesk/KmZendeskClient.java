package io.kommunicate.zendesk;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.conversation.AlConversationResponse;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.kommunicate.KmConversationHelper;
import io.kommunicate.KmException;
import io.kommunicate.R;
import io.kommunicate.async.KmStatusUpdateTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmClientService;
import io.kommunicate.utils.KmAppSettingPreferences;
import zendesk.chat.Attachment;
import zendesk.chat.Chat;
import zendesk.chat.ChatLog;
import zendesk.chat.ChatParticipant;
import zendesk.chat.ChatState;
import zendesk.chat.CompletionCallback;
import zendesk.chat.ConnectionStatus;
import zendesk.chat.JwtAuthenticator;
import zendesk.chat.FileUploadListener;
import zendesk.chat.ObservationScope;
import zendesk.chat.Observer;


/**
 * Zendesk Chat SDK V2 integration
 *
 * @author Aman
 * @date December '22
 */

public class KmZendeskClient {

    private static String TAG = "KmZendeskClient";
    private static KmZendeskClient kmZendeskClient;
    private boolean zendeskConnected;
    private boolean transcriptSent;
    private boolean zendeskInitialized;
    private boolean handoffHappened;
    private Contact contact;
    private Channel channel;
    private Context context;
    private ObservationScope observationScope;
    private Long lastSyncTime;
    private List<String> messagesInBuffer;

    private KmZendeskClient(Context context) {
        this.context = context;
        observationScope = new ObservationScope();
        messagesInBuffer = new ArrayList<>();
    }

    public static KmZendeskClient getInstance(Context context) {
        if(kmZendeskClient == null) {
            kmZendeskClient = new KmZendeskClient(context);
        }
        return kmZendeskClient;
    }

    //Initialize Zendesk with Zendesk Chat SDK Key
    public void initializeZendesk(String accountKey, Contact contact) {
        if(zendeskInitialized) {
            return;
        }
        Utils.printLog(context, TAG, "Zendesk Initialized with account key : " + accountKey);
        this.contact = contact;
        Chat.INSTANCE.init(context, accountKey);
        zendeskInitialized = true;
        authenticateZendeskUser(contact);
    }

    public void handleHandoff(Channel channel, boolean happenedNow) {
        this.channel = channel;
        handoffHappened = true;
        lastSyncTime = System.currentTimeMillis();
        if(happenedNow && !transcriptSent) {
            sendZendeskChatTranscript();
        }
    }

    //Checks Zendesk's socket connection and handle connection
    private void observeZendeskConnection() {
        Chat.INSTANCE.providers().connectionProvider().observeConnectionStatus(observationScope, new Observer<ConnectionStatus>() {
            @Override
            public void update(ConnectionStatus connectionStatus) {
                if(connectionStatus != ConnectionStatus.CONNECTED) {
                    connectToZendeskSocket();
                    return;
                }
                zendeskConnected = true;
                observeChatLogs();
                for(String message: messagesInBuffer) {
                    sendZendeskMessage(message);
                }
                messagesInBuffer.clear();
            }
        });
    }

    private void connectToZendeskSocket() {
        Chat.INSTANCE.providers().connectionProvider().connect();
    }

    //JWT Authentication for logged in users
    public void authenticateZendeskUser(final Contact contact) {
        if(TextUtils.isEmpty(contact.getDisplayName()) || TextUtils.isEmpty(contact.getUserId()) || TextUtils.isEmpty(contact.getEmailId())) {
            observeZendeskConnection();
            return;
        }
                    try {
                        JwtAuthenticator jwtAuthenticator = new JwtAuthenticator() {
                            @Override
                            public void getToken(final JwtCompletion jwtCompletion) {
                                try {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                new KmZendeskClientService(context).getJwtForZendeskAuthentication(contact.getUserId(), contact.getDisplayName(),contact.getEmailId(), jwtCompletion);
                                            } catch (Exception e) {

                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        };
                        Chat.INSTANCE.setIdentity(jwtAuthenticator, new CompletionCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                observeZendeskConnection();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
    }

    private void observeChatLogs() {
        lastSyncTime = MobiComUserPreference.getInstance(context).getZendeskLastSyncTime();

        Chat.INSTANCE.providers().chatProvider().observeChatState(observationScope, new Observer<ChatState>() {
            @Override
            public void update(ChatState chatState) {
                if(contact == null) {
                    return;
                }
                for (ChatLog log : chatState.getChatLogs()) {
                    if(lastSyncTime != null && lastSyncTime >= log.getCreatedTimestamp()) {
                        continue;
                    }

                    if(log instanceof ChatLog.Message && ChatParticipant.AGENT.equals(log.getChatParticipant())) {
                        Utils.printLog(context, TAG, "Zendesk Agent message : " + ((ChatLog.Message) log).getMessage() + "from :" + log.getDisplayName());
                        processAgentMessage(((ChatLog.Message) log).getMessage(), log.getDisplayName(), log.getNick(), channel.getKey(), log.getCreatedTimestamp());
                    } else if(log instanceof ChatLog.AttachmentMessage && ChatParticipant.AGENT.equals(log.getChatParticipant())) {
                        Utils.printLog(context, TAG, "Zendesk Agent  : " + ((ChatLog.AttachmentMessage) log).getAttachment().getName() + "from :" + log.getDisplayName());
                        processAttachmentAgentMessage(((ChatLog.AttachmentMessage) log).getAttachment(), log.getDisplayName(), log.getNick(), channel.getKey(), log.getCreatedTimestamp());
                    }
                    else if(log.getType().equals(ChatLog.Type.MEMBER_LEAVE) && ChatParticipant.AGENT.equals(log.getChatParticipant())) {
                        Utils.printLog(context, TAG, "Zendesk Agent Left");
                        processAgentLeave();
                    }
                    lastSyncTime = log.getCreatedTimestamp();
                }
                MobiComUserPreference.getInstance(context).setZendeskLastSyncTime(lastSyncTime);
            }
        });
    }

private void processAgentLeave() {
        if(channel == null || channel.getMetadata() == null) {
            return;
        }
        channel.getMetadata().put(Channel.CONVERSATION_STATUS, String.valueOf(2));
        channel.setKmStatus(Channel.CLOSED_CONVERSATIONS);
        ChannelService.getInstance(context).updateChannel(channel);

        new KmStatusUpdateTask(channel.getKey(), 2, true, new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                Utils.printLog(context, TAG, "Zendesk conversation resolved");
                BroadcastService.sendUpdate(context, true, BroadcastService
                        .INTENT_ACTIONS.CHANNEL_SYNC.toString());
                endZendeskChat();
            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Zendesk conversation failed to resolve");
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendZendeskMessage(String message) {
        if(!handoffHappened || !zendeskInitialized || TextUtils.isEmpty(message)) {
            return;
        }
        if(!zendeskConnected) {
            messagesInBuffer.add(message);
            return;
        }
        Utils.printLog(context, TAG, "Sent Zendesk Message : " + message);
        Chat.INSTANCE.providers().chatProvider().sendMessage(message);
    }

    //send Agent message from Zendesk dashboard to Kommunicate server
    //using SERIAL_EXECUTOR to send messages in correct order
    public void processAgentMessage(final String message, final String displayName, final String agentId, final Integer conversationId, final Long messageTimestamp) {
        new KmZendeskSendMessageTask(context, message, displayName, agentId, conversationId, messageTimestamp, new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                Utils.printLog(context, TAG, "Zendesk Send Agent Message success");
            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Zendesk Send Agent Message failed");
            }
        }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    //send Attachment from Zendesk dashboard to Kommunicate server
    private void processAttachmentAgentMessage(final Attachment attachment, final String displayName, final String agentId, final Integer conversationId, final Long messageTimestamp) {
        new KmZendeskSendMessageTask(context, attachment, displayName, agentId, conversationId, messageTimestamp, new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                Utils.printLog(context, TAG, "Zendesk Send Agent Message success");

            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Zendesk Send Agent Message failed");

            }
        }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void sendZendeskAttachment(String filePath) {
        if(!zendeskInitialized) {
            return;
        }
        Chat.INSTANCE.providers().chatProvider().sendFile(new File(filePath), new FileUploadListener() {
            @Override
            public void onProgress(String s, long l, long l1) {
                if(l1 == 0) {
                    Utils.printLog(context, TAG, "Attachment upload in progress");
                }
                if(l == l1) {
                    Utils.printLog(context, TAG, "Attachment uploaded successfully");

                }
            }
        });
    }

    //fetches Chat list and send the chat transcript to Zendesk
    public void sendZendeskChatTranscript() {
        if(contact == null || channel == null) {
            return;
        }
        final StringBuilder transcriptString = new StringBuilder();
        sendZendeskMessage(context.getString(R.string.km_zendesk_transcript_message, new KmClientService(context).getConversationShareUrl(), channel.getKey()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlConversationResponse kmConversationResponse = null;
                List<Message> listOfMessage;
                try {
                    kmConversationResponse = (AlConversationResponse) GsonUtils.getObjectFromJson(new MessageClientService(context).getMessages(contact, channel, null, null, channel.getKey(), false), AlConversationResponse.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (kmConversationResponse != null) {
                    listOfMessage = Arrays.asList(kmConversationResponse.getMessage());
                    Collections.reverse(listOfMessage);
                    for(Message message : listOfMessage) {
                        String username;
                        if(message.isHidden() || message.isChannelCustomMessage()) {
                            continue;
                        }
                        if(message.getContactIds().equals(contact.getContactIds())) {
                            username = "User";
                        } else {
                            username = new AppContactService(context).getContactById(message.getContactIds()).getDisplayName();
                        }
                        String messageString = getMessageForTranscript(message);
                        if(TextUtils.isEmpty(messageString) || TextUtils.isEmpty(username)) {
                            continue;
                        }
                        transcriptString.append(username).append(" : ").append(getMessageForTranscript(message)).append("\n");
                    }
                }
                sendZendeskMessage(transcriptString.toString());
                transcriptSent = true;
                Utils.printLog(context, TAG, String.valueOf(transcriptString));
            }
        }).start();
    }

    //Fetch list of messages and send it as a transcipt to Zendesk chat
    public String getMessageForTranscript(Message message) {
        if(!TextUtils.isEmpty(message.getMessage())) {
            return message.getMessage();
        }
        if(message.getFileMetas() != null && !TextUtils.isEmpty(message.getFileMetas().getBlobKeyString())) {
            return new KmClientService(context).getBaseUrl() + "/rest/ws/attachment/" + message.getFileMetas().getBlobKeyString();
        }
        if (message.getMetadata() != null && !TextUtils.isEmpty(message.getMetadata().get("templateId"))) {
            return "TemplateId: " + message.getMetadata().get("templateId");
        }
        return "";
    }

    public boolean isZendeskConnected() {
        return zendeskConnected;
    }
    public boolean isZendeskInitialized() {
        return zendeskInitialized;
    }

    public void openZendeskChat(final Context context) {
        this.context = context;
                 Integer conversationId = MobiComUserPreference.getInstance(context).getLatestZendeskConversationId();
                if(conversationId == null || conversationId == 0) {
                    KmConversationHelper.launchConversationIfLoggedIn(context, new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            Utils.printLog(context, TAG, "Successfully launched new conversation Id:" + String.valueOf(message));
                        }

                        @Override
                        public void onFailure(Object error) {
                            Utils.printLog(context, TAG, "Failed to launched Zendesk conversation Id:");
                        }
                    });
                    return;
                }
                AppContactService appContactService = new AppContactService(context);

                initializeZendesk(KmAppSettingPreferences.getInstance().getZendeskSdkKey(), appContactService.getContactById(MobiComUserPreference.getInstance(context).getUserId()));
                Channel latestChannel = ChannelService.getInstance(context).getChannel(conversationId);
                Contact assigneeContact = appContactService.getContactById(latestChannel.getConversationAssignee());
                if(User.RoleType.AGENT.getValue().equals(assigneeContact.getRoleType())) {
                    handleHandoff(latestChannel, false);
                }
        try {
            KmConversationHelper.openConversation(context, true, conversationId, new KmCallback() {
                @Override
                public void onSuccess(Object message) {
                    Utils.printLog(context, TAG, "Successfully launched Zendesk conversation Id:" + String.valueOf(message));
                }

                @Override
                public void onFailure(Object error) {
                    Utils.printLog(context, TAG, "Failed to launch Zendesk conversation : " + error.toString());
                }
            });
        } catch (KmException e) {
            e.printStackTrace();
        }
    }

    public void endZendeskChat() {
        if(!zendeskInitialized) {
            return;
        }
        Chat.INSTANCE.providers().chatProvider().endChat(new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                observationScope.cancel();
                kmZendeskClient = null;
                Utils.printLog(context, TAG, "Successfully ended Zendesk Chat");
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Utils.printLog(context, TAG, errorResponse.getReason() + errorResponse.getResponseBody());
            }
        });
    }
}
