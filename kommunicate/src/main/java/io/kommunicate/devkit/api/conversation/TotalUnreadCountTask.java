package io.kommunicate.devkit.api.conversation;

import android.content.Context;

import annotations.CleanUpRequired;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.feed.ChannelFeed;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.task.CoreAsyncTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.ref.WeakReference;

@Deprecated
@CleanUpRequired(reason = "Not used anywhere")
public class TotalUnreadCountTask extends CoreAsyncTask<Void, Integer> {

    private static final String TAG = "AlTotalUnreadCountTask";
    private TaskListener callback;
    private WeakReference<Context> weakReferenceContext;
    MessageDatabaseService messageDatabaseService;
    private static final String groupFeeds = "groupFeeds";
    private static final String err_msg = "Failed to fetch the unread count";

    public TotalUnreadCountTask(Context context, TaskListener callback) {
        this.callback = callback;
        this.weakReferenceContext = new WeakReference<Context>(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
    }

    @Override
    protected Integer doInBackground() {
        try {
            String message = new MessageClientService(AppContextService.getContextFromWeak(weakReferenceContext)).getMessages(null,null,null,null,null,false);
            JsonObject messageObject = JsonParser.parseString(message).getAsJsonObject();
            if (messageObject.has(groupFeeds)) {
                String channelFeedResponse = messageObject.get(groupFeeds).toString();
                ChannelFeed[] channelFeeds = (ChannelFeed[]) GsonUtils.getObjectFromJson(channelFeedResponse, ChannelFeed[].class);
                int totalUnreadCount = 0;
                for (ChannelFeed channelFeed : channelFeeds){
                    totalUnreadCount += channelFeed.getUnreadCount();
                }
                return totalUnreadCount;
            }
            return messageDatabaseService.getTotalUnreadCount();
        } catch (Exception e) {
            Utils.printLog(AppContextService.getContextFromWeak(weakReferenceContext), TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer unreadCount) {
        super.onPostExecute(unreadCount);
        if (callback != null) {
            if (unreadCount != null) {
                callback.onSuccess(unreadCount);
            } else {
                callback.onFailure(err_msg);
            }
        }
    }

    public interface TaskListener {
        void onSuccess(Integer unreadCount);

        void onFailure(String error);
    }
}
