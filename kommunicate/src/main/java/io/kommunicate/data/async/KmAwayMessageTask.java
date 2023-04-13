package io.kommunicate.data.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.data.api.MobiComKitClientService;
import io.kommunicate.data.services.KmService;
import io.kommunicate.models.KmApiResponse;

/**
 * Created by ashish on 03/04/18.
 */

public class KmAwayMessageTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;
    private Integer groupId;
    private KmAwayMessageHandler handler;
    private Exception exception;

    public KmAwayMessageTask(Context context, Integer groupId, KmAwayMessageHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.groupId = groupId;
        this.handler = handler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return new KmService(context.get()).getAwayMessage(MobiComKitClientService.getApplicationKey(context.get()), groupId);
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        KmApiResponse<KmApiResponse.KmDataResponse> response = null;

        if (s != null) {
            try {
                Type type = new TypeToken<KmApiResponse<KmApiResponse.KmDataResponse>>() {
                }.getType();
                response = new Gson().fromJson(s, type);
            } catch (Exception e) {
                handler.onFailure(context.get(), e, s);
            }

            if (response != null) {
                if ("SUCCESS".equals(response.getCode())) {
                    handler.onSuccess(context.get(), response.getData());
                } else {
                    handler.onFailure(context.get(), exception, s);
                }
            } else {
                handler.onFailure(context.get(), exception, s);
            }
        }
    }
}
