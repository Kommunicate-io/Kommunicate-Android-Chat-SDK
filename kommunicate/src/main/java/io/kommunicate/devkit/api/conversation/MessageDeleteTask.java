package io.kommunicate.devkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated MessageDeleteTask to MessageDeleteUseCase")
public class MessageDeleteTask extends CoreAsyncTask<Void, String> {

    private WeakReference<Context> context;
    private String messageKey;
    private boolean deleteForAll;
    private Exception exception;
    private MobiComMessageService mobiComMessageService;
    private ResultCallback callback;
    private static final String INTERNAL_ERR = "Some internal error occurred";

    public MessageDeleteTask(Context context, String messageKey, boolean deleteForAll, ResultCallback callback) {
        this.context = new WeakReference<>(context);
        this.messageKey = messageKey;
        this.deleteForAll = deleteForAll;
        this.callback = callback;
        this.mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);
    }

    @Override
    protected String doInBackground() {
        try {
            return mobiComMessageService.getMessageDeleteForAllResponse(messageKey, deleteForAll);
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (callback != null) {
            if (!TextUtils.isEmpty(response)) {
                callback.onSuccess(response);
            } else {
                callback.onError(exception != null ? exception.getLocalizedMessage() : INTERNAL_ERR);
            }
        }
    }
}
