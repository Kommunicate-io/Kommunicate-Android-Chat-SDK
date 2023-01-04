package io.kommunicate.async;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmClientService;

public class KmStatusUpdateTask extends AsyncTask<Void, Void, String> {

    private Integer groupId;
    private int status;
    private boolean sendNotifyMessage;
    private KmCallback callback;
    private KmClientService kmClientService;

    public KmStatusUpdateTask(Integer groupId, int status, boolean sendNotifyMessage, KmCallback callback) {
        this.groupId = groupId;
        this.status = status;
        this.sendNotifyMessage = sendNotifyMessage;
        this.callback = callback;
        this.kmClientService = new KmClientService(ApplozicService.getAppContext());
    }

    @Override
    protected String doInBackground(Void... voids) {
        return kmClientService.changeConversationStatus(groupId, status, sendNotifyMessage);
    }

    @Override
    protected void onPostExecute(String s) {
        if (callback != null) {
            if (!TextUtils.isEmpty(s)) {
                ApiResponse<String> apiResponse = (ApiResponse<String>) GsonUtils.getObjectFromJson(s, ApiResponse.class);
                if (apiResponse != null) {
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getResponse());
                    } else {
                        callback.onFailure(apiResponse.getErrorResponse());
                    }
                } else {
                    callback.onFailure("Some error occurred");
                }
            } else {
                callback.onFailure("Some error occurred");
            }
        }
        super.onPostExecute(s);
    }
}