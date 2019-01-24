package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;

public class KmToast {

    public static Toast makeText(Context context, String text, int duration) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.km_custom_toast_layout,
                (ViewGroup) ((Activity) context).findViewById(R.id.custom_toast_container));

        TextView textView = (TextView) layout.findViewById(R.id.toastText);
        textView.setText(text);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, Utils.dpToPx(89));
        toast.setDuration(duration);
        toast.setView(layout);

        return toast;
    }

    public static Toast makeText(Context context, View view, int duration) {
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(duration);
        toast.setView(view);

        return toast;
    }
}
