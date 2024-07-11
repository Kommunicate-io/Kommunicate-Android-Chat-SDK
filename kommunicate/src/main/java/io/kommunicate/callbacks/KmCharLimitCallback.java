package io.kommunicate.callbacks;

public interface KmCharLimitCallback {
    void onCrossed(boolean exceeded, boolean warning, int deltaCharacters);
    void onNormal();
}
