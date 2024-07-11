package io.kommunicate.async;

import android.content.Context;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

public class KmUpdateConversationTask extends AlAsyncTask<Void, String> {

    private WeakReference<Context> context;
    private GroupInfoUpdate groupInfoUpdate;
    private KmConversationUpdateListener listener;
    private static final String SUCCESS = "success";

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
