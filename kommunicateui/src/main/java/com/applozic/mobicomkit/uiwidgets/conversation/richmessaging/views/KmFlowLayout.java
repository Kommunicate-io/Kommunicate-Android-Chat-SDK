package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

//simply a flex-box layout in wrap mode
public class KmFlowLayout extends FlexboxLayout {

    void initFlexBoxInWrapMode() {
        this.setFlexDirection(FlexDirection.ROW);
        this.setFlexWrap(FlexWrap.WRAP);
    }

    public KmFlowLayout(Context context) {
        super(context);
        initFlexBoxInWrapMode();
    }

    public KmFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFlexBoxInWrapMode();
    }

    public KmFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFlexBoxInWrapMode();
    }
}
