package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;

import io.kommunicate.utils.KmUtils;

public class KmToast {

    public static Toast makeText(Context context, String text, int duration) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.km_recorder_message_layout,
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

    public static Toast success(Context context, int textResId, int duration) {
        return success(context, Utils.getString(context, textResId), -1, duration);
    }

    public static Toast error(Context context, int textResId, int duration) {
        return error(context, Utils.getString(context, textResId), -1, duration);
    }

    public static Toast success(Context context, String text, int duration) {
        return success(context, text, -1, duration);
    }

    public static Toast error(Context context, String text, int duration) {
        return error(context, text, -1, duration);
    }

    public static Toast success(Context context, int textResId, int imageResId, int duration) {
        return makeText(context, Utils.getString(context, textResId), R.color.km_toast_success_color, imageResId, R.color.white, R.color.white, duration);
    }

    public static Toast error(Context context, int textResId, int imageResId, int duration) {
        return makeText(context, Utils.getString(context, textResId), R.color.km_toast_error_color, imageResId, R.color.white, R.color.white, duration);
    }

    public static Toast success(Context context, String text, int imageResId, int duration) {
        return makeText(context, text, R.color.km_toast_success_color, imageResId, R.color.white, R.color.white, duration);
    }

    public static Toast error(Context context, String text, int imageResId, int duration) {
        return makeText(context, text, R.color.km_toast_error_color, imageResId, R.color.white, R.color.white, duration);
    }

    public static Toast makeText(Context context, String text, int backgroundColorResId, int drawableResId, int textColorResId, int drawableTintColorResId, int duration) {
        final Toast currentToast = Toast.makeText(context, "", duration);
        final View toastLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.km_custom_toast_layout, null);
        final ImageView toastIcon = toastLayout.findViewById(R.id.km_toast_icon);
        final TextView toastTextView = toastLayout.findViewById(R.id.km_toast_text);

        if (!TextUtils.isEmpty(text)) {
            toastTextView.setText(text);
        }
        if (textColorResId > 0) {
            toastTextView.setTextColor(Utils.getColor(context, textColorResId));
        }

        if (drawableResId > 0) {
            toastIcon.setVisibility(View.VISIBLE);
            toastIcon.setImageDrawable(KmUtils.getDrawable(context, drawableResId));
            if (drawableTintColorResId > 0) {
                toastIcon.setColorFilter(Utils.getColor(context, drawableTintColorResId));
            }
        } else {
            toastIcon.setVisibility(View.GONE);
        }

        if (backgroundColorResId > 0) {
            toastLayout.setBackgroundColor(Utils.getColor(context, backgroundColorResId));
        }

        NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) KmUtils.getDrawable(context, R.drawable.toast_frame);
        ninePatchDrawable.setColorFilter(Utils.getColor(context, backgroundColorResId), PorterDuff.Mode.SRC_IN);
        toastLayout.setBackground(ninePatchDrawable);
        currentToast.setView(toastLayout);

        return currentToast;
    }
}
