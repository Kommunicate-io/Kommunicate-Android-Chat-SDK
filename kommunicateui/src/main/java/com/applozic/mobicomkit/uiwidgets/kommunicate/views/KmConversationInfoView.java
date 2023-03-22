package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.uiwidgets.R;

import androidx.annotation.Nullable;

public class KmConversationInfoView extends LinearLayout {
    private static final String TAG = "KmConversationInfoView";
    private LinearLayout rootLinearLayout;
    private Context context;
    private ImageView leadingImageView;
    private ImageView trailingImageView;
    private TextView kmConversationInfoTextView;

    public KmConversationInfoView(Context context) {
        super(context);
        init(inflateView(context));
    }

    public KmConversationInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(inflateView(context));
    }

    public KmConversationInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(inflateView(context));
    }

    public LinearLayout inflateView(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.km_conversation_info_layout, this, true);
        return rootLinearLayout;
    }
    private void init(View view) {
        leadingImageView = view.findViewById(R.id.km_conversation_leading_image_view);
        trailingImageView = view.findViewById(R.id.km_conversation_trailing_image_view);
        kmConversationInfoTextView = view.findViewById(R.id.km_conversation_text_view);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlEventManager.getInstance().sendOnConversationInfoClicked();
            }
        });
    }
    public void setupView(String infoContent, String contentColor, String backgroundColor, String leadingImageIcon, String trailingImageIcon) {
        if(!TextUtils.isEmpty(infoContent)) {
            kmConversationInfoTextView.setText(infoContent);
        }
        if(!TextUtils.isEmpty(contentColor)) {
            kmConversationInfoTextView.setTextColor(Color.parseColor(contentColor));
        }
        if(!TextUtils.isEmpty(backgroundColor)) {
            rootLinearLayout.setBackgroundColor(Color.parseColor(backgroundColor));
        }
        if(!TextUtils.isEmpty(leadingImageIcon)) {
            leadingImageView.setImageResource(getResources().getIdentifier(leadingImageIcon, "drawable", context.getPackageName()));
        }
        if(!TextUtils.isEmpty(trailingImageIcon)) {
            trailingImageView.setImageResource(getResources().getIdentifier(trailingImageIcon, "drawable", context.getPackageName()));
        }
    }
}
