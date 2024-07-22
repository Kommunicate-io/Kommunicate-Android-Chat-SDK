package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

public class MessageDeleteTask extends AlAsyncTask<Void, String> {

    private WeakReference<Context> context;
    private String messageKey;
    private boolean deleteForAll;
    private Exception exception;
    private MobiComMessageService mobiComMessageService;
    private AlCallback callback;

    public MessageDeleteTask(Context context, String messageKey, boolean deleteForAll, AlCallback callback) {
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
                callback.onError(exception != null ? exception.getLocalizedMessage() : "Some internal error occurred");
            }
        }
    }
}
