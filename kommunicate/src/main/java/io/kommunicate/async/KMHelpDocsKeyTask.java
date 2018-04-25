package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.json.GsonUtils;

import java.lang.ref.WeakReference;

import io.kommunicate.KmPreference;
import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.models.KmHelpDocKey;
import io.kommunicate.services.KmUserService;

/**
 * Created by ashish on 23/04/18.
 */

public class KMHelpDocsKeyTask extends AsyncTask<Void, Void, String> {
    private WeakReference<Context> context;
    private String type;
    private Exception exception;
    private KmFaqTaskListener listener;

    public KMHelpDocsKeyTask(Context context, String type, KmFaqTaskListener listener) {
        this.context = new WeakReference<Context>(context);
        this.type = type;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String helpDocsKey = KmPreference.getInstance(context.get()).getHelpDocsKey();

        if (helpDocsKey == null) {
            try {
                helpDocsKey = parseHelpDocsKey(new KmUserService(context.get()).getHelpDocsKey(MobiComKitClientService.getApplicationKey(context.get()), type));
            } catch (Exception e) {
                exception = e;
            }
        }
        return helpDocsKey;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (s != null) {
            listener.onSuccess(context.get(), s);
        } else {
            listener.onFailure(context.get(), exception, "Unable to get Access key");
        }
    }

    private String parseHelpDocsKey(String data) {
        try {
            KmHelpDocKey helpDocKey = (KmHelpDocKey) GsonUtils.getObjectFromJson(data, KmHelpDocKey.class);
            if (helpDocKey != null && "SUCCESS".equals(helpDocKey.getCode()) && !helpDocKey.getMessage().isEmpty()) {
                KmPreference.getInstance(context.get()).setHelpDocsKey(helpDocKey.getMessage().get(0).getAccessKey());
                return helpDocKey.getMessage().get(0).getAccessKey();
            }
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }
}
