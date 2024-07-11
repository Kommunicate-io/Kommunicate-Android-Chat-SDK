package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.DimensionsUtils;

public class KmRecyclerView extends RecyclerView {
    private int mMaxHeight = 0;

    public KmRecyclerView(Context context) {
        super(context);
    }

    public KmRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KmRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setmMaxHeight(int mMaxHeight) {
        this.mMaxHeight = mMaxHeight;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = MeasureSpec.makeMeasureSpec(DimensionsUtils.convertDpToPx(mMaxHeight), MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
    }
}
