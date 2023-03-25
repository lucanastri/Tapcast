package com.kizune.tapcast.animation

import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * LinearProgressIndicator animation that...
 * fades in the view with an end action (optional)
 */
fun LinearProgressIndicator.fadeIn(action: () -> Unit = {}) {
    this.animate()
        .alpha(1f)
        .setDuration(200)
        .withEndAction {
            action()
        }
}

/**
 * LinearProgressIndicator animation that...
 * fades out the view an end action (optional)
 */
fun LinearProgressIndicator.fadeOut(action: () -> Unit = {}) {
    this.animate()
        .alpha(0f)
        .setDuration(200)
        .withEndAction {
            action()
        }
}