package com.applozic.mobicomkit.uiwidgets.kommunicate.animators;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class KmScaleAnimation {
    private View view;
    private String SCALE_Y = "scaleY";
    private String SCALE_X = "scaleX";

    public KmScaleAnimation(View view) {
        this.view = view;
    }


    public void start() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, SCALE_Y, 2.0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, SCALE_X, 2.0f);
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }

    public void startWithValue(float scale) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, SCALE_Y, scale);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, SCALE_X, scale);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }

    public void stop() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, SCALE_X, 1.0f);

        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }
}
