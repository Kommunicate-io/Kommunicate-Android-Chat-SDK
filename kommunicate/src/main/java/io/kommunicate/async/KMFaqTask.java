package io.kommunicate.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;


import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.json.GsonUtils;

import java.lang.ref.WeakReference;

import io.kommunicate.callbacks.KmFaqTaskListener;
import io.kommunicate.models.KmArticleModel;
import io.kommunicate.services.KmUserService;

/**
 * Created by ashish on 20/04/18.
 * <p>
 * This is a combined Task for getting FAQ related data
 */

public class KMFaqTask extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> context;
    private boolean isArticleRequest = false;
    private boolean isSelectedArticleRequest = false;
    private boolean isAnswerRequest = false;
    private boolean isDashBoardFaqRequest = false;
    private String accessKey;
    private String data;
    private KmUserService kmUserService;
    private Exception exception;
    private KmFaqTaskListener listener;

    public KMFaqTask(Context context, String accessKey, String data, KmFaqTaskListener listener) {
        this.context = new WeakReference<Context>(context);
        this.accessKey = accessKey;
        this.data = data;
        this.listener = listener;

        kmUserService = new KmUserService(context);
    }

    public KMFaqTask forArticleRequest() {
        this.isArticleRequest = true;
        return this;
    }

    public KMFaqTask forSelectedArticles() {
        this.isSelectedArticleRequest = true;
        return this;
    }

    public KMFaqTask forAnswerRequest() {
        this.isAnswerRequest = true;
        return this;
    }

    public KMFaqTask forDashboardFaq() {
        this.isDashBoardFaqRequest = true;
        return this;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            if (!TextUtils.isEmpty(accessKey)) {
                if (isArticleRequest) {
                    return kmUserService.getArticleList(accessKey);
                } else if (isAnswerRequest && !TextUtils.isEmpty(data)) {
                    return kmUserService.getArticleAnswer(accessKey, data);
                } else if (isSelectedArticleRequest && !TextUtils.isEmpty(data)) {
                    return kmUserService.getSelectedArticles(accessKey, data);
                }
            }

            if (isDashBoardFaqRequest) {
                return kmUserService.getDashboardFaq(MobiComKitClientService.getApplicationKey(context.get()), data);
            }

        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (listener != null) {
            if (s == null) {
                listener.onFailure(context.get(), exception, null);
            } else {
                if (exception != null) {
                    listener.onFailure(context.get(), exception, s);
                } else {
                    try {
                        KmArticleModel model = (KmArticleModel) GsonUtils.getObjectFromJson(s, KmArticleModel.class);
                        if (model != null && (model.getArticles() != null || model.getArticle() != null || "SUCCESS".equals(model.getCode()))) {
                            listener.onSuccess(context.get(), s);
                        }
                    } catch (Exception e) {
                        listener.onFailure(context.get(), e, s);
                    }
                }
            }
        }
    }
}
