package com.applozic.mobicomkit.uiwidgets.kommunicate.animators;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmRecordButton;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class KmAnimationHelper {
    private Context context;
    private AnimatedVectorDrawableCompat animatedVectorDrawable;
    private ImageView basketImg;
    private TextView smallBlinkingDot;
    private AlphaAnimation alphaAnimation;
    private OnBasketAnimationEndListener onBasketAnimationEndListener;
    private boolean isBasketAnimating, isStartRecorded = false;
    private float micX, micY = 0;
    private AnimatorSet micAnimation;
    private TranslateAnimation translateAnimation1, translateAnimation2;
    private Handler handler1, handler2;

    public KmAnimationHelper(Context context, ImageView basketImg, TextView smallBlinkingDot) {
        this.context = context;
        this.smallBlinkingDot = smallBlinkingDot;
        this.basketImg = basketImg;
        animatedVectorDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.recv_basket_animated);
    }

    @SuppressLint("RestrictedApi")
    public void animateBasket(float basketInitialY) {
        isBasketAnimating = true;

        clearAlphaAnimation(false);

        if (micX == 0) {
            micX = smallBlinkingDot.getX();
            micY = smallBlinkingDot.getY();
        }

        micAnimation = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(context, R.animator.delete_mic_animation);
        micAnimation.setTarget(smallBlinkingDot); // set the view you want to animate

        translateAnimation1 = new TranslateAnimation(0, 0, basketInitialY, basketInitialY - 90);
        translateAnimation1.setDuration(250);

        translateAnimation2 = new TranslateAnimation(0, 0, basketInitialY - 130, basketInitialY);
        translateAnimation2.setDuration(350);


        micAnimation.start();
        basketImg.setImageDrawable(animatedVectorDrawable);

        handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                basketImg.setVisibility(VISIBLE);
                basketImg.startAnimation(translateAnimation1);
            }
        }, 350);

        translateAnimation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                animatedVectorDrawable.start();

                handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        basketImg.startAnimation(translateAnimation2);
                        smallBlinkingDot.setVisibility(INVISIBLE);
                        basketImg.setVisibility(INVISIBLE);
                    }
                }, 250);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        translateAnimation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                basketImg.setVisibility(INVISIBLE);

                isBasketAnimating = false;

                //if the user pressed the record button while the animation is running
                // then do NOT call on Animation end
                if (onBasketAnimationEndListener != null && !isStartRecorded) {
                    onBasketAnimationEndListener.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void resetBasketAnimation() {
        if (isBasketAnimating) {
            translateAnimation1.reset();
            translateAnimation1.cancel();
            translateAnimation2.reset();
            translateAnimation2.cancel();

            micAnimation.cancel();

            smallBlinkingDot.clearAnimation();
            basketImg.clearAnimation();

            if (handler2 != null) {
                handler2.removeCallbacksAndMessages(null);
            }

            basketImg.setVisibility(INVISIBLE);
            smallBlinkingDot.setX(micX);
            smallBlinkingDot.setY(micY);
            smallBlinkingDot.setVisibility(View.GONE);

            isBasketAnimating = false;
        }
    }

    public void clearAlphaAnimation(boolean hideView) {
        alphaAnimation.cancel();
        alphaAnimation.reset();
        smallBlinkingDot.clearAnimation();
        if (hideView) {
            smallBlinkingDot.setVisibility(View.GONE);
        }
    }

    public void animateSmallMicAlpha() {
        alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        smallBlinkingDot.startAnimation(alphaAnimation);
    }

    public void moveRecordButtonAndSlideToCancelBack(final KmRecordButton recordBtn, LinearLayout slideToCancelLayout, float initialX, float difX) {

        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(recordBtn.getX(), initialX);

        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) animation.getAnimatedValue();
                recordBtn.setX(x);
            }
        });

        recordBtn.stopScale();
        positionAnimator.setDuration(0);
        positionAnimator.start();

        if (difX != 0) {
            float x = initialX - difX;
            slideToCancelLayout.animate()
                    .x(x)
                    .setDuration(0)
                    .start();
        }
    }

    public void resetSmallMic() {
        smallBlinkingDot.setAlpha(1.0f);
        smallBlinkingDot.setScaleX(1.0f);
        smallBlinkingDot.setScaleY(1.0f);
    }

    public void setOnBasketAnimationEndListener(OnBasketAnimationEndListener onBasketAnimationEndListener) {
        this.onBasketAnimationEndListener = onBasketAnimationEndListener;

    }

    public void onAnimationEnd() {
        if (onBasketAnimationEndListener != null)
            onBasketAnimationEndListener.onAnimationEnd();
    }

    public void setStartRecorded(boolean startRecorded) {
        isStartRecorded = startRecorded;
    }
}
