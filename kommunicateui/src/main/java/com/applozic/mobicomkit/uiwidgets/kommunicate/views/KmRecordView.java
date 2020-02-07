package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.DimensionsUtils;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.animators.KmAnimationHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.animators.OnBasketAnimationEndListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmOnRecordListener;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.IOException;

public class KmRecordView extends FrameLayout {
    public static final int DEFAULT_CANCEL_BOUNDS = 8; //8dp
    private TextView smallBlinkingDot;
    private Chronometer counterTime;
    private TextView slideToCancel;
    private TextView recordingText;
    private LinearLayout slideToCancelLayout;
    private ImageView arrow, basketImg;
    private float initialX, basketInitialY, difX = 0;
    private float cancelBounds = DEFAULT_CANCEL_BOUNDS;
    private long startTime, elapsedTime = 0;
    private Context context;
    private KmOnRecordListener recordListener;
    private boolean isSwiped, isLessThanSecondAllowed = false;
    private boolean isSoundEnabled = true;
    private MediaPlayer player;
    private KmAnimationHelper animationHelper;
    private boolean isSpeechToTextEnabled;

    public KmRecordView(Context context) {
        super(context);
        this.context = context;
        init(context, null, -1, -1);
    }


    public KmRecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs, -1, -1);
    }

    public KmRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs, defStyleAttr, -1);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View view = View.inflate(context, R.layout.km_record_view_layout, null);
        addView(view);


        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setClipChildren(false);

        arrow = view.findViewById(R.id.arrow);
        slideToCancel = view.findViewById(R.id.slide_to_cancel);
        smallBlinkingDot = view.findViewById(R.id.record_circle);
        counterTime = view.findViewById(R.id.counter_tv);
        basketImg = view.findViewById(R.id.basket_img);
        slideToCancelLayout = view.findViewById(R.id.slide_to_cancel_layout);
        recordingText = view.findViewById(R.id.recording_text);

        hideViews(true);

        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KmRecordView,
                    defStyleAttr, defStyleRes);


            int slideArrowResource = typedArray.getResourceId(R.styleable.KmRecordView_slide_to_cancel_arrow, -1);
            String slideToCancelText = typedArray.getString(R.styleable.KmRecordView_slide_to_cancel_text);
            int slideMarginRight = (int) typedArray.getDimension(R.styleable.KmRecordView_slide_to_cancel_margin_right, 30);
            int counterTimeColor = typedArray.getColor(R.styleable.KmRecordView_counter_time_color, -1);
            int arrowColor = typedArray.getColor(R.styleable.KmRecordView_slide_to_cancel_arrow_color, -1);


            int cancelBounds = typedArray.getDimensionPixelSize(R.styleable.KmRecordView_slide_to_cancel_bounds, -1);

            if (cancelBounds != -1) {
                setCancelBounds(cancelBounds, false);
            }

            if (slideArrowResource != -1) {
                Drawable slideArrow = AppCompatResources.getDrawable(getContext(), slideArrowResource);
                arrow.setImageDrawable(slideArrow);
            }

            if (slideToCancelText != null)
                slideToCancel.setText(slideToCancelText);

            if (counterTimeColor != -1)
                setCounterTimeColor(counterTimeColor);


            if (arrowColor != -1)
                setSlideToCancelArrowColor(arrowColor);


            setMarginRight(slideMarginRight, true);

            typedArray.recycle();
        }

        animationHelper = new KmAnimationHelper(context, basketImg, smallBlinkingDot);
    }

    public void hideViews(boolean hideSmallMic) {
        slideToCancelLayout.setVisibility(GONE);
        counterTime.setVisibility(GONE);
        recordingText.setVisibility(GONE);
        if (hideSmallMic)
            smallBlinkingDot.setVisibility(GONE);
    }

    private void showViews() {
        if (!isSpeechToTextEnabled) {
            counterTime.setVisibility(VISIBLE);
            recordingText.setVisibility(VISIBLE);
            smallBlinkingDot.setVisibility(VISIBLE);
            slideToCancelLayout.setVisibility(VISIBLE);
        } else {
            recordingText.setText(Utils.getString(context, R.string.km_speech_listening_text));
        }
    }

    private boolean isLessThanOneSecond(long time) {
        return time <= 1000;
    }

    private void playSound(int soundRes) {

        if (isSoundEnabled) {
            if (soundRes == 0)
                return;

            try {
                player = new MediaPlayer();
                AssetFileDescriptor afd = context.getResources().openRawResourceFd(soundRes);
                if (afd == null) return;
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                player.prepare();
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }

                });
                player.setLooping(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onActionDown(KmRecordButton recordBtn) {
        if (recordListener != null) {
            recordListener.onRecordStart();
        }

        animationHelper.setStartRecorded(true);
        animationHelper.resetBasketAnimation();
        animationHelper.resetSmallMic();


        if (!isSpeechToTextEnabled) {
            recordBtn.startScale();
        }
        initialX = recordBtn.getX();

        showViews();

        animationHelper.animateSmallMicAlpha();
        counterTime.setBase(SystemClock.elapsedRealtime());
        startTime = System.currentTimeMillis();
        counterTime.start();
        isSwiped = false;
    }

    protected void onActionMove(KmRecordButton recordBtn, MotionEvent motionEvent) {
        if (isSpeechToTextEnabled) {
            return;
        }
        long time = System.currentTimeMillis() - startTime;

        if (context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {

            if (!isSwiped && time >= 150) {
                if (slideToCancelLayout.getX() != 0 && (slideToCancelLayout.getX() + slideToCancelLayout.getWidth()) >= counterTime.getX() - cancelBounds) {
                    if (isLessThanOneSecond(time)) {
                        hideViews(true);
                        animationHelper.clearAlphaAnimation(false);
                        animationHelper.onAnimationEnd();
                    } else {
                        //animate the basket
                        hideViews(false);
                        animationHelper.animateBasket(basketInitialY);
                    }


                    animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialX, difX);

                    counterTime.stop();
                    isSwiped = true;

                    animationHelper.setStartRecorded(false);
                    if (recordListener != null)
                        recordListener.onRecordCancel();
                } else {
                    if (motionEvent.getRawX() > initialX) {
                        recordBtn.animate()
                                .x(motionEvent.getRawX())
                                .setDuration(0)
                                .start();
                        if (difX == 0)
                            difX = (initialX - slideToCancelLayout.getX());
                        slideToCancelLayout.animate()
                                .x(motionEvent.getRawX() - difX)
                                .setDuration(0)
                                .start();
                    }
                }
            }

        } else {

            if (!isSwiped && time >= 150) {
                //  getX , getRight, +
                if (slideToCancelLayout.getX() != 0 && slideToCancelLayout.getX() <= counterTime.getRight() + cancelBounds) {
                    if (isLessThanOneSecond(time)) {
                        hideViews(true);
                        animationHelper.clearAlphaAnimation(false);
                        animationHelper.onAnimationEnd();
                    } else {
                        hideViews(false);
                        animationHelper.animateBasket(basketInitialY);
                    }

                    animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialX, difX);

                    counterTime.stop();
                    isSwiped = true;

                    animationHelper.setStartRecorded(false);
                    if (recordListener != null)
                        recordListener.onRecordCancel();
                } else {
                    // <
                    if (motionEvent.getRawX() < initialX) {
                        recordBtn.animate()
                                .x(motionEvent.getRawX())
                                .setDuration(0)
                                .start();
                        if (difX == 0)
                            //no -
                            difX = (initialX - slideToCancelLayout.getX());

                        slideToCancelLayout.animate()
                                .x(motionEvent.getRawX() - difX)
                                .setDuration(0)
                                .start();
                    }
                }
            }
        }
    }

    protected void onActionUp(KmRecordButton recordBtn) {
        if (isSpeechToTextEnabled) {
            return;
        }

        stopRecordingAnimation(recordBtn);
    }

    public void stopRecordingAnimation(KmRecordButton recordButton) {
        elapsedTime = System.currentTimeMillis() - startTime;

        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {
            if (recordListener != null)
                recordListener.onLessThanSecond();

            animationHelper.setStartRecorded(false);
        } else {
            if (recordListener != null && !isSwiped)
                recordListener.onRecordFinish(elapsedTime);

            animationHelper.setStartRecorded(false);
        }

        hideViews(!isSwiped);

        if (!isSwiped)
            animationHelper.clearAlphaAnimation(true);

        if (recordButton != null) {
            animationHelper.moveRecordButtonAndSlideToCancelBack(recordButton, slideToCancelLayout, initialX, difX);
        }
        counterTime.stop();
    }

    private void setMarginRight(int marginRight, boolean convertToDp) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) slideToCancelLayout.getLayoutParams();
        if (convertToDp) {
            layoutParams.rightMargin = (int) DimensionsUtils.convertDpToPixel(marginRight);
        } else
            layoutParams.rightMargin = marginRight;

        slideToCancelLayout.setLayoutParams(layoutParams);
    }


    public void setOnRecordListener(KmOnRecordListener recrodListener) {
        this.recordListener = recrodListener;
    }

    public void setOnBasketAnimationEndListener(OnBasketAnimationEndListener onBasketAnimationEndListener) {
        animationHelper.setOnBasketAnimationEndListener(onBasketAnimationEndListener);
    }

    public void setSoundEnabled(boolean isEnabled) {
        isSoundEnabled = isEnabled;
    }

    public void setLessThanSecondAllowed(boolean isAllowed) {
        isLessThanSecondAllowed = isAllowed;
    }

    public void setSlideToCancelText(String text) {
        slideToCancel.setText(text);
    }

    public void setSlideToCancelTextColor(int color) {
        slideToCancel.setTextColor(color);
    }

    public void setSlideMarginRight(int marginRight) {
        setMarginRight(marginRight, true);
    }

    public float getCancelBounds() {
        return cancelBounds;
    }

    public void setCancelBounds(float cancelBounds) {
        setCancelBounds(cancelBounds, true);
    }

    public void enableSpeechToText(boolean enable) {
        this.isSpeechToTextEnabled = enable;
    }

    public void setCounterTimeColor(int color) {
        counterTime.setTextColor(color);
    }

    public void setSlideToCancelArrowColor(int color) {
        arrow.setColorFilter(color);
    }

    private void setCancelBounds(float cancelBounds, boolean convertDpToPixel) {
        float bounds = convertDpToPixel ? DimensionsUtils.convertDpToPixel(cancelBounds) : cancelBounds;
        this.cancelBounds = bounds;
    }
}