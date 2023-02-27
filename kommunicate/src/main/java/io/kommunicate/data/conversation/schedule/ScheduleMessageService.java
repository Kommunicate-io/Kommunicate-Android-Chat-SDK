package io.kommunicate.data.conversation.schedule;

import android.app.IntentService;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.data.conversation.MessageIntentService;
import io.kommunicate.data.conversation.MobiComConversationService;
import io.kommunicate.data.conversation.database.MessageDatabaseService;

public class ScheduleMessageService extends IntentService {

    public ScheduleMessageService() {
        super("MobiTexter Message Scheduler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        MessageDatabaseService messageDatabaseService = new MessageDatabaseService(getApplicationContext());
        MobiComConversationService conversationService = new MobiComConversationService(getApplicationContext());
        List<Message> messages = messageDatabaseService.getScheduledMessages(time);
        for (Message message : messages) {
            message.setScheduledAt(null);
            conversationService.sendMessage(message, MessageIntentService.class);
            //Todo: broadcast for scheduled message fragment.
        }
        messageDatabaseService.deleteScheduledMessages(time);
    }

}
