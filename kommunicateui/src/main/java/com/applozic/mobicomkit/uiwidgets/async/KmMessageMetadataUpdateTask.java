package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.feed.ApiResponse;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by reytum on 17/11/17.
 */

public class KmMessageMetadataUpdateTask extends AsyncTask<Void, Void, ApiResponse> {

    private WeakReference<Context> context;
    private String key;
    private Map<String, String> metadata;
    private MessageMetadataListener listener;

    public KmMessageMetadataUpdateTask(Context context, String key, Map<String, String> metadata, MessageMetadataListener listener) {
        this.context = new WeakReference<Context>(context);
        this.key = key;
        this.metadata = metadata;
        this.listener = listener;
    }

    @Override
    protected ApiResponse doInBackground(Void... voids) {
        return new MobiComMessageService(context.get(), MessageIntentService.class).getUpdateMessageMetadata(key, metadata);
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);

        if (apiResponse == null) {
            listener.onFailure(context.get(), "Some error occurred");
        } else if (!"success".equals(apiResponse.getStatus()) && apiResponse.getErrorResponse() != null) {
            listener.onFailure(context.get(), apiResponse.getErrorResponse().toString());
        } else if ("success".equals(apiResponse.getStatus())) {
            new MessageDatabaseService(context.get()).updateMessageMetadata(key, metadata);
            listener.onSuccess(context.get(), "Metadata updated successfully for messsage key : " + key);
        }
    }

    public interface MessageMetadataListener {

        void onSuccess(Context context, String message);

        void onFailure(Context context, String error);
    }
}
