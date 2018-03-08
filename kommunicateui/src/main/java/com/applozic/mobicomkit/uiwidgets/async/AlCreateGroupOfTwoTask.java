package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.people.channel.Channel;

/**
 * Created by reytum on 6/10/17.
 */

public class AlCreateGroupOfTwoTask extends AsyncTask<Void, Void, Channel> {
    Context context;
    ChannelService channelService;
    ChannelInfo channelInfo;
    TaskListenerInterface taskListenerInterface;

    public AlCreateGroupOfTwoTask(Context context, ChannelInfo channelInfo, TaskListenerInterface taskListenerInterface) {
        this.context = context;
        this.taskListenerInterface = taskListenerInterface;
        this.channelInfo = channelInfo;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected Channel doInBackground(Void[] params) {
        if (channelInfo != null) {
            return channelService.createGroupOfTwo(channelInfo);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Channel channel) {
        super.onPostExecute(channel);
        if (channel != null) {
            taskListenerInterface.onSuccess(channel, context);
        } else {
            taskListenerInterface.onFailure("Some error occured", context);
        }
    }

    public interface TaskListenerInterface {
        void onSuccess(Channel channel, Context context);

        void onFailure(String error, Context context);
    }
}
