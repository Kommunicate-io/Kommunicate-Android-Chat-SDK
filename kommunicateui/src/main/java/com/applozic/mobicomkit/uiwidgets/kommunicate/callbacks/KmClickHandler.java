package com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks;

import android.view.View;

public interface KmClickHandler<T>  {
    void onItemClicked(View view, T data);
}
