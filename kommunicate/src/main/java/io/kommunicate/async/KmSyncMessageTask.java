package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.api.conversation.SyncCallService;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;

import java.lang.ref.WeakReference;

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
            if (!Utils.isInternetAvailable(AppContextService.getContextFromWeak(context))) {
                return null;
            }
            SyncCallService.getInstance(AppContextService.getContextFromWeak(context)).syncMessages(null);
            if(metadataUpdate) {
                SyncCallService.getInstance(AppContextService.getContextFromWeak(context)).syncMessageMetadata();
            }
        } catch (Exception e) {
            Utils.printLog(AppContextService.getContextFromWeak(context), TAG, e.getMessage());
        }
        return null;
    }
}
