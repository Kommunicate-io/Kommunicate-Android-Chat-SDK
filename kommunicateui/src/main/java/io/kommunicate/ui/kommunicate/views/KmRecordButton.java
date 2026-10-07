package io.kommunicate.ui.kommunicate.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.animators.KmScaleAnimation;

public class KmRecordButton extends AppCompatImageView implements View.OnTouchListener, View.OnClickListener {
    private KmScaleAnimation scaleAnim;
    private KmRecordView recordView;
    private boolean listenForRecord = true;
    private OnRecordClickListener onRecordClickListener;
    private final Handler gestureHandler = new Handler(Looper.getMainLooper());
    private boolean longPressTriggered;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (recordView == null) {
                return;
            }
            longPressTriggered = true;
            recordView.onActionDown(KmRecordButton.this);
        }
    };


    public void setRecordView(KmRecordView recordView) {
        this.recordView = recordView;
    }

    public KmRecordButton(Context context) {
        super(context);
        init(context, null);
    }

    public KmRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KmRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);


    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KmRecordButton);

            int imageResource = typedArray.getResourceId(R.styleable.KmRecordButton_mic, -1);

            if (imageResource != -1) {
                setTheImageResource(imageResource);
            }

            typedArray.recycle();
        }

        scaleAnim = new KmScaleAnimation(this);

        setOnTouchListener(this);
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClip(this);
    }

    public void setClip(View v) {
        if (v.getParent() == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(false);
            ((ViewGroup) v).setClipToPadding(false);
        }

        if (v.getParent() instanceof View) {
            setClip((View) v.getParent());
        }
    }


    private void setTheImageResource(int imageResource) {
        Drawable image = AppCompatResources.getDrawable(getContext(), imageResource);
        setImageDrawable(image);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isListenForRecord()) {
            if (recordView != null && recordView.isSpeechToTextEnabled()) {
                return handleSpeechToTextGesture(event);
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    recordView.onActionDown((KmRecordButton) v);
                    break;

                case MotionEvent.ACTION_MOVE:
                    recordView.onActionMove((KmRecordButton) v, event);
                    break;

                case MotionEvent.ACTION_UP:
                    recordView.onActionUp((KmRecordButton) v);
                    break;
            }
        }
        return isListenForRecord();
    }

    private boolean handleSpeechToTextGesture(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                longPressTriggered = false;
                gestureHandler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                break;
            case MotionEvent.ACTION_MOVE:
                if (longPressTriggered) {
                    recordView.onActionMove(this, event);
                }
                break;
            case MotionEvent.ACTION_UP:
                gestureHandler.removeCallbacks(longPressRunnable);
                if (longPressTriggered) {
                    recordView.onActionUp(this);
                } else {
                    performClick();
                    recordView.onSpeechToTextTap();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                gestureHandler.removeCallbacks(longPressRunnable);
                if (longPressTriggered) {
                    recordView.onActionCancel(this);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void startScale() {
        scaleAnim.start();
    }

    public void stopScale() {
        scaleAnim.stop();
    }

    public void startScaleWithValue(float value) {
        scaleAnim.startWithValue(value);
    }

    public void setListenForRecord(boolean listenForRecord) {
        this.listenForRecord = listenForRecord;
    }

    public boolean isListenForRecord() {
        return listenForRecord;
    }

    public void setOnRecordClickListener(OnRecordClickListener onRecordClickListener) {
        this.onRecordClickListener = onRecordClickListener;
    }


    @Override
    public void onClick(View v) {
        if (onRecordClickListener != null)
            onRecordClickListener.onClick(v);
    }

    public interface OnRecordClickListener {
        void onClick(View v);
    }
}
