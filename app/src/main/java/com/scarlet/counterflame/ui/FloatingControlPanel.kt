package com.scarlet.counterflame.ui

import android.view.View
import com.google.android.material.card.MaterialCardView

class FloatingControlPanel(private val cardView: MaterialCardView) {

    fun toggleVisibility() {
        if (cardView.visibility == View.VISIBLE) {
            hide()
        } else {
            show()
        }
    }

    fun show() {
        cardView.alpha = 0f
        cardView.visibility = View.VISIBLE
        cardView.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    fun hide() {
        cardView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction { cardView.visibility = View.GONE }
            .start()
    }

    val isVisible: Boolean
        get() = cardView.visibility == View.VISIBLE
}
