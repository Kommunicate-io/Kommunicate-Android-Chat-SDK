package com.applozic.mobicomkit.uiwidgets.conversation;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.library.baseAdapters.BR;

import com.applozic.mobicommons.ApplozicService;


public class KmResolve extends BaseObservable {

    private boolean visible;
    private Drawable icon;
    private String statusName;
    private int colorResId;
    private String extensionText;
    private boolean statusTextStyleBold;
    private int iconTintColorId;

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

    @Bindable
    public String getExtensionText() {
        return extensionText;
    }

    public void setExtensionText(String extensionText) {
        this.extensionText = extensionText;
        notifyPropertyChanged(BR.extensionText);
    }

    @Bindable
    public boolean isStatusTextStyleBold() {
        return statusTextStyleBold;
    }

    public void setStatusTextStyleBold(boolean statusTextStyleBold) {
        this.statusTextStyleBold = statusTextStyleBold;
        notifyPropertyChanged(BR.statusTextStyleBold);
    }

    public int getIconTintColorId() {
        return iconTintColorId;
    }

    public void setIconTintColorId(int iconTintColorId) {
        this.iconTintColorId = iconTintColorId;
    }

    @BindingAdapter("isBold")
    public static void setBold(TextView view, boolean statusTextStyleBold) {
        view.setTypeface(null, statusTextStyleBold ? Typeface.BOLD : Typeface.NORMAL);
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
