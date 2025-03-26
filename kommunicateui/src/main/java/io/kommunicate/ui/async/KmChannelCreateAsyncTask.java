package io.kommunicate.ui.async;

import android.content.Context;
import android.os.AsyncTask;

import io.kommunicate.devkit.api.people.ChannelInfo;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.feed.ChannelFeedApiResponse;
import io.kommunicate.commons.people.channel.Channel;

/**
 * Created by Sunil on 12/26/2016.
 */

public class KmChannelCreateAsyncTask extends AsyncTask<Void, Void, ChannelFeedApiResponse> {
    Context context;
    ChannelService channelService;
    ChannelInfo channelInfo;
    TaskListenerInterface taskListenerInterface;

    public KmChannelCreateAsyncTask(Context context, ChannelInfo channelInfo, TaskListenerInterface taskListenerInterface) {
        this.context = context;
        this.taskListenerInterface = taskListenerInterface;
        this.channelInfo = channelInfo;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected ChannelFeedApiResponse doInBackground(Void... voids) {
        if (channelInfo != null) {
            return channelService.createChannelWithResponse(channelInfo);
        }
        return null;
    }

    @Override
    protected void onPostExecute(ChannelFeedApiResponse channelFeedApiResponse) {
        super.onPostExecute(channelFeedApiResponse);
        if (channelFeedApiResponse != null) {
            if (channelFeedApiResponse.isSuccess()) {
                taskListenerInterface.onSuccess(channelService.getChannel(channelFeedApiResponse.getResponse()), context);
            } else {
                taskListenerInterface.onFailure(channelFeedApiResponse, context);
            }
        } else {
            taskListenerInterface.onFailure(null, context);
        }
    }

    public interface TaskListenerInterface {
        void onSuccess(Channel channel, Context context);

        void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context);
    }
}
