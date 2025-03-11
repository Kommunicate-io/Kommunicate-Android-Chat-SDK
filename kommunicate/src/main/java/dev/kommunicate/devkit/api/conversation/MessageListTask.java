package dev.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import dev.kommunicate.devkit.api.account.user.MobiComUserPreference;
import dev.kommunicate.devkit.exception.ApplozicException;
import dev.kommunicate.devkit.listners.MessageListHandler;
import dev.kommunicate.commons.commons.core.utils.DateUtils;
import dev.kommunicate.commons.people.channel.Channel;
import dev.kommunicate.commons.people.contact.Contact;
import dev.kommunicate.commons.task.AlAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import annotations.CleanUpRequired;

/**
 * Created by reytum on 27/11/17.
 */

@Deprecated
@CleanUpRequired(reason = "Migrated MessageListTask to MessageListUseCase")
public class MessageListTask extends AlAsyncTask<Void, List<Message>> {

    private WeakReference<Context> context;
    private Contact contact;
    private Channel channel;
    private Long startTime;
    private Long endTime;
    private MessageListHandler handler;
    private boolean isForMessageList;
    private ApplozicException exception;
    private String searchString;
    private static final String INTERNAL_ERR = "Some internal error occurred";

    public MessageListTask(Context context, String searchString, Contact contact, Channel channel, Long startTime, Long endTime, MessageListHandler handler, boolean isForMessageList) {
        this.context = new WeakReference<Context>(context);
        this.contact = contact;
        this.channel = channel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.handler = handler;
        this.isForMessageList = isForMessageList;
        this.searchString = searchString;
    }

    @Override
    protected List<Message> doInBackground() {
        List<Message> messageList = null;
        try {
            if (isForMessageList) {
                messageList = new MobiComConversationService(context.get()).getLatestMessagesGroupByPeople(startTime, TextUtils.isEmpty(searchString) ? null : searchString);
            } else {
                messageList = new MobiComConversationService(context.get()).getMessages(startTime, endTime, contact, channel, null);
            }

            if (messageList == null && exception == null) {
                exception = new ApplozicException(INTERNAL_ERR);
            }

            if (isForMessageList) {
                List<String> recList = new ArrayList<String>();
                List<Message> messages = new ArrayList<Message>();

                if (messageList != null) {
                    for (Message message : messageList) {
                        if ((message.getGroupId() == null || message.getGroupId() == 0) && !recList.contains(message.getContactIds())) {
                            recList.add(message.getContactIds());
                            messages.add(message);
                        } else if (message.getGroupId() != null && !recList.contains("group" + message.getGroupId())) {
                            recList.add("group" + message.getGroupId());
                            messages.add(message);
                        }
                    }
                    if (!messageList.isEmpty()) {
                        MobiComUserPreference.getInstance(context.get()).setStartTimeForPagination(messageList.get(messageList.size() - 1).getCreatedAtTime());
                    }
                    return messages;
                }
            } else {
                List<Message> mergedList = new ArrayList<>();

                if (messageList != null && !messageList.isEmpty()) {
                    mergedList.add(getInitialTopMessage());
                    mergedList.add(getDateMessage(messageList.get(0)));

                    for (int i = 0; i < messageList.size(); i++) {
                        if (i == 0) {
                            mergedList.add(messageList.get(0));
                            continue;
                        }

                        long dayDifference = DateUtils.daysBetween(new Date(messageList.get(i - 1).getCreatedAtTime()), new Date(messageList.get(i).getCreatedAtTime()));

                        if (dayDifference >= 1) {
                            Message message = getDateMessage(messageList.get(i));

                            if (!mergedList.contains(message)) {
                                mergedList.add(message);
                            }
                        }

                        if (!mergedList.contains(messageList.get(i))) {
                            mergedList.add(messageList.get(i));
                        }
                    }
                    return mergedList;
                }
            }
        } catch (Exception e) {
            exception = new ApplozicException(e.getMessage());
        }
        return messageList;
    }

    @Override
    protected void onPostExecute(List<Message> messageList) {
        super.onPostExecute(messageList);

        if (handler != null) {
            handler.onResult(messageList, exception);
        }
    }

    private Message getDateMessage(Message message) {
        Message firstDateMessage = new Message();
        firstDateMessage.setTempDateType(Short.valueOf("100"));
        firstDateMessage.setCreatedAtTime(message.getCreatedAtTime());
        return firstDateMessage;
    }

    private Message getInitialTopMessage() {
        Message firstMessage = new Message();
        firstMessage.setInitialFirstMessage();
        return firstMessage;
    }
}