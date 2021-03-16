package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by reytum on 27/11/17.
 */

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
                exception = new ApplozicException("Some internal error occurred");
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
}