package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;

public class KmCustomDialog {

    private Dialog dialog;

    public void showDialog(Activity activity, String assignedBot, final KmDialogClickListener listener) {
        if (activity == null) {
            return;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.km_take_over_from_bot_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.kmDialogTitle);
        if (dialogTitle != null) {
            dialogTitle.setText(ApplozicService.getContext(activity).getString(R.string.km_take_over_from_bot_dialog_title, assignedBot));
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(ApplozicService.getContext(activity), R.drawable.km_take_over_from_bot_button_background));

            TextView cancelButton = dialog.findViewById(R.id.kmDialogNegativeText);
            TextView takeOverFromBotButton = dialog.findViewById(R.id.kmDialogPositiveText);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClickNegativeButton(dialog);
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            takeOverFromBotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClickPositiveButton(dialog);
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        }
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public interface KmDialogClickListener {
        void onClickNegativeButton(Dialog dialog);

        void onClickPositiveButton(Dialog dialog);
    }
}
