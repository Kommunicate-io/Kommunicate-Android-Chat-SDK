package io.kommunicate.data.conversation;

import io.kommunicate.callbacks.AlCallback;
import io.kommunicate.data.json.GsonUtils;
import io.kommunicate.data.async.task.AlAsyncTask;
import io.kommunicate.models.feed.ApiResponse;

public class AlMessageReportTask extends AlAsyncTask<Void, String> {

    private String messageKey;
    private MobiComConversationService conversationService;
    private AlCallback alCallback;


    public AlMessageReportTask(String messageKey, MobiComConversationService conversationService, AlCallback alCallback) {
        this.messageKey = messageKey;
        this.conversationService = conversationService;
        this.alCallback = alCallback;
    }

    @Override
    protected String doInBackground() {
        return conversationService.reportMessage(messageKey);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (alCallback != null) {
            ApiResponse<String> response = (ApiResponse<String>) GsonUtils.getObjectFromJson(s, ApiResponse.class);
            if (response != null) {
                if (response.isSuccess()) {
                    alCallback.onSuccess(response.getResponse());
                } else {
                    alCallback.onError("error");
                }
            } else {
                alCallback.onError("error");
            }
        }
    }
}
