package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.CoreJobIntentService;

import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.Utils;

/**
 * Created by sunil on 26/12/15.
 */
public class ChatIntentService extends CoreJobIntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    private MessageClientService messageClientService;
    public static final String AL_SYNC_ON_CONNECTIVITY = "AL_SYNC_ON_CONNECTIVITY";
    public static final String AL_TIME_CHANGE_RECEIVER = "AL_TIME_CHANGE_RECEIVER";
    MobiComConversationService conversationService;

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1010;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(AppContextService.getContext(context), ChatIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.messageClientService = new MessageClientService(this);
        this.conversationService = new MobiComConversationService(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        // If the service is started by the system before a user has logged in,
        // the SDK won't have the necessary credentials. Abort early to prevent crashes.
        if (!MobiComUserPreference.getInstance(this).isLoggedIn()) {
            Utils.printLog(this, "ChatIntentService", "Service triggered, but user is not logged in. Aborting work.");
            return;
        }

        boolean connectivityChange = intent.getBooleanExtra(AL_SYNC_ON_CONNECTIVITY, false);
        boolean timeChangeReceiver = intent.getBooleanExtra(AL_TIME_CHANGE_RECEIVER, false);

        if (connectivityChange) {
            SyncCallService.getInstance(ChatIntentService.this).syncMessages(null);
            messageClientService.syncPendingMessages(true);
            messageClientService.syncDeleteMessages(true);
            conversationService.processLastSeenAtStatus();
            UserService.getInstance(ChatIntentService.this).processSyncUserBlock();
        }

        if (timeChangeReceiver) {
            Utils.printLog(this, "TimeChange", "This service has been called on date change");
            long diff = DateUtils.getTimeDiffFromUtc();
            MobiComUserPreference.getInstance(this).setDeviceTimeOffset(diff);
        }
    }
}

