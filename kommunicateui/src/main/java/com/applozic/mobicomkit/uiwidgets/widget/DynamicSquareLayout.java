package com.applozic.mobicomkit.uiwidgets.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class DynamicSquareLayout extends RelativeLayout {

    public DynamicSquareLayout(Context context) {
        super(context);
    }

    public DynamicSquareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DynamicSquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(size, size);
    }
}
