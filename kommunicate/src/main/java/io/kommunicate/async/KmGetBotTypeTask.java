package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import io.kommunicate.commons.json.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;

import annotations.CleanUpRequired;
import io.kommunicate.preference.KmBotPreference;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.MessageTypeKmApiResponse;
import io.kommunicate.services.KmUserService;

@Deprecated
@CleanUpRequired(reason = "Migrated KmGetBotTypeTask to GetBotTypeUseCase")
public class KmGetBotTypeTask extends AsyncTask<Void, Void, String> {

    private static final String RESPONSE_STRING_NULL = "Response string for bot details null.";
    private String botId;
    private KmUserService userService;
    private KmCallback callback;
    private WeakReference<Context> weakReference;

    public KmGetBotTypeTask(Context context, String botId, KmCallback callback) {
        this.botId = botId;
        this.callback = callback;
        weakReference = new WeakReference<>(context);
        userService = new KmUserService(weakReference.get());
    }

    @Override
    protected String doInBackground(Void... voids) {
        return userService.getBotDetailResponse(botId);
    }

    @Override
    protected void onPostExecute(String response) {
        if (callback != null) {
            if (TextUtils.isEmpty(response)) {
               callback.onFailure(RESPONSE_STRING_NULL);
            } else {
                Type responseClassType = new TypeToken<MessageTypeKmApiResponse<List<BotDetailsResponseData>>>() { }.getType();
                try {
                    MessageTypeKmApiResponse<List<BotDetailsResponseData>> responseObject = (MessageTypeKmApiResponse<List<BotDetailsResponseData>>) GsonUtils.getObjectFromJson(response, responseClassType);
                    String botType = responseObject.getData().get(0).getAiPlatform();
                    if(!TextUtils.isEmpty(botType)) {
                        //add to local shared preference
                        KmBotPreference.getInstance(weakReference.get()).addBotType(botId, botType);
                        callback.onSuccess(botType);
                    }
                } catch (Exception exception) {
                    callback.onFailure(exception.getMessage());
                }
            }
        }
    }

    @Deprecated
    @CleanUpRequired(reason = "Migrated BotDetailsResponseData to GetBotTypeUseCase.BotDetailsResponseData")
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
