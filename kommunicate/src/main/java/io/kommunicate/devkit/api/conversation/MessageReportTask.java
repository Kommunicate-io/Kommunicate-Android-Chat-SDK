package io.kommunicate.devkit.api.conversation;

import annotations.CleanUpRequired;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.task.CoreAsyncTask;

@Deprecated
@CleanUpRequired(reason = "Not used anywhere")
public class MessageReportTask extends CoreAsyncTask<Void, String> {

    private String messageKey;
    private MobiComConversationService conversationService;
    private ResultCallback resultCallback;
    private static final String error = "error";

    public MessageReportTask(String messageKey, MobiComConversationService conversationService, ResultCallback resultCallback) {
        this.messageKey = messageKey;
        this.conversationService = conversationService;
        this.resultCallback = resultCallback;
    }

    @Override
    protected String doInBackground() {
        return conversationService.reportMessage(messageKey);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (resultCallback != null) {
            ApiResponse<String> response = (ApiResponse<String>) GsonUtils.getObjectFromJson(s, ApiResponse.class);
            if (response != null) {
                if (response.isSuccess()) {
                    resultCallback.onSuccess(response.getResponse());
                } else {
                    resultCallback.onError(error);
                }
            } else {
                resultCallback.onError(error);
            }
        }
    }
}
