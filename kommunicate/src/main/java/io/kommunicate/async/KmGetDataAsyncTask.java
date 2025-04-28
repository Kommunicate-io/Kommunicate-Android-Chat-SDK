package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Map;

import annotations.CleanUpRequired;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmHttpClient;

@Deprecated
@CleanUpRequired(reason = "Migrated KmGetDataAsyncTask to GetDataUseCase")
public class KmGetDataAsyncTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;
    private KmCallback callback;
    private String contentType;
    private String data;
    private String accept;
    private String url;
    private Exception exception = null;
    private Map<String, String> headers;

    public KmGetDataAsyncTask(Context context, String url, String accept, String contentType, String data, Map<String, String> headers, KmCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
        this.url = url;
        this.contentType = contentType;
        this.data = data;
        this.accept = accept;
        this.headers = headers;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            if(!TextUtils.isEmpty(data)) {
                url = url + (URLEncoder.encode(data, "UTF-8")).trim();
            }
            return new KmHttpClient(context.get()).getResponseWithException(url, contentType, accept, headers);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        if (callback != null) {
            if (response != null) {
                callback.onSuccess(response);
            } else {
                callback.onFailure(exception);
            }
        }
        super.onPostExecute(response);
    }
}
