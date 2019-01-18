package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicommons.json.GsonUtils;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAgentModel;
import io.kommunicate.services.KmUserService;

public class KmGetAgentListTask extends AsyncTask<Void, Void, KmAgentModel> {

    private String appKey;
    private KmUserService userService;
    private Exception exception;
    private KmCallback callback;

    public KmGetAgentListTask(Context context, String appKey, KmCallback callback) {
        this.appKey = appKey;
        this.callback = callback;
        userService = new KmUserService(new WeakReference<>(context).get());
    }

    @Override
    protected KmAgentModel doInBackground(Void... voids) {
        try {
            return (KmAgentModel) GsonUtils.getObjectFromJson(userService.getAgentList(appKey), KmAgentModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(KmAgentModel agentModel) {
        if (callback != null) {
            if (exception != null) {
                callback.onFailure(exception);
            } else if (agentModel != null) {
                if (KmAgentModel.SUCCESS.equals(agentModel.getCode())) {
                    callback.onSuccess(agentModel.getResponse());
                } else {
                    callback.onFailure(agentModel);
                }
            }
        }
    }
}
