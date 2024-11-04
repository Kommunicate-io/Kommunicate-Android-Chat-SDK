package io.kommunicate.utils

import io.kommunicate.callbacks.KmCharLimitCallback

class KmInputTextLimitUtil(private val characterLimit: Int, charactersRemainingTillLimit: Int) {

    private val notifyAtCharacterCount = characterLimit - charactersRemainingTillLimit

    fun checkCharacterLimit(characterCount: Int, kmCharLimitCallback: KmCharLimitCallback) {
        val deltaCharacterCount = characterCount - characterLimit
        val exceeded = characterCount > characterLimit
        val warning = characterCount >= notifyAtCharacterCount
        if (warning || exceeded) {
            kmCharLimitCallback.onCrossed(
                exceeded,
                warning,
                if (exceeded) deltaCharacterCount else -deltaCharacterCount
            )
        } else {
            kmCharLimitCallback.onNormal()
        }
    }
}
