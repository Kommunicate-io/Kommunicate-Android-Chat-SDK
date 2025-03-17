package io.kommunicate.async;

import android.text.TextUtils;

import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.commons.ApplozicService;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.task.AlAsyncTask;

import annotations.CleanUpRequired;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.services.KmClientService;

@Deprecated
@CleanUpRequired(reason = "Migrated KmAssigneeUpdateTask to AssigneeUpdateUseCase")
public class KmAssigneeUpdateTask extends AlAsyncTask<Void, String> {
    private Integer groupId;
    private String assigneeId;
    private boolean switchAssignee;
    private boolean sendNotifyMessage;
    private boolean takeOverFromBot;
    private KmCallback callback;
    private KmClientService clientService;
    private static final String FAILURE_MSG = "Failed to update Assignee";

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
                    callback.onFailure(FAILURE_MSG);
                }
            } else {
                callback.onFailure(FAILURE_MSG);
            }
        }
        super.onPostExecute(s);
    }
}
