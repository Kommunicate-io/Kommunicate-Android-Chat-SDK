package com.applozic.mobicomkit.uiwidgets.notification;

/**
 * Created by Rahul-PC on 21-08-2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.notification.NotificationIntentService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class PushNotificationJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Bundle bundle = params.getExtras();
        try {
            String messageKey = bundle.getString(MobiComKitConstants.AL_MESSAGE_KEY);
            if (!TextUtils.isEmpty(messageKey)) {
                Intent notificationIntentService = new Intent(PushNotificationJobService.this, NotificationIntentService.class);
                notificationIntentService.setAction(NotificationIntentService.ACTION_AL_NOTIFICATION);
                notificationIntentService.putExtra(MobiComKitConstants.AL_MESSAGE_KEY, messageKey);
                NotificationIntentService.enqueueWork(PushNotificationJobService.this, notificationIntentService, R.drawable.mobicom_ic_launcher, R.string.wearable_action_label, R.string.wearable_action_title, R.drawable.mobicom_ic_action_send);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}
