package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.DashedLineView;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.people.contact.Contact;

import androidx.annotation.Nullable;
import io.kommunicate.models.KmApiResponse;

public class KmAwayView extends LinearLayout {
    LinearLayout rootLinearLayout;
    //DashedLineView awayMessageDivider;
    TextView awayMessageTv;
    LinearLayout askEmailLinearLayout;
    ImageView askEmailImageView;
    TextView askEmailTextView;
    boolean isUserAnonymous;
    boolean isCollectEmailOnAwayEnabled;
    String awayMessage;
    protected AlCustomizationSettings alCustomizationSettings;

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
        //awayMessageDivider = view.findViewById(R.id.awayMessageDivider);
        awayMessageTv = view.findViewById(R.id.awayMessageTV);
        askEmailLinearLayout = view.findViewById(R.id.askEmailLinearLayout);
        askEmailImageView = view.findViewById(R.id.askEmailImageView);
        askEmailTextView = view.findViewById(R.id.askEmailTextView);
    }

    public void setupAwayMessage(KmApiResponse.KmDataResponse response) {
        isUserAnonymous = response.isUserAnonymous();
        isCollectEmailOnAwayEnabled = response.isCollectEmailOnAwayMessage();
        awayMessage = response.getMessageList().get(0).getMessage();
        handleAwayMessage(true);
    }

    public void handleAwayMessage(boolean show) {
        if(show) {
            awayMessageTv.setVisibility(View.VISIBLE);
            awayMessageTv.setText(awayMessage);
            askEmailLinearLayout.setVisibility(View.GONE);
        }
        else {
            awayMessageTv.setVisibility(View.GONE);
            //awayMessageTv.setText(message);
        }

    }

    public void askForEmail() {
        awayMessageTv.setVisibility(View.GONE);
        askEmailLinearLayout.setVisibility(View.VISIBLE);
    }

    public void showInvalidEmail() {
        askEmailTextView.setText("Invalid email");
        askEmailImageView.setImageDrawable(getResources().getDrawable(R.drawable.km_mail_error));
    }

    public boolean isUserAnonymous() {
        return isUserAnonymous;
    }
    public boolean isCollectEmailOnAwayEnabled() {
        return isCollectEmailOnAwayEnabled;
    }

    public boolean isAwayMessageVisible() {
        return (awayMessageTv != null && awayMessageTv.getVisibility() == View.VISIBLE);
    }

    public void handleUserEmail(String inputMessage) {
        isUserAnonymous = false;
        User user = new User();
        user.setEmail(inputMessage);
        UserService.getInstance(rootLinearLayout.getContext()).updateUser(user, true, new AlCallback() {
            @Override
            public void onSuccess(Object response) {
                Log.e("email", String.valueOf(response));
            }

            @Override
            public void onError(Object error) {
                Log.e("emailerr", String.valueOf(error));
            }
        });
    }
}
