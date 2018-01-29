package com.applozic.mobicomkit.uiwidgets.schedule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicommons.commons.core.utils.DateUtils;

/**
 * Created with IntelliJ IDEA.
 * User: shai
 * Date: 5/27/14
 * Time: 1:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConversationScheduler extends DialogFragment {
    private ScheduledTimeHolder scheduledTimeHolder;
    private LinearLayout conversationScheduler;
    private Button scheduleOption;
    private String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};

    public ConversationScheduler() {

    }

    public void setScheduleOption(Button scheduleOption) {
        this.scheduleOption = scheduleOption;
    }

    public void setScheduledTimeHolder(ScheduledTimeHolder scheduledTimeHolder) {
        this.scheduledTimeHolder = scheduledTimeHolder;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View conversationSchedulerLayout = inflater.inflate(R.layout.conversation_scheduler, null, false);
        conversationScheduler = (LinearLayout) conversationSchedulerLayout.findViewById(R.id.conversation_scheduler);

        final TextView scheduledDate = (TextView) conversationScheduler.findViewById(R.id.scheduledDate);
        final TextView scheduledTime = (TextView) conversationScheduler.findViewById(R.id.scheduledTime);
        if (scheduledTimeHolder.getTimestamp() != null) {
            String[] date = scheduledTimeHolder.getDate().split("-");
            scheduledDate.setText(date[0] + "-" + monthNames[Integer.parseInt(date[1]) - 1] + "-" + date[2]);
            scheduledTime.setText(scheduledTimeHolder.getTime());
        }


        scheduledDate.setOnClickListener(new View.OnClickListener() {

                                             @Override
                                             public void onClick(View v) {

                                                 SelectDateFragment dateFragment = new SelectDateFragment();
                                                 dateFragment.setScheduledDateView(conversationScheduler);
                                                 dateFragment.setScheduledTimeHolder(scheduledTimeHolder);
                                                 dateFragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
                                                 resetColor(scheduledDate, scheduledTime);
                                             }
                                         }
        );

        scheduledTime.setOnClickListener(new View.OnClickListener() {

                                             @Override
                                             public void onClick(View v) {

                                                 SelectTimeFragment timeFragment = new SelectTimeFragment();
                                                 timeFragment.setScheduledTimeView(conversationScheduler);
                                                 timeFragment.setScheduledTimeHolder(scheduledTimeHolder);
                                                 timeFragment.show(getActivity().getSupportFragmentManager(), "TimePicker");
                                                 resetColor(scheduledDate, scheduledTime);
                                             }
                                         }
        );

        builder.setView(conversationSchedulerLayout)

                .setPositiveButton(R.string.ScheduleText, null)
                .setNegativeButton(R.string.negetiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        scheduledTimeHolder.resetScheduledTimeHolder();
                        scheduleOption.setText(R.string.ScheduleText);
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long timestamp = scheduledTimeHolder.getTimestamp();
                if (timestamp == null) {
                    if (scheduledTimeHolder.getDate() == null) {
                        scheduledDate.setTextColor(Color.RED);
                    }
                    if (scheduledTimeHolder.getTime() == null) {
                        scheduledTime.setTextColor(Color.RED);
                    }
                } else {
                    scheduleOption.setText(DateUtils.getFormattedDate(timestamp));
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    public void resetColor(TextView scheduledDate, TextView scheduledTime) {
        scheduledDate.setTextColor(getResources().getColor(R.color.holo_blue));
        scheduledTime.setTextColor(getResources().getColor(R.color.holo_blue));
    }

}