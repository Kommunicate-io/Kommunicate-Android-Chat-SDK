package io.kommunicate.utils;

import io.kommunicate.callbacks.KmCharLimitCallback;

public class KmInputTextLimitUtil {
    private int characterLimit;
    private int notifyAtCharacterCount;

    public KmInputTextLimitUtil(int characterLimit, int charactersRemainingTillLimit) {
        this.characterLimit = characterLimit;
        this.notifyAtCharacterCount = characterLimit - charactersRemainingTillLimit;
    }

    public void checkCharacterLimit(int characterCount, KmCharLimitCallback kmCharLimitCallback) {
        int deltaCharacterCount = characterCount - characterLimit;
        boolean exceeded = characterCount > characterLimit;
        boolean warning = characterCount >= notifyAtCharacterCount;
        if (warning || exceeded) {
            kmCharLimitCallback.onCrossed(exceeded, warning, exceeded ? deltaCharacterCount : -deltaCharacterCount);
        } else {
            kmCharLimitCallback.onNormal();
        }
    }
}
