package dev.kommunicate.devkit.api.conversation.selfdestruct;

import android.content.Context;
import android.util.Log;

import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.devkit.api.conversation.MobiComConversationService;
import dev.kommunicate.devkit.broadcast.BroadcastService;

import java.util.TimerTask;

public class DisappearingMessageTask extends TimerTask {
    private static final String TAG = "DisappearingMessageTask";

    private Context context;
    private MobiComConversationService conversationService;
    private Message message;

    public DisappearingMessageTask(Context context, MobiComConversationService conversationService, Message message) {
        this.context = context;
        this.conversationService = conversationService;
        this.message = message;
    }

    @Override
    public void run() {
        String smsKeyString = message.getKeyString();
        Log.i(TAG, "Self deleting message for keyString: " + smsKeyString);
        conversationService.deleteMessage(message);
        BroadcastService.sendMessageDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString(), smsKeyString, message.getContactIds(), message);
    }
}
