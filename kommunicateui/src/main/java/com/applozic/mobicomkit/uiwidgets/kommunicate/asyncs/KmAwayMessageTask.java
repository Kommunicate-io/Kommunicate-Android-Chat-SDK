package com.applozic.mobicomkit.uiwidgets.kommunicate.asyncs;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmAwayMessageHandler;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmAwayMessageResponse;
import com.applozic.mobicomkit.uiwidgets.kommunicate.services.KmService;
import com.applozic.mobicommons.json.GsonUtils;

import java.lang.ref.WeakReference;

/**
 * Created by ashish on 03/04/18.
 */

public class KmAwayMessageTask extends AsyncTask<Void, Void, String> {

    WeakReference<Context> context;
    String appKey;
    Integer groupId;
    KmAwayMessageHandler handler;
    Exception exception;

    public KmAwayMessageTask(Context context, String appKey, Integer groupId, KmAwayMessageHandler handler) {
        this.context = new WeakReference<Context>(context);
        this.appKey = appKey;
        this.groupId = groupId;
        this.handler = handler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return new KmService(context.get()).getAwayMessage(appKey, groupId);
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        KmAwayMessageResponse response = null;

        if (s != null) {
            try {
                response = (KmAwayMessageResponse) GsonUtils.getObjectFromJson(s, KmAwayMessageResponse.class);
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
