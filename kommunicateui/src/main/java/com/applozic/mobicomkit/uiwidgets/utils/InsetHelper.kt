package com.applozic.mobicomkit.uiwidgets.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * A utility object that helps manage system insets (like status bar, navigation bar, etc.)
 * by configuring padding or margin adjustments based on the system window insets.
 */
object InsetHelper {

    /**
     * [systemTypeMask] is a type mask for system bars (status bar and navigation bar).
     */
    @JvmField
    val systemTypeMask = WindowInsetsCompat.Type.systemBars()

    /**
     * [cameraTypeMask] is a type mask for display cutout (e.g., notches).
     */
    @JvmField
    val cameraTypeMask = WindowInsetsCompat.Type.displayCutout()

    /**
     * [navigationTypeMask] is a type mask for navigation bars.
     */
    @JvmField
    val navigationTypeMask = WindowInsetsCompat.Type.navigationBars()

    /**
     * Configures the system insets with status bar to update either
     * padding or margin of the target view.
     *
     * This function listens for window insets and adjusts the target view's padding or margins
     * accordingly. You can configure specific sides (top, bottom) with the given values.
     *
     * If the padding is being updated (`isPadding = true`), it modifies the padding of the view.
     * If the margins are being updated (`isPadding = false`), it modifies the margins of the view.
     *
     * @param view The target [View] to which the insets will be applied.
     * @param top The padding or margin to apply on the top side. Default is -1 (which means apply the inset value).
     * @param bottom The padding or margin to apply on the bottom side. Default is -1 (which means apply the inset value).
     * @param isPadding If true, the function adjusts the padding of the view. If false, it adjusts the margins of the view. Default is true.
     */
    @JvmStatic
    fun configureSystemInsets(
        view: View,
        top: Int = -1,
        bottom: Int = -1,
        isPadding: Boolean = true
    ) {
        configureInset(view, systemTypeMask, 0 , 0, top, bottom, isPadding)
    }

    /**
     * Configures the system insets (e.g., status bar, navigation bar, etc.) to update either
     * padding or margin of the target view based on the provided `typeMask`.
     *
     * This function listens for window insets and adjusts the target view's padding or margins
     * accordingly. You can configure specific sides (left, right, top, bottom) with the given values.
     *
     * If the padding is being updated (`isPadding = true`), it modifies the padding of the view.
     * If the margins are being updated (`isPadding = false`), it modifies the margins of the view.
     *
     * @param view The target [View] to which the insets will be applied.
     * @param typeMask The type of window inset to consider (e.g., system bars, navigation bars).
     * @param left The padding or margin to apply on the left side. Default is -1 (which means apply the inset value).
     * @param right The padding or margin to apply on the right side. Default is -1 (which means apply the inset value).
     * @param top The padding or margin to apply on the top side. Default is -1 (which means apply the inset value).
     * @param bottom The padding or margin to apply on the bottom side. Default is -1 (which means apply the inset value).
     * @param isPadding If true, the function adjusts the padding of the view. If false, it adjusts the margins of the view. Default is true.
     */
    @JvmStatic
    fun configureInset(
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
            } else {
                targetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = resolveInset(left, insets.left)
                    rightMargin = resolveInset(right, insets.right)
                    bottomMargin = resolveInset(bottom, insets.bottom)
                    topMargin = resolveInset(top, insets.top)
                }
            }
            // Return CONSUMED to prevent further insets handling by the system
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Resolves the inset value to either apply the existing value, use the inset value, or add the inset to the existing value.
     *
     * If the `existingPadding` is -1, the inset value is directly used.
     * If the `existingPadding` is 0, no padding or margin is applied (it will be 0).
     * Otherwise, the inset value is added to the `existingPadding`.
     *
     * @param existingPadding The current padding or margin that should be considered.
     * @param insetValue The value of the system inset (e.g., status bar, navigation bar).
     * @return The resolved value to apply as padding or margin.
     */
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
