package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import androidx.appcompat.widget.SearchView;
import android.util.AttributeSet;

class KmSearchView extends SearchView {

    OnSubmitListener listener;

    public KmSearchView(Context context) {
        super(context);
    }

    public KmSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KmSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.listener = listener;
    }

    public interface OnSubmitListener {
        void submit();
    }
}