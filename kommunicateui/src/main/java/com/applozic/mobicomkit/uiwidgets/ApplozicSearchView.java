package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;

class ApplozicSearchView extends SearchView {

    OnSubmitListener listener;

    public ApplozicSearchView(Context context) {
        super(context);
    }

    public ApplozicSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ApplozicSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.listener = listener;
    }

    public interface OnSubmitListener {
        void submit();
    }
}