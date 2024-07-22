package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import io.kommunicate.services.KmUserClientService;

/**
 * Created by ashish on 13/02/18.
 */

public class KmUserPasswordResetTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;
    private String userId;
    private String applicationId;
    private KmPassResetHandler handler;

    public KmUserPasswordResetTask(Context context, String userId, String applicationId, KmPassResetHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.userId = userId;
        this.applicationId = applicationId;
        this.handler = handler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return new KmUserClientService(context.get()).resetUserPassword(userId, applicationId);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (handler != null) {
            if (s != null) {
                handler.onSuccess(context.get(), s);
            } else {
                handler.onFailure(context.get(), "Some error occurred");
            }
        }
    }

    public interface KmPassResetHandler {
        void onSuccess(Context context, String response);

        void onFailure(Context context, String error);
    }
}
