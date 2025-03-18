package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.AlJobIntentService;

import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.commons.ApplozicService;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.Utils;

/**
 * Created by sunil on 26/12/15.
 */
public class ApplozicIntentService extends AlJobIntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    private static final String TAG = "ApplozicIntentService";
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
        enqueueWork(ApplozicService.getContext(context), ApplozicIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.messageClientService = new MessageClientService(this);
        this.conversationService = new MobiComConversationService(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        boolean connectivityChange = intent.getBooleanExtra(AL_SYNC_ON_CONNECTIVITY, false);
        boolean timeChangeReceiver = intent.getBooleanExtra(AL_TIME_CHANGE_RECEIVER, false);

        if (connectivityChange) {
            SyncCallService.getInstance(ApplozicIntentService.this).syncMessages(null);
            messageClientService.syncPendingMessages(true);
            messageClientService.syncDeleteMessages(true);
            conversationService.processLastSeenAtStatus();
            UserService.getInstance(ApplozicIntentService.this).processSyncUserBlock();
        }

        if (timeChangeReceiver) {
            Utils.printLog(this, "TimeChange", "This service has been called on date change");
            long diff = DateUtils.getTimeDiffFromUtc();
            MobiComUserPreference.getInstance(this).setDeviceTimeOffset(diff);
        }
    }
}

