package com.applozic.mobicomkit.uiwidgets.uilistener;

public interface KmOnRecordListener {
    void onRecordStart();

    void onRecordCancel();

    void onRecordFinish(long recordTime);

    void onLessThanSecond();
}
