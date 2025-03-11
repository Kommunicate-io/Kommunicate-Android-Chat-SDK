package dev.kommunicate.devkit.api.conversation.schedule;

import android.app.IntentService;
import android.content.Intent;

import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.devkit.api.conversation.MessageIntentService;
import dev.kommunicate.devkit.api.conversation.MobiComConversationService;
import dev.kommunicate.devkit.api.conversation.database.MessageDatabaseService;

import java.util.Calendar;
import java.util.List;

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
