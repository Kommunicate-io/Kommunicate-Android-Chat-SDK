package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.utils.KmAppSettingPreferences;

public class KmAppSettingTask extends AsyncTask<Void, Void, KmAppSettingModel> {

    private WeakReference<Context> context;
    private String appId;
    private String error;
    private KmCallback callback;

    public KmAppSettingTask(Context context, String appId, KmCallback callback) {
        this.context = new WeakReference<>(context);
        this.appId = appId;
        this.callback = callback;
    }

    @Override
    protected KmAppSettingModel doInBackground(Void... voids) {
        try {
            KmAppSettingPreferences.getInstance().clearInstance();
            return KmAppSettingPreferences.fetchAppSetting(context.get(), appId);
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getLocalizedMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(KmAppSettingModel kmAppSettingModel) {
        super.onPostExecute(kmAppSettingModel);

        if (callback != null) {
            if (kmAppSettingModel != null) {
                if (kmAppSettingModel.isSuccess()) {
                    callback.onSuccess(kmAppSettingModel);
                } else {
                    callback.onFailure(error);
                }
            } else {
                callback.onFailure(error);
            }
        }
    }
}
