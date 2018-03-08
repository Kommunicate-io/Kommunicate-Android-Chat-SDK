package com.applozic.mobicomkit.uiwidgets.schedule;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.Calendar;

public class SelectTimeFragment extends DialogFragment implements TimePickerDialog.OnClickListener {
    final Calendar calendar = Calendar.getInstance();
    private View scheduledTimeView;
    private ScheduledTimeHolder scheduledTimeHolder;
    private String[] monthNames = {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    public TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (scheduledTimeHolder.getDate() == null) {
                        int year = calendar.get(Calendar.YEAR);
                        int monthOfYear = calendar.get(Calendar.MONTH);
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        scheduledTimeHolder.setDate(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        TextView selectedDate = (TextView) scheduledTimeView.findViewById(R.id.scheduledDate);
                        selectedDate.setText(dayOfMonth + "-" + monthNames[monthOfYear] + "-" + year);
                    }

                    TextView selectedTime = (TextView) scheduledTimeView.findViewById(R.id.scheduledTime);
                    scheduledTimeHolder.setTime(hourOfDay + ":" + minute + ":" + "00");
                    selectedTime.setText(scheduledTimeHolder.getTime());

                }
            };

    public SelectTimeFragment() {

    }

    public void setScheduledTimeView(View scheduledTimeView) {
        this.scheduledTimeView = scheduledTimeView;
    }

    public void setScheduledTimeHolder(ScheduledTimeHolder scheduledTimeHolder) {
        this.scheduledTimeHolder = scheduledTimeHolder;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), mTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), false);
        if (scheduledTimeHolder.getTimestamp() != null) {
            String scheduledTime = scheduledTimeHolder.getTime();
            String[] st = scheduledTime.split(":");
            timePickerDialog.updateTime(Integer.parseInt(st[0]), Integer.parseInt(st[1]));
        }
        return timePickerDialog;
    }


}
