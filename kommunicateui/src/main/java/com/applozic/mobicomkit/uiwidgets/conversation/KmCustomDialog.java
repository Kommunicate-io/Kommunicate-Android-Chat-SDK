package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.app.Dialog;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.JsonMarker;

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

    public static class KmDialogBuilder extends JsonMarker {
        private Activity activity;
        private String title;
        private String message;
        private String positiveButtonLabel;
        private String negativeButtonLabel;
        private int titleTextColor;
        private int messageTextColor;
        private int positiveButtonTextColor;
        private int negativeButtonTextColor;
        private Dialog dialog;
        private KmDialogClickListener kmDialogClickListener;

        public Activity getActivity() {
            return activity;
        }

        public KmDialogBuilder(Activity activity) {
            this.activity = activity;
        }

        public String getTitle() {
            return title;
        }

        public KmDialogBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public KmDialogBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getPositiveButtonLabel() {
            return positiveButtonLabel;
        }

        public KmDialogBuilder setPositiveButtonLabel(String positiveButtonLabel) {
            this.positiveButtonLabel = positiveButtonLabel;
            return this;
        }

        public String getNegativeButtonLabel() {
            return negativeButtonLabel;
        }

        public KmDialogBuilder setNegativeButtonLabel(String negativeButtonLabel) {
            this.negativeButtonLabel = negativeButtonLabel;
            return this;
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public KmDialogBuilder setTitleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public int getMessageTextColor() {
            return messageTextColor;
        }

        public KmDialogBuilder setMessageTextColor(int messageTextColor) {
            this.messageTextColor = messageTextColor;
            return this;
        }

        public int getPositiveButtonTextColor() {
            return positiveButtonTextColor;
        }

        public KmDialogBuilder setPositiveButtonTextColor(int positiveButtonTextColor) {
            this.positiveButtonTextColor = positiveButtonTextColor;
            return this;
        }

        public int getNegativeButtonTextColor() {
            return negativeButtonTextColor;
        }

        public KmDialogBuilder setNegativeButtonTextColor(int negativeButtonTextColor) {
            this.negativeButtonTextColor = negativeButtonTextColor;
            return this;
        }

        public KmDialogClickListener getKmDialogClickListener() {
            return kmDialogClickListener;
        }

        public KmDialogBuilder setKmDialogClickListener(KmDialogClickListener kmDialogClickListener) {
            this.kmDialogClickListener = kmDialogClickListener;
            return this;
        }

        public Dialog getDialog() {
            return dialog;
        }

        public void show(final KmDialogClickListener kmDialogClickListener) {
            if (activity == null) {
                return;
            }
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.km_take_over_from_bot_dialog);

            TextView dialogTitle = dialog.findViewById(R.id.kmDialogTitle);
            TextView dialogMessage = dialog.findViewById(R.id.kmDialogMessage);
            if (dialogTitle != null) {
                if (!TextUtils.isEmpty(title)) {
                    dialogTitle.setText(title);
                } else {
                    dialogTitle.setText("");
                }

                if (titleTextColor > 0) {
                    dialogTitle.setTextColor(ApplozicService.getContext(activity).getResources().getColor(titleTextColor));
                }
            }
            if (dialogMessage != null) {
                if (!TextUtils.isEmpty(message)) {
                    dialogMessage.setText(message);
                } else {
                    dialogMessage.setText("");
                }
                if (messageTextColor > 0) {
                    dialogMessage.setTextColor(ApplozicService.getContext(activity).getResources().getColor(messageTextColor));
                }
            }

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(ApplozicService.getContext(activity), R.drawable.km_take_over_from_bot_button_background));

                TextView cancelButton = dialog.findViewById(R.id.kmDialogNegativeText);
                TextView takeOverFromBotButton = dialog.findViewById(R.id.kmDialogPositiveText);


                if (cancelButton != null) {
                    if (!TextUtils.isEmpty(negativeButtonLabel)) {
                        cancelButton.setText(negativeButtonLabel);
                    }
                    if (negativeButtonTextColor > 0) {
                        cancelButton.setTextColor(ApplozicService.getContext(activity).getResources().getColor(negativeButtonTextColor));
                    }
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (kmDialogClickListener != null) {
                                kmDialogClickListener.onClickNegativeButton(dialog);
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });
                }

                if (takeOverFromBotButton != null) {
                    if (!TextUtils.isEmpty(positiveButtonLabel)) {
                        takeOverFromBotButton.setText(positiveButtonLabel);
                    }
                    if (positiveButtonTextColor > 0) {
                        takeOverFromBotButton.setTextColor(ApplozicService.getContext(activity).getResources().getColor(positiveButtonTextColor));
                    }
                    takeOverFromBotButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (kmDialogClickListener != null) {
                                kmDialogClickListener.onClickPositiveButton(dialog);
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });
                }
                dialog.show();
            }
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
