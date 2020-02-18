package com.applozic.mobicomkit.uiwidgets.conversation;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.applozic.mobicommons.ApplozicService;


public class KmResolve extends BaseObservable {

    private boolean visible;
    private Drawable icon;
    private String statusName;
    private int colorResId;

    public KmResolve() {

    }

    public KmResolve(int status) {
        setStatusName(KmConversationStatus.getStatusText(status));
        setColorResId(KmConversationStatus.getColorId(status));
        setIconId(KmConversationStatus.getIconId(status));
    }

    public KmResolve(int status, boolean visible) {
        setVisible(visible);
        setStatusName(KmConversationStatus.getStatusText(status));
        setColorResId(KmConversationStatus.getColorId(status));
        setIconId(KmConversationStatus.getIconId(status));
    }

    public KmResolve(int status, String name, boolean visible) {
        setStatusName(name);
        setVisible(visible);
        setColorResId(KmConversationStatus.getColorId(status));
        setIconId(KmConversationStatus.getIconId(status));
    }

    @Bindable
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        notifyPropertyChanged(BR.visible);
    }

    @Bindable
    public Drawable getIcon() {
        return icon;
    }

    public void setIconId(int iconResId) {
        this.icon = ContextCompat.getDrawable(ApplozicService.getAppContext(), iconResId);
        notifyPropertyChanged(BR.icon);
    }

    @Bindable
    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
        notifyPropertyChanged(BR.statusName);
    }

    @Bindable
    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = ContextCompat.getColor(ApplozicService.getAppContext(), colorResId);
        notifyPropertyChanged(BR.colorResId);
    }

    @Override
    public String toString() {
        return "KmResolve{" +
                "visible=" + visible +
                ", iconResId=" + icon +
                ", statusName='" + statusName + '\'' +
                ", colorResId=" + colorResId +
                '}';
    }
}
