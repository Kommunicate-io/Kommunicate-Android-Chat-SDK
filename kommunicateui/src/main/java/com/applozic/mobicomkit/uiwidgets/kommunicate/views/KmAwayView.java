package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.DashedLineView;
import com.applozic.mobicomkit.uiwidgets.R;

import androidx.annotation.Nullable;
import io.kommunicate.models.KmApiResponse;

public class KmAwayView extends LinearLayout {
    LinearLayout rootLinearLayout;
    DashedLineView awayMessageDivider;
    TextView awayMessageTv;
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
        awayMessageDivider = view.findViewById(R.id.awayMessageDivider);
        awayMessageTv = view.findViewById(R.id.awayMessageTV);
    }

    public void showAwayMessage(boolean show, KmApiResponse.KmDataResponse response) {
        if(response.isUserAnonymous()) {
            awayMessageTv.setText(response.getMessageList().get(0).getMessage() + "Anon");
        }
        else {
            awayMessageTv.setText(response.getMessageList().get(0).getMessage());
        }
    }

    public void showInvalidEmail() {
        awayMessageTv.setText("invalidemail");
    }

    public boolean isAwayMessageVisible() {
        return (awayMessageTv != null && awayMessageDivider != null && awayMessageTv.getVisibility() == View.VISIBLE && awayMessageDivider.getVisibility() == View.VISIBLE);
    }

}
