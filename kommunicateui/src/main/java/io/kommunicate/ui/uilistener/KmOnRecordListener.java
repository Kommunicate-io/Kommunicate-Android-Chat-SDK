package io.kommunicate.ui.uilistener;

public interface KmOnRecordListener {
    void onRecordStart();

    default void onSpeechToTextStart() {
    }

    void onRecordCancel();

    void onRecordFinish(long recordTime);

    void onLessThanSecond();
}
