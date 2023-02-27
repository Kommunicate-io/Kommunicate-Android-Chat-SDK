package io.kommunicate.data.conversation;

import android.content.Context;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.kommunicate.callbacks.ConversationListHandler;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.conversation.database.MessageDatabaseService;
import io.kommunicate.data.channel.service.ChannelService;
import io.kommunicate.data.contact.AppContactService;
import io.kommunicate.data.people.channel.Channel;
import io.kommunicate.data.people.contact.Contact;
import io.kommunicate.data.async.task.AlAsyncTask;
import io.kommunicate.exception.KommunicateException;

public class ConversationListTask extends AlAsyncTask<Void, List<AlConversation>> {

    private WeakReference<Context> context;
    private String searchString;
    private Contact contact;
    private Channel channel;
    private Long startTime;
    private Long endTime;
    private boolean isForMessageList;
    private ConversationListHandler handler;
    private KommunicateException exception;
    private AppContactService appContactService;
    private ChannelService channelService;
    private MessageDatabaseService messageDatabaseService;

    public ConversationListTask(Context context, String searchString, Contact contact, Channel channel, Long startTime, Long endTime, ConversationListHandler handler, boolean isForMessageList) {
        this.context = new WeakReference<Context>(context);
        this.searchString = searchString;
        this.contact = contact;
        this.channel = channel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.handler = handler;
        this.isForMessageList = isForMessageList;
        channelService = ChannelService.getInstance(this.context.get());
        appContactService = new AppContactService(this.context.get());
        messageDatabaseService = new MessageDatabaseService(this.context.get());
    }

    @Override
    protected List<AlConversation> doInBackground() {
        List<Message> messageList = null;

        try {
            if (isForMessageList) {
                messageList = new MobiComConversationService(context.get()).getLatestMessagesGroupByPeople(startTime, TextUtils.isEmpty(searchString) ? null : searchString);
            } else {
                messageList = new MobiComConversationService(context.get()).getMessages(startTime, endTime, contact, channel, null);
            }

            if (messageList == null && exception == null) {
                exception = new KommunicateException("Some internal error occurred");
            }

            List<String> recList = new ArrayList<String>();
            List<AlConversation> conversationList = new ArrayList<AlConversation>();

            if (isForMessageList) {
                if (messageList != null) {
                    for (Message message : messageList) {
                        AlConversation conversation = new AlConversation();

                        if ((message.getGroupId() == null || message.getGroupId() == 0) && !recList.contains(message.getContactIds())) {
                            recList.add(message.getContactIds());

                            conversation.setMessage(message);
                            conversation.setContact(appContactService.getContactById(message.getContactIds()));
                            conversation.setChannel(null);
                            conversation.setUnreadCount(messageDatabaseService.getUnreadMessageCountForContact(message.getContactIds()));
                            conversationList.add(conversation);
                        } else if (message.getGroupId() != null && !recList.contains("group" + message.getGroupId())) {
                            recList.add("group" + message.getGroupId());

                            conversation.setMessage(message);
                            conversation.setContact(null);
                            conversation.setChannel(channelService.getChannel(message.getGroupId()));
                            conversation.setUnreadCount(messageDatabaseService.getUnreadMessageCountForChannel(message.getGroupId()));
                            conversationList.add(conversation);
                        }
                    }
                    if (!messageList.isEmpty()) {
                        MobiComUserPreference.getInstance(context.get()).setStartTimeForPagination(messageList.get(messageList.size() - 1).getCreatedAtTime());
                    }
                    return conversationList;
                }
            }
        } catch (Exception e) {
            exception = new KommunicateException(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<AlConversation> conversationList) {
        super.onPostExecute(conversationList);

        handler.onResult(context.get(), conversationList, exception);
    }
}
