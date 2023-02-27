package io.kommunicate.data.async;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import io.kommunicate.KommunicateService;
import io.kommunicate.data.conversation.SyncCallService;
import io.kommunicate.utils.Utils;

public class KmSyncMessageTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "KmSyncMessageTask";
    private WeakReference<Context> context;
    private boolean metadataUpdate;

    public KmSyncMessageTask(Context context, boolean isMessageMetadataUpdate) {
        this.context = new WeakReference<>(context);
        this.metadataUpdate = isMessageMetadataUpdate;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (!Utils.isInternetAvailable(KommunicateService.getContextFromWeak(context))) {
                return null;
            }
            SyncCallService.getInstance(KommunicateService.getContextFromWeak(context)).syncMessages(null);
            if (metadataUpdate) {
                SyncCallService.getInstance(KommunicateService.getContextFromWeak(context)).syncMessageMetadata();
            }
        } catch (Exception e) {
            Utils.printLog(KommunicateService.getContextFromWeak(context), TAG, e.getMessage());
        }
        return null;
    }
}
