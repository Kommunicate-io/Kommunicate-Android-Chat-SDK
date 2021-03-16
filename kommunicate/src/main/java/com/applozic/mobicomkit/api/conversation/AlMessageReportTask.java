package com.applozic.mobicomkit.api.conversation;

import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.task.AlAsyncTask;

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
