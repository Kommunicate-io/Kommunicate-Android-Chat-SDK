package io.kommunicate.data.async;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.kommunicate.data.channel.service.ChannelService;
import io.kommunicate.data.async.task.AlAsyncTask;
import io.kommunicate.models.feed.GroupInfoUpdate;

public class KmUpdateConversationTask extends AlAsyncTask<Void, String> {

    private static final String SUCCESS = "success";
    private WeakReference<Context> context;
    private GroupInfoUpdate groupInfoUpdate;
    private KmConversationUpdateListener listener;

    public KmUpdateConversationTask(Context context, GroupInfoUpdate groupInfoUpdate, KmConversationUpdateListener listener) {
        this.context = new WeakReference<>(context);
        this.groupInfoUpdate = groupInfoUpdate;
        this.listener = listener;
    }

    @Override
    protected String doInBackground() {
        return ChannelService.getInstance(context.get()).updateChannel(groupInfoUpdate);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (listener != null) {
            if (SUCCESS.equals(s)) {
                listener.onSuccess(context.get());
            } else {
                listener.onFailure(context.get());
            }
        }
    }

    public interface KmConversationUpdateListener {
        void onSuccess(Context context);

        void onFailure(Context context);
    }
}
