package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.core.app.CoreJobIntentService;

import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.schedule.ScheduleMessageService;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devashish on 15/12/13.
 */
public class MessageIntentService extends CoreJobIntentService {

    private static final String TAG = "MessageIntentService";
    private MessageClientService messageClientService;
    private static Map<Long, Handler> uploadQueueMap = new HashMap<>();

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1111;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work, Handler handler) {
        enqueueWork(AppContextService.getContext(context), MessageIntentService.class, JOB_ID, work);
        if (work != null) {
            final Message message = (Message) GsonUtils.getObjectFromJson(work.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT), Message.class);
            if (uploadQueueMap != null && handler != null) {
                uploadQueueMap.put(message.getCreatedAtTime(), handler);
            }
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        messageClientService = new MessageClientService(MessageIntentService.this);
        try {
            final Message message = (Message) GsonUtils.getObjectFromJson(intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT), Message.class);
            Thread thread = new Thread(new MessageSender(message, uploadQueueMap.get(message.getCreatedAtTime()),intent.getStringExtra(MobiComKitConstants.DISPLAY_NAME)));
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MessageSender implements Runnable {
        private Message message;
        private Handler handler;
        private String userDisplayName;

        public MessageSender(Message message, Handler handler, String userDisplayName) {
            this.message = message;
            this.handler = handler;
            this.userDisplayName = userDisplayName;
        }

        @Override
        public void run() {
            try {
                messageClientService.sendMessageToServer(message, handler, ScheduleMessageService.class, userDisplayName);
                messageClientService.syncPendingMessages(true);
                uploadQueueMap.remove(message.getCreatedAtTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
