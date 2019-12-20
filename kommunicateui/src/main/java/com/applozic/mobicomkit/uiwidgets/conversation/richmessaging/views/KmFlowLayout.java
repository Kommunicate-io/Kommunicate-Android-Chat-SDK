package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;

public class KmFlowLayout extends ViewGroup {

    protected int lineHeightSpace;
    protected boolean alignSenderSide;
    protected boolean isRtlDirection = false;

    public KmFlowLayout(Context context) {
        super(context);
    }

    public KmFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray kmCustomAttrTypedArray = ApplozicService.getContext(context).obtainStyledAttributes(attrs, R.styleable.KmFlowLayout, 0, 0);
        alignSenderSide = kmCustomAttrTypedArray.getBoolean(R.styleable.KmFlowLayout_alignSenderSide, false);
        isRtlDirection = ApplozicService.getContext(context).getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        kmCustomAttrTypedArray.recycle();
    }

    public KmFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KmFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int horizontalSpacing;
        public int verticalSpacing;

        public LayoutParams(int horizontalSpacing, int verticalSpacing) {
            super(0, 0);
            this.horizontalSpacing = horizontalSpacing;
            this.verticalSpacing = verticalSpacing;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec) - (getPaddingStart() + getPaddingEnd());
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int xpos = getPaddingStart();
        int ypos = getPaddingTop();

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        processPositions(width, childHeightMeasureSpec, xpos, ypos, height, heightMeasureSpec, true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        int xpos = getPaddingStart();
        int ypos = getPaddingTop();

        processPositions(width, 0, xpos, ypos, 0, 0, false);
    }

    private void processPositions(int width, int childHeightMeasureSpec, int xpos, int ypos, int height, int heightMeasureSpec, boolean measureOnly) {
        if (alignAtEnd()) {
            xpos = width - (alignSenderSide ? getPaddingEnd() : getPaddingStart());
        }

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (measureOnly) {
                    child.measure(MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST), childHeightMeasureSpec);
                }
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lineHeightSpace = Math.max(lineHeightSpace, child.getMeasuredHeight() + lp.verticalSpacing);

                if (alignAtEnd()) {
                    if (childw > xpos) {
                        xpos = width;
                        ypos += lineHeightSpace;
                    }
                    if (!measureOnly) {
                        child.layout(xpos - childw, ypos, xpos, ypos + childh);
                    }
                    xpos -= (childw + lp.horizontalSpacing);
                } else {
                    if (xpos + childw > width) {
                        xpos = getPaddingStart();
                        ypos += lineHeightSpace;
                    }
                    if (!measureOnly) {
                        child.layout(xpos, ypos, xpos + childw, ypos + childh);
                    }
                    xpos += (childw + lp.horizontalSpacing);
                }
            }
        }

        if (measureOnly) {
            if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.UNSPECIFIED) {
                height = ypos + lineHeightSpace;
            } else if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
                if (ypos + lineHeightSpace < height) {
                    height = ypos + lineHeightSpace;
                }
            }
            setMeasuredDimension(width, height);
        }
    }

    public boolean alignAtEnd() {
        return alignSenderSide != isRtlDirection;
    }
}
