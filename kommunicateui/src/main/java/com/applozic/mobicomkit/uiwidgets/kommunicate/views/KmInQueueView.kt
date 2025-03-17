package com.applozic.mobicomkit.uiwidgets.kommunicate.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings
import com.applozic.mobicomkit.uiwidgets.DashedLineView
import com.applozic.mobicomkit.uiwidgets.R

class KmInQueueView : LinearLayout {
    private var awayMessage: String? = null
    private lateinit var rootLinearLayout: LinearLayout
    lateinit var awayMessageTv: TextView
        private set
    private lateinit var dashedLineView: DashedLineView

    private fun inflateView(context: Context): LinearLayout {
        val layoutInflater = LayoutInflater.from(context)
        rootLinearLayout = layoutInflater.inflate(R.layout.km_away_layout, this, true) as LinearLayout
        return rootLinearLayout as LinearLayout
    }

    constructor(context: Context) : super(context) {
        init(inflateView(context))
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(inflateView(context))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(inflateView(context))
    }

    private fun init(view: View) {
        awayMessageTv = view.findViewById(R.id.awayMessageTV)
        dashedLineView = view.findViewById(R.id.awayMessageDivider)
    }

    fun setupInQueueMessage(rank: Int) {
        if (rank != 0) {
            awayMessage = context.getString(R.string.in_queue_position, rank.toString())
            handleAwayMessage(true)
        } else {
            handleAwayMessage(false)
        }
    }

    private fun handleAwayMessage(show: Boolean) {
        awayMessage?.let {
            awayMessageTv.text = it
            awayMessageTv.visibility = if (show) VISIBLE else GONE
            dashedLineView.visibility = if (show) VISIBLE else GONE
        }
    }

    fun setupTheme(isDarkModeEnabled: Boolean, alCustomizationSettings: AlCustomizationSettings) {
        setBackgroundColor(if (isDarkModeEnabled) resources.getColor(R.color.dark_mode_default) else Color.WHITE)
        awayMessageTv.setTextColor(Color.parseColor(if (isDarkModeEnabled) alCustomizationSettings.awayMessageTextColor[1] else alCustomizationSettings.awayMessageTextColor[0]))
    }
}