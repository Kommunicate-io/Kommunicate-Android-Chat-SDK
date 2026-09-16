package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.CoreJobIntentService;
import androidx.core.content.IntentCompat;

import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.sentry.Sentry;

/**
 * Created by devashish on 15/12/13.
 */
public class ConversationIntentService extends CoreJobIntentService {

    public static final String SYNC = "AL_SYNC";
    public static final String AL_MESSAGE = "AL_MESSAGE";
    public static final String AL_MESSAGE_JSON = "AL_MESSAGE_JSON";
    private static final String TAG = "ConversationIntent";
    public static final String MESSAGE_METADATA_UPDATE = "MessageMetadataUpdate";
    public static final String MUTED_USER_LIST_SYNC = "MutedUserListSync";
    private static final int PRE_FETCH_MESSAGES_FOR = 6;
    private MobiComMessageService mobiComMessageService;


    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(AppContextService.getContext(context), ConversationIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent == null) {
            return;
        }
        if (mobiComMessageService == null) {
            mobiComMessageService = new MobiComMessageService(this, MessageIntentService.class);
        }
        boolean sync = intent.getBooleanExtra(SYNC, false);
        Utils.printLog(ConversationIntentService.this, TAG, "Syncing messages service started: " + sync);
        boolean metadataSync = intent.getBooleanExtra(MESSAGE_METADATA_UPDATE, false);
        boolean mutedUserListSync = intent.getBooleanExtra(MUTED_USER_LIST_SYNC, false);

        if (mutedUserListSync) {
            Utils.printLog(ConversationIntentService.this, TAG, "Muted user list sync started..");
            try {
                Thread thread = new Thread(new MutedUserListSync());
                thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (metadataSync) {
            Utils.printLog(ConversationIntentService.this, TAG, "Syncing messages service started for metadata update");
            mobiComMessageService.syncMessageForMetadataUpdate();
            return;
        }

        Message message = getMessage(intent);

        if (message != null) {
            mobiComMessageService.processInstantMessage(message);
        } else {
            if (sync) {
                mobiComMessageService.syncMessages();
            }
        }
    }

    /**
     * New work is transported as JSON so changes to Message's Parcelable layout cannot make
     * queued work unreadable after an SDK update. The legacy readers allow already queued work
     * from older SDK versions to finish.
     */
    private Message getMessage(Intent intent) {
        try {
            String messageJson = intent.getStringExtra(AL_MESSAGE_JSON);
            if (!TextUtils.isEmpty(messageJson)) {
                return GsonUtils.getObjectFromJson(messageJson, Message.class);
            }

            Message parcelableMessage = IntentCompat.getParcelableExtra(intent, AL_MESSAGE, Message.class);
            if (parcelableMessage != null) {
                return parcelableMessage;
            }

            return IntentCompat.getSerializableExtra(intent, AL_MESSAGE, Message.class);
        } catch (RuntimeException exception) {
            // BadParcelableException can be thrown by work queued with an incompatible Message
            // parcel layout. Returning null lets onHandleWork perform the normal full sync.
            Utils.printLog(this, TAG, "Unable to read queued message; falling back to full sync: " + exception.getMessage());
            Sentry.captureException(exception);
            return null;
        }
    }

    private class MutedUserListSync implements Runnable {
        @Override
        public void run() {
            try {
                UserService.getInstance(ConversationIntentService.this).processSyncUserBlock();
                UserService.getInstance(ConversationIntentService.this).getMutedUserList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
