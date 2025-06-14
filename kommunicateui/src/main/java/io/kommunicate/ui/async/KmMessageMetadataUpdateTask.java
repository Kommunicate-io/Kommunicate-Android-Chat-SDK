package io.kommunicate.ui.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.api.conversation.MessageIntentService;
import io.kommunicate.devkit.api.conversation.MobiComMessageService;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.feed.ApiResponse;

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
    private static final String ERR_OCCURRED = "Some Error occurred";
    private static final String METADATA_UPDATED_SUCCESS = "Metadata updated successfully for messsage key : ";
    private static final String SUCCESS = "success";

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
            listener.onFailure(context.get(), ERR_OCCURRED);
        } else if (!SUCCESS.equals(apiResponse.getStatus()) && apiResponse.getErrorResponse() != null) {
            listener.onFailure(context.get(), apiResponse.getErrorResponse().toString());
        } else if (SUCCESS.equals(apiResponse.getStatus())) {
            new MessageDatabaseService(context.get()).updateMessageMetadata(key, metadata);
            listener.onSuccess(context.get(), METADATA_UPDATED_SUCCESS + key);
        }
    }

    public interface MessageMetadataListener {

        void onSuccess(Context context, String message);

        void onFailure(Context context, String error);
    }
}
