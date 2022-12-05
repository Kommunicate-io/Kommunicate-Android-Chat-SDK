package io.kommunicate.services;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.conversation.AlConversationResponse;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.kommunicate.R;
import zendesk.chat.Chat;
import zendesk.chat.ChatInfo;
import zendesk.chat.ChatState;
import zendesk.chat.ConnectionStatus;
import zendesk.chat.ObservationScope;
import zendesk.chat.Observer;
import zendesk.chat.ProfileProvider;
import zendesk.chat.VisitorInfo;

public class KmZendeskClient {

    private static String TAG = "KmZendeskClient";
    private static KmZendeskClient kmZendeskClient;
    private Integer groupId;
    private ProfileProvider profileProvider;
    private boolean isZendeskConnected;
    private boolean isZendeskInitialized;
    private Contact contact;
    private Channel channel;
    private Context context;
    private ObservationScope observationScope;

    private KmZendeskClient(Context context) {
        this.context = context;
        observationScope = new ObservationScope();
    }

    public static KmZendeskClient getInstance(Context context) {
        if(kmZendeskClient == null) {
            kmZendeskClient = new KmZendeskClient(context);
        }
        return kmZendeskClient;
    }

    public void initializeZendesk(String accountKey, Integer channelKey, Contact contact, Channel channel ) {
        Utils.printLog(context, TAG, "zendesk initialized");
        Chat.INSTANCE.init(context, accountKey);
        isZendeskInitialized = true;
        groupId = channelKey;
        this.contact = contact;
        authenticateZendeskUser(contact);
        this.channel = channel;
        Chat.INSTANCE.providers().connectionProvider().observeConnectionStatus(observationScope, new Observer<ConnectionStatus>() {
            @Override
            public void update(ConnectionStatus connectionStatus) {
                if(connectionStatus != ConnectionStatus.CONNECTED) {
                    connectToZendeskSocket();
                    return;
                }
                isZendeskConnected = true;
                observeChatLogs();
                sendZendeskChatTranscript();
            }
        });

    }
    private void connectToZendeskSocket() {
        Chat.INSTANCE.providers().connectionProvider().connect();
    }

    public void authenticateZendeskUser(Contact contact) {
       // Chat.INSTANCE.providers().

        VisitorInfo visitorInfo = VisitorInfo.builder()
                .withName(TextUtils.isEmpty(contact.getDisplayName()) ? "" : contact.getDisplayName())
                .withEmail(TextUtils.isEmpty(contact.getEmailId()) ? "" : contact.getEmailId())
                .withPhoneNumber(TextUtils.isEmpty(contact.getContactNumber()) ? "" : contact.getContactNumber()) // numeric string
                .build();
        Chat.INSTANCE.providers().profileProvider().setVisitorInfo(visitorInfo, new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Utils.printLog(context, TAG, "loginsuccess");
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Utils.printLog(context, TAG, "loginfailed");
            }
        });
    }

    private void observeChatLogs() {
        Chat.INSTANCE.providers().chatProvider().observeChatState(observationScope, new Observer<ChatState>() {
            @Override
            public void update(ChatState chatState) {
                Utils.printLog(context, TAG, String.valueOf(chatState));
            }
        });
    }

    public void sendZendeskMessage(String message) {
        Utils.printLog(context, TAG, "sent message");
        Chat.INSTANCE.providers().chatProvider().sendMessage(message);
    }

    public void sendZendeskChatTranscript() {
        final StringBuilder transcriptString = new StringBuilder();
        sendZendeskMessage(context.getString(R.string.km_zendesk_transcript_message, new KmClientService(context).getConversationShareUrl(), groupId));

        MessageDatabaseService messageDatabaseService = new MessageDatabaseService(context);
        //List<Message> messageList = messageDatabaseService.getMessages(null, null, contact, channel, groupId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlConversationResponse kmConversationResponse = null;
                Message messageList[];
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
            }
        }).start();


           // Utils.printLog(context, TAG, String.valueOf(messageList));

    }

    public String getMessageForTranscript(Message message) {
        Utils.printLog(context, TAG, "SENDING TRANSXEIPR");
        if(!TextUtils.isEmpty(message.getMessage())) {
            return message.getMessage();
        }
        if(message.getFileMetas() != null && !TextUtils.isEmpty(message.getFileMetas().getBlobKeyString())) {
            return "/rest/ws/attachment/" + message.getFileMetas().getBlobKeyString();
        }
        if (message.getMetadata() != null && !TextUtils.isEmpty(message.getMetadata().get("templateId"))) {
            return "TemplateId: " + message.getMetadata().get("templateId");
        }
        return "";
    }

    public boolean isZendeskConnected() {
        return isZendeskConnected;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void isChatGoingOn(final ChatStatus chatStatus) {
        if(!isZendeskInitialized) {
            chatStatus.onChatFinished();
            return;
        }
        try {
                Chat.INSTANCE.providers().chatProvider().getChatInfo(new ZendeskCallback<ChatInfo>() {
                    @Override
                    public void onSuccess(ChatInfo chatInfo) {
                        if (chatInfo.isChatting()) {
                            chatStatus.onChatGoingOn();
                        } else {
                            chatStatus.onChatFinished();
                        }
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        chatStatus.onChatError(errorResponse.getReason());
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
                chatStatus.onChatFinished();
            }
    }

    public void endZendeskChat() {
        Chat.INSTANCE.providers().chatProvider().endChat(new ZendeskCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Utils.printLog(context, TAG, "Successfully ended Zendesk Chat");
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Utils.printLog(context, TAG, errorResponse.getReason() + errorResponse.getResponseBody());
            }
        });
        observationScope.cancel();
        kmZendeskClient = null;
    }
    public interface ChatStatus {
        void onChatGoingOn();
        void onChatFinished();
        void onChatError(String errorMessage);
    }
}
