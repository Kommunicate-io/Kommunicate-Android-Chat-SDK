package io.kommunicate.ui.kommunicate.views;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;
import androidx.annotation.Nullable;
/**
 * UI class for Multiple Select Button.
 * It can be enabled by adding "checkboxAsMultipleButton"
 * This will replace the normal Checkbox with multiple select buttons
 *
 * @author Aman
 * @date March '23
 */
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

    public String getText() {
        return buttonTextView.getText().toString();
    }

    public void setChecked(boolean isChecked) {
        checked = isChecked;
        buttonTextView.setTypeface(null, isChecked? Typeface.BOLD : Typeface.NORMAL);
        buttonTextView.setCompoundDrawablesWithIntrinsicBounds(isChecked ? R.drawable.km_selector_success: 0, 0,0, 0);
        buttonTextView.setPadding(DimensionsUtils.convertDpToPx(isChecked ? 5 : 16), DimensionsUtils.convertDpToPx(isChecked ? 8 : 9), DimensionsUtils.convertDpToPx(isChecked ? 5 : 17), DimensionsUtils.convertDpToPx(8));
        StateListDrawable drawable = (StateListDrawable)buttonTextView.getBackground();
        DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
        Drawable[] drawableItems = dcs.getChildren();
        GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
        gradientDrawableChecked.setColor(getResources().getColor(isChecked ? R.color.km_multiple_select_selected_background_color : R.color.applozic_transparent_color));
    }
    public void setOnCheckedChangeListener(final onMultipleSelectButtonClicked onCheckedChangeListener) {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckedChangeListener.onSelectionChanged(KmSelectButton.this, !checked);
            }
        });
    }

    public interface onMultipleSelectButtonClicked {
        void onSelectionChanged(View view, boolean isChecked);
    }
}
