package com.zjta.wyb.widget.banner.transformer

import android.view.View
import androidx.viewpager.widget.ViewPager

/**
 * 全民直播banner效果
 */
class GokuTransformer : ViewPager.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.75f
    }

    override fun transformPage(page: View, pos: Float) {

        when {
            pos < -1 -> {
                page.alpha = 0f
            }
            pos <= 0 -> {
                page.alpha = 1f
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
            }
            pos <= 1 -> {
                page.alpha = 1 - pos
                page.translationX = page.width * -pos
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(pos))
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            }
        }
    }
}
