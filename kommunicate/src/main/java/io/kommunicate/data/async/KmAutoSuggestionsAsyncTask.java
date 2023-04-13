package io.kommunicate.data.async;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import io.kommunicate.data.json.GsonUtils;
import io.kommunicate.data.services.KmService;
import io.kommunicate.models.KmApiResponse;
import io.kommunicate.models.KmAutoSuggestionModel;

public class KmAutoSuggestionsAsyncTask extends AsyncTask<Void, Void, KmApiResponse<List<KmAutoSuggestionModel>>> {

    private KmService kmService;
    private KmAutoSuggestionListener listener;

    public KmAutoSuggestionsAsyncTask(Context context, KmAutoSuggestionListener listener) {
        this.listener = listener;
        kmService = new KmService(context);
    }

    @Override
    protected KmApiResponse<List<KmAutoSuggestionModel>> doInBackground(Void... voids) {
        return kmService.getKmAutoSuggestions();
    }

    @Override
    protected void onPostExecute(KmApiResponse<List<KmAutoSuggestionModel>> apiResponse) {
        super.onPostExecute(apiResponse);

        if (listener != null) {
            if (apiResponse != null) {
                if (KmApiResponse.KM_AUTO_SUGGESSTION_SUCCESS_RESPONSE.equals(apiResponse.getCode())) {
                    listener.onSuccess(apiResponse.getData());
                } else {
                    listener.onFailure(apiResponse.getData() != null ? GsonUtils.getJsonFromObject(apiResponse.getData().toArray(), KmAutoSuggestionModel[].class) : "Some error occurred");
                }
            } else {
                listener.onFailure("Some error occurred");
            }
        }
    }

    public interface KmAutoSuggestionListener {
        void onSuccess(List<KmAutoSuggestionModel> autoSuggestionList);

        void onFailure(String error);
    }
}
