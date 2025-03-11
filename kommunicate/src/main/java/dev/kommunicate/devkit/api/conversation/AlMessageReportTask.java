package dev.kommunicate.devkit.api.conversation;

import dev.kommunicate.devkit.feed.ApiResponse;
import dev.kommunicate.devkit.listners.AlCallback;
import dev.kommunicate.commons.json.GsonUtils;
import dev.kommunicate.commons.task.AlAsyncTask;

public class AlMessageReportTask extends AlAsyncTask<Void, String> {

    private String messageKey;
    private MobiComConversationService conversationService;
    private AlCallback alCallback;
    private static final String error = "error";

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
                    alCallback.onError(error);
                }
            } else {
                alCallback.onError(error);
            }
        }
    }
}
