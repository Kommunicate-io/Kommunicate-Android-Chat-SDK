package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat;
import io.kommunicate.utils.KmUtils;

public class KmTypingView extends LinearLayout {

    private TextView firstDot;
    private TextView secondDot;
    private TextView thirdDot;
    private LinearLayout parentLayout;

    public KmTypingView(Context context) {
        super(context);
        init(context, null, -1, -1);

    }

    public KmTypingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);

    }

    public KmTypingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs,  defStyleAttr, -1);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KmTypingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs,  defStyleAttr, -1);

    }

    @SuppressLint("RestrictedApi")
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View view = View.inflate(context, R.layout.km_typing_indicator_layout, null);
        addView(view);

        firstDot = view.findViewById(R.id.typing_first_dot);
        secondDot = view.findViewById(R.id.typing_second_dot);
        thirdDot = view.findViewById(R.id.typing_third_dot);
        parentLayout = view.findViewById(R.id.typing_linear_layout);
        setupBackground();
        startTypingAnimation();
    }

    private void setupBackground() {
        GradientDrawable bgShape;
            bgShape = (GradientDrawable) parentLayout.getBackground();

        if (bgShape != null) {
            String bgColor = new AlCustomizationSettings().getReceivedMessageBackgroundColor();
            bgShape.setColor(Color.parseColor(bgColor));
            bgShape.setStroke(3, Color.parseColor(bgColor));

        }
    }

    @SuppressLint("RestrictedApi")
    private void startTypingAnimation() {
        AnimatorSet firstAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(getContext(), R.animator.km_blinking);
        firstAnimatorSet.setTarget(firstDot);
        firstAnimatorSet.start();

        AnimatorSet secondAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(getContext(), R.animator.km_blinking);
        secondAnimatorSet.setStartDelay(200);
        secondAnimatorSet.setTarget(secondDot);
        secondAnimatorSet.start();

        AnimatorSet thirdAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(getContext(), R.animator.km_blinking);
        thirdAnimatorSet.setStartDelay(400);
        thirdAnimatorSet.setTarget(thirdDot);
        thirdAnimatorSet.start();

    }
}
