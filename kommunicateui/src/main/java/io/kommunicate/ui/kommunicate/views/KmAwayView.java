package io.kommunicate.ui.kommunicate.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.DashedLineView;
import io.kommunicate.ui.R;

import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.people.channel.Channel;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.models.KmApiResponse;

public class KmAwayView extends LinearLayout {

    private static final String TAG = "KmAwayView";
    private LinearLayout rootLinearLayout;
    private TextView awayMessageTv;
    private LinearLayout askEmailLinearLayout;
    private ImageView askEmailImageView;
    private TextView askEmailTextView;
    private DashedLineView dashedLineView;
    private String awayMessage;
    private Channel channel;
    private boolean isUserAnonymous;
    private boolean isCollectEmailOnAwayEnabled;
    private static final String KM_FAIL_ERROR = "km_mail_error";

    public LinearLayout inflateView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.km_away_layout, this, true);
        return rootLinearLayout;
    }

    public KmAwayView(Context context) {
        super(context);
        init(inflateView(context));
    }

    public KmAwayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(inflateView(context));
    }

    public KmAwayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(inflateView(context));
    }

    private void init(View view) {
        awayMessageTv = view.findViewById(R.id.awayMessageTV);
        askEmailLinearLayout = view.findViewById(R.id.askEmailLinearLayout);
        askEmailImageView = view.findViewById(R.id.askEmailImageView);
        askEmailTextView = view.findViewById(R.id.askEmailTextView);
        dashedLineView = view.findViewById(R.id.awayMessageDivider);
    }

    public void setupAwayMessage(KmApiResponse.KmDataResponse response, Channel channel) {
        if(!response.getMessageList().isEmpty()) {
            awayMessage = response.getMessageList().get(0).getMessage();
            handleAwayMessage(true);
        }
        else {
            handleAwayMessage(false);
        }
        isUserAnonymous = response.isUserAnonymous();
        isCollectEmailOnAwayEnabled = response.isCollectEmailOnAwayMessage();
        this.channel = channel;
    }

    public void handleAwayMessage(boolean show) {
            awayMessageTv.setVisibility(show ? VISIBLE : GONE);
            awayMessageTv.setText(awayMessage);
            dashedLineView.setVisibility(show ? VISIBLE : GONE);
            askEmailLinearLayout.setVisibility(View.GONE);
    }

    public void askForEmail() {
        awayMessageTv.setVisibility(View.GONE);
        askEmailLinearLayout.setVisibility(View.VISIBLE);
    }

    public void showInvalidEmail() {
        askEmailTextView.setText(rootLinearLayout.getContext().getString(R.string.invalid_email));
        askEmailImageView.setImageDrawable(VectorDrawableCompat.create(getResources(), getResources().getIdentifier(KM_FAIL_ERROR, "drawable", rootLinearLayout.getContext().getPackageName()), null));
    }

    public TextView getAwayMessageTv() {
        return awayMessageTv;
    }

    public String getAwayMessage() {
        return awayMessage;
    }

    public boolean isUserAnonymous() {
        return isUserAnonymous;
    }

    public boolean isCollectEmailOnAwayEnabled() {
        return isCollectEmailOnAwayEnabled;
    }

    public boolean isAwayMessageVisible() {
        return (awayMessageTv != null && awayMessageTv.getVisibility() == View.VISIBLE) || (askEmailLinearLayout != null && askEmailLinearLayout.getVisibility() == View.VISIBLE);
    }

    public void handleUserEmail(String inputMessage) {
        User user = new User();
        user.setEmail(inputMessage);
        handleAwayMessage(false);
        isUserAnonymous = false;
        UserService.getInstance(rootLinearLayout.getContext()).updateUser(user, true, new ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                Kommunicate.loadAwayMessage(getContext(), channel.getKey(), new KmAwayMessageHandler() {
                    @Override
                    public void onSuccess(Context context, KmApiResponse.KmDataResponse response) {
                        if(!response.getMessageList().isEmpty()) {
                            awayMessage = response.getMessageList().get(0).getMessage();
                            handleAwayMessage(true);
                        }
                        else {
                            handleAwayMessage(false);
                        }
                    }

                    @Override
                    public void onFailure(Context context, Exception e, String response) {
                        handleAwayMessage(false);
                        Utils.printLog(context, TAG, "Response: " + response + "\nException : " + e);
                    }
                });
            }

            @Override
            public void onError(Object error) {
                 Utils.printLog(rootLinearLayout.getContext(), TAG, "Error: " + error);
            }
        });
    }

    public void setupTheme(boolean isDarkModeEnabled, CustomizationSettings customizationSettings){
        setBackgroundColor(isDarkModeEnabled ? getResources().getColor(R.color.dark_mode_default) : Color.WHITE);
        awayMessageTv.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getAwayMessageTextColor().get(1) : customizationSettings.getAwayMessageTextColor().get(0)));
        askEmailTextView.setTextColor(isDarkModeEnabled ? Color.WHITE : getContext().getResources().getColor(R.color.km_away_message_text_color));
    }
}
