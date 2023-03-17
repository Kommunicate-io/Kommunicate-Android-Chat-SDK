package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

public class KmSelectButton extends LinearLayout {
    private static final String TAG = "KmSelectButton";
    private LinearLayout rootLinearLayout;
    private TextView buttonTextView;
    private boolean checked = false;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public LinearLayout inflateView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.km_multiple_select_button, this, true);
        buttonTextView = rootLinearLayout.findViewById(R.id.singleTextItem);
        return rootLinearLayout;
    }
    public KmSelectButton(Context context) {
        super(context);
        inflateView(context);
    }
    public KmSelectButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    public KmSelectButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    public void setText(String text) {
        buttonTextView.setText(text);
    }
    public void setChecked(boolean isChecked) {
        Log.e("checkbox", "check");

        checked = isChecked;
        buttonTextView.setPressed(isChecked);
        buttonTextView.setSelected(isChecked);
        buttonTextView.setCompoundDrawablesWithIntrinsicBounds(isChecked ? R.drawable.km_selector_success: 0, 0,0, 0);
        StateListDrawable drawable = (StateListDrawable)buttonTextView.getBackground();
        DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
        Drawable[] drawableItems = dcs.getChildren();
        GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
//        gradientDrawableChecked.setColor(Color.BLUE);
        //        GradientDrawable drawable = (GradientDrawable) buttonTextView.getBackground();
//        drawable.mutate();
//        drawable.setColor(Color.BLUE);
        //        buttonTextView.setBackgroundColor(isChecked ? getResources().getColor(R.color.holo_blue) : getResources().getColor(R.color.white));
//        buttonTextView.setEnabled(true);
//        buttonTextView.setSelected(true);
//        buttonTextView.setActivated(true);
       // this.setPressed(true);
    }
    public void setOnCheckedChangeListener(final onMultipleSelectButtonClicked onCheckedChangeListener) {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckedChangeListener.onSelectionChanged(null, !checked);

            }
        });
    }

    public interface onMultipleSelectButtonClicked {
        void onSelectionChanged(View view, boolean isChecked);
    }
}
