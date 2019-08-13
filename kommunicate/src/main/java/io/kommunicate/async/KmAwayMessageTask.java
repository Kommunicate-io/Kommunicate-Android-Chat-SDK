package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.models.KmApiResponse;
import io.kommunicate.services.KmService;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

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

        KmApiResponse<KmApiResponse.KmDataResposne> response = null;

        if (s != null) {
            try {
                Type type = new TypeToken<KmApiResponse<KmApiResponse.KmDataResposne>>() {
                }.getType();
                response = new Gson().fromJson(s, type);
            } catch (Exception e) {
                handler.onFailure(context.get(), e, s);
            }

            if (response != null) {
                if ("SUCCESS".equals(response.getCode()) && !response.getData().getMessageList().isEmpty()) {
                    handler.onSuccess(context.get(), response.getData().getMessageList().get(0));
                } else {
                    handler.onFailure(context.get(), exception, s);
                }
            } else {
                handler.onFailure(context.get(), exception, s);
            }
        }
    }
}
