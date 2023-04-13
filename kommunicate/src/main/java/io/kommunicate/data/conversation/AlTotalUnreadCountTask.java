package io.kommunicate.data.conversation;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.kommunicate.KommunicateClient;
import io.kommunicate.KommunicateService;
import io.kommunicate.data.account.user.MobiComUserPreference;
import io.kommunicate.data.conversation.database.MessageDatabaseService;
import io.kommunicate.data.async.task.AlAsyncTask;
import io.kommunicate.utils.Utils;

public class AlTotalUnreadCountTask extends AlAsyncTask<Void, Integer> {

    private static final String TAG = "AlTotalUnreadCountTask";
    MessageDatabaseService messageDatabaseService;
    private TaskListener callback;
    private WeakReference<Context> weakReferenceContext;

    public AlTotalUnreadCountTask(Context context, TaskListener callback) {
        this.callback = callback;
        this.weakReferenceContext = new WeakReference<Context>(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
    }

    @Override
    protected Integer doInBackground() {
        try {
            // Call the List api method only if server call for list was not done before and return the unread count.
            if (!KommunicateClient.getInstance(KommunicateService.getContextFromWeak(weakReferenceContext)).wasServerCallDoneBefore(null, null, null)) {
                if (!Utils.isInternetAvailable(KommunicateService.getContextFromWeak(weakReferenceContext))) {
                    return null;
                }
                SyncCallService.getInstance(KommunicateService.getContextFromWeak(weakReferenceContext)).getLatestMessagesGroupByPeople(null, MobiComUserPreference.getInstance(KommunicateService.getContextFromWeak(weakReferenceContext)).getParentGroupKey());
            }
            return messageDatabaseService.getTotalUnreadCount();
        } catch (Exception e) {
            Utils.printLog(KommunicateService.getContextFromWeak(weakReferenceContext), TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer unreadCount) {
        super.onPostExecute(unreadCount);
        if (callback != null) {
            if (unreadCount != null) {
                callback.onSuccess(unreadCount);
            } else {
                callback.onFailure("Failed to fetch the unread count");
            }
        }
    }

    public interface TaskListener {
        void onSuccess(Integer unreadCount);

        void onFailure(String error);
    }
}
