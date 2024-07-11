package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.animators.KmScaleAnimation;

public class KmRecordButton extends AppCompatImageView implements View.OnTouchListener, View.OnClickListener {
    private KmScaleAnimation scaleAnim;
    private KmRecordView recordView;
    private boolean listenForRecord = true;
    private OnRecordClickListener onRecordClickListener;


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