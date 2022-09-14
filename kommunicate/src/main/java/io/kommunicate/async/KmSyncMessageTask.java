package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;

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
            if (!Utils.isInternetAvailable(ApplozicService.getContextFromWeak(context))) {
                return null;
            }
            SyncCallService.getInstance(ApplozicService.getContextFromWeak(context)).syncMessages(null);
            if(metadataUpdate) {
                SyncCallService.getInstance(ApplozicService.getContextFromWeak(context)).syncMessageMetadata();
            }
        } catch (Exception e) {
            Utils.printLog(ApplozicService.getContextFromWeak(context), TAG, e.getMessage());
        }
        return null;
    }
}
