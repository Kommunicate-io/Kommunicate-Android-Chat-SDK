package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmHttpClient;

public class KmPostDataAsyncTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;
    private KmCallback callback;
    private String contentType;
    private String data;
    private String accept;
    private String url;
    private Exception exception = null;

    public KmPostDataAsyncTask(Context context, String url, String accept, String contentType, String data, KmCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
        this.url = url;
        this.contentType = contentType;
        this.data = data;
        this.accept = accept;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return new KmHttpClient(context.get()).postData(url, contentType, accept, data);
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
