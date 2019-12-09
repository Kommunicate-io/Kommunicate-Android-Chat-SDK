package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public abstract class KmFlowLayout extends ViewGroup {

    protected int line_height_space;

    public KmFlowLayout(Context context) {
        super(context);
    }

    public KmFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KmFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KmFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int horizontal_spacing;
        public int vertical_spacing;

        public LayoutParams(int horizontal_spacing, int vertical_spacing) {
            super(0, 0);
            this.horizontal_spacing = horizontal_spacing;
            this.vertical_spacing = vertical_spacing;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        processOnLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        processOnMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected abstract void processOnMeasure(int widthMeasureSpec, int heightMeasureSpec);

    protected abstract void processOnLayout(boolean changed, int l, int t, int r, int b);
}
