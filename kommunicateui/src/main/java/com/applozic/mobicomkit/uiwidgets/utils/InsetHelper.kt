package com.applozic.mobicomkit.uiwidgets.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

object InsetHelper {

    @JvmField
    val systemTypeMask = WindowInsetsCompat.Type.systemBars()

    @JvmField
    val cameraTypeMask = WindowInsetsCompat.Type.displayCutout()

    @JvmField
    val navigationTypeMask = WindowInsetsCompat.Type.navigationBars()

    @JvmStatic
    fun configureSystemInset(
        view: View,
        typeMask: Int,
        left: Int = -1,
        right: Int = -1,
        top: Int = -1,
        bottom: Int = -1,
        isPadding: Boolean = true
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { targetView, windowInsets ->
            val insets = windowInsets.getInsets(typeMask)
            if (isPadding) {
                targetView.updatePadding(
                    left = resolveInset(left, insets.left),
                    right = resolveInset(right, insets.right),
                    top = resolveInset(top, insets.top),
                    bottom = resolveInset(bottom, insets.bottom)
                )
            }else {
                targetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = resolveInset(left, insets.left)
                    rightMargin = resolveInset(right, insets.right)
                    bottomMargin = resolveInset(bottom, insets.bottom)
                    topMargin = resolveInset(top, insets.top)
                }
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun resolveInset(existingPadding: Int, insetValue: Int): Int {
        return when (existingPadding) {
            -1 -> {
                insetValue
            }
            0 -> {
                0
            }
            else -> {
                insetValue + existingPadding
            }
        }
    }
}