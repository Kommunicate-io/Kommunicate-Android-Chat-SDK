package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class KmReceiverFlowLayout extends KmFlowLayout {

    private int line_height_space;

    public KmReceiverFlowLayout(Context context) {
        super(context);
    }

    public KmReceiverFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KmReceiverFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KmReceiverFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void processOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec) - (getPaddingStart() + getPaddingEnd());
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int line_height_space = 0;

        int xpos = getPaddingStart();
        int ypos = getPaddingTop();

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        boolean isRtlDirection = getContext().getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        if (isRtlDirection) {
            xpos = width - getPaddingStart();
        }

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.measure(MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST), childHeightMeasureSpec);
                final int childw = child.getMeasuredWidth();
                line_height_space = Math.max(line_height_space, child.getMeasuredHeight() + lp.vertical_spacing);

                if (isRtlDirection) {
                    if (childw > xpos) {
                        xpos = getPaddingRight();
                        ypos += line_height_space;
                    }
                    xpos -= (childw + lp.horizontal_spacing);
                } else {
                    if (xpos + childw > width) {
                        xpos = getPaddingStart();
                        ypos += line_height_space;
                    }
                    xpos += (childw + lp.horizontal_spacing);
                }
            }
        }

        this.line_height_space = line_height_space;

        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.UNSPECIFIED) {
            height = ypos + line_height_space;
        } else if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
            if (ypos + line_height_space < height) {
                height = ypos + line_height_space;
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void processOnLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        int xpos = getPaddingStart();
        int ypos = getPaddingTop();
        boolean isRtlDirection = getContext().getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        if (isRtlDirection) {
            xpos = width - getPaddingStart();
        }
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                final KmReceiverFlowLayout.LayoutParams lp = (KmReceiverFlowLayout.LayoutParams) child.getLayoutParams();
                if (isRtlDirection) {
                    if (childw > xpos) {
                        xpos = width;
                        ypos += line_height_space;
                    }
                    child.layout(xpos - childw, ypos, xpos, ypos + childh);
                    xpos -= childw - lp.horizontal_spacing;
                } else {
                    if (xpos + childw > width) {
                        xpos = getPaddingStart();
                        ypos += line_height_space;
                    }
                    child.layout(xpos, ypos, xpos + childw, ypos + childh);
                    xpos += childw + lp.horizontal_spacing;
                }
            }
        }
    }
}
