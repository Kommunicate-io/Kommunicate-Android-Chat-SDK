package com.applozic.mobicomkit.uiwidgets.kommunicate;

import android.content.Context;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.uiwidgets.kommunicate.asyncs.KmAwayMessageTask;
import com.applozic.mobicomkit.uiwidgets.kommunicate.asyncs.KmConversationFeedbackTask;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmAwayMessageHandler;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmFeedbackCallback;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmFeedback;

/**
 * Created by ashish on 03/04/18.
 * updated by shubham on 07/19 (feedback)
 */

public class KommunicateUI {

    public static final String CONVERSATION_ASSIGNEE = "CONVERSATION_ASSIGNEE";
    public static final String KM_CONVERSATION_TITLE = "KM_CONVERSATION_TITLE";
    public static final String KM_HELPCENTER_URL = "KM_HELPCENTER_URL";

    public static void getAwayMessage(Context context, String appKey, Integer groupId, KmAwayMessageHandler handler) {
        new KmAwayMessageTask(context, appKey, groupId, handler).execute();
    }

    public static void getAwayMessage(Context context, Integer groupId, KmAwayMessageHandler handler) {
        getAwayMessage(context, Applozic.getInstance(context).getApplicationKey(), groupId, handler);
    }

    /**
     * This method will get the conversation feedback using a async task for the given conversation id
     * @param context the contet
     * @param conversationId of which we need to get the feed back
     * @param kmFeedbackCallback the callback with the onSuccess and onFailure
     */
    public static void getConversationFeedback(Context context, String conversationId, KmFeedbackCallback kmFeedbackCallback) {
        KmConversationFeedbackTask kmConversationFeedbackTask = new KmConversationFeedbackTask(context, conversationId, null, kmFeedbackCallback);
        kmConversationFeedbackTask.execute();
    }

    /**
     * this method will set the conversation feedback using a async task from the given KmFeedback object
     * @param context the context
     * @param kmFeedback will have the feedback and the conversation id of the conversation
     * @param kmFeedbackCallback the callback with the onSuccess and onFailure1
     */
    public static void setConversationFeedback(Context context, KmFeedback kmFeedback, KmFeedbackCallback kmFeedbackCallback) {
        KmConversationFeedbackTask kmConversationFeedbackTask = new KmConversationFeedbackTask(context, null, kmFeedback, kmFeedbackCallback);
        kmConversationFeedbackTask.execute();
    }
}
