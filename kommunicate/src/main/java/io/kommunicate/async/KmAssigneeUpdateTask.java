package io.kommunicate.async;

import android.text.TextUtils;

import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.task.AlAsyncTask;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmClientService;

public class KmAssigneeUpdateTask extends AlAsyncTask<Void, String> {
    private Integer groupId;
    private String assigneeId;
    private boolean switchAssignee;
    private boolean sendNotifyMessage;
    private boolean takeOverFromBot;
    private KmCallback callback;
    private KmClientService clientService;

    public KmAssigneeUpdateTask(Integer conversationId, String assigneeId, KmCallback callback) {
        this(conversationId, assigneeId, true, true, true, callback);
    }

    public KmAssigneeUpdateTask(Integer groupId, String assigneeId, boolean switchAssignee, boolean sendNotifyMessage, boolean takeOverFromBot, KmCallback callback) {
        this.groupId = groupId;
        this.assigneeId = assigneeId;
        this.switchAssignee = switchAssignee;
        this.sendNotifyMessage = sendNotifyMessage;
        this.takeOverFromBot = takeOverFromBot;
        this.callback = callback;
        this.clientService = new KmClientService(ApplozicService.getAppContext());
    }

    @Override
    protected String doInBackground() throws Exception {
        return clientService.switchConversationAssignee(groupId, assigneeId, switchAssignee, sendNotifyMessage, takeOverFromBot);
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
                    callback.onFailure("Failed to update Assignee");
                }
            } else {
                callback.onFailure("Failed to update Assignee");
            }
        }
        super.onPostExecute(s);
    }
}
