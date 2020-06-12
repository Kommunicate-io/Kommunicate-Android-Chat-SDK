package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.MessageTypeKmApiResponse;
import io.kommunicate.services.KmUserService;

public class KmGetBotTypeTask extends AsyncTask<Void, Void, String> {

    private String applicationId;
    private String botId;
    private KmUserService userService;
    private KmCallback callback;

    public KmGetBotTypeTask(Context context, String applicationId, String botId, KmCallback callback) {
        this.applicationId = applicationId;
        this.botId = botId;
        this.callback = callback;
        userService = new KmUserService(new WeakReference<>(context).get());
    }

    @Override
    protected String doInBackground(Void... voids) {
        return userService.getBotDetailResponse(applicationId, botId);
    }

    @Override
    protected void onPostExecute(String response) {
        if (callback != null) {
            if (TextUtils.isEmpty(response)) {
               callback.onFailure("Response string for bot details null.");
            } else {
                Type responseClassType = new TypeToken<MessageTypeKmApiResponse<List<BotDetailsResponseData>>>() { }.getType();
                try {
                    MessageTypeKmApiResponse<List<BotDetailsResponseData>> responseObject = (MessageTypeKmApiResponse<List<BotDetailsResponseData>>) GsonUtils.getObjectFromJson(response, responseClassType);
                    callback.onSuccess(responseObject.getData().get(0).getAiPlatform());
                } catch (Exception exception) {
                    callback.onFailure(exception.getMessage());
                }
            }
        }
    }

    public static class BotDetailsResponseData {
        public static final String PLATFORM_DIALOG_FLOW = "dialogflow";

        private String aiPlatform;

        public String getAiPlatform() {
            return aiPlatform;
        }

        public void setAiPlatform(String aiPlatform) {
            this.aiPlatform = aiPlatform;
        }
    }
}
