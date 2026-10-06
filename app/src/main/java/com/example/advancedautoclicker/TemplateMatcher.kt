package com.example.advancedautoclicker

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class MatchResult(
    val score: Float,
    val centerX: Float,
    val centerY: Float
)

object TemplateMatcher {

    /**
     * Lightweight normalized pixel-distance matcher.
     * For large templates/screens, sampling keeps CPU use manageable.
     */
    fun findBestMatch(screen: Bitmap, template: Bitmap): MatchResult {
        if (template.width > screen.width || template.height > screen.height) {
            return MatchResult(0f, 0f, 0f)
        }

        val scale = min(
            1f,
            min(
                140f / max(1, template.width),
                140f / max(1, template.height)
            )
        )

        val tw = max(1, (template.width * scale).toInt())
        val th = max(1, (template.height * scale).toInt())
        val t = Bitmap.createScaledBitmap(template, tw, th, true)

        val step = max(4, min(tw, th) / 12)
        var best = -1f
        var bestX = 0
        var bestY = 0

        val maxX = screen.width - tw
        val maxY = screen.height - th

        var y = 0
        while (y <= maxY) {
            var x = 0
            while (x <= maxX) {
                var diff = 0L
                var count = 0
                var yy = 0
                while (yy < th) {
                    var xx = 0
                    while (xx < tw) {
                        val sp = screen.getPixel(x + xx, y + yy)
                        val tp = t.getPixel(xx, yy)
                        diff += abs(((sp shr 16) and 255) - ((tp shr 16) and 255))
                        diff += abs(((sp shr 8) and 255) - ((tp shr 8) and 255))
                        diff += abs((sp and 255) - (tp and 255))
                        count += 3
                        xx += step
                    }
                    yy += step
                }
                val score = 1f - diff.toFloat() / (count * 255f)
                if (score > best) {
                    best = score
                    bestX = x
                    bestY = y
                }
                x += step
            }
            y += step
        }

        return MatchResult(
            best.coerceIn(0f, 1f),
            bestX + tw / 2f,
            bestY + th / 2f
        )
    }
}
