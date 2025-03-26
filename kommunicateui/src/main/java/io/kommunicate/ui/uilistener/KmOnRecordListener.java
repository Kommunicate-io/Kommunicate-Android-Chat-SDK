package io.kommunicate.ui.uilistener;

public interface KmOnRecordListener {
    void onRecordStart();

    void onRecordCancel();

    void onRecordFinish(long recordTime);

    void onLessThanSecond();
}
