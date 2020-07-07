package com.zjta.wyb.kotlin

import android.animation.ValueAnimator
import android.graphics.Path
import android.graphics.PathMeasure
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.DimenRes
import androidx.core.view.ViewCompat
import com.zjta.wyb.utils.StringUtils


/**
 *  View相关的扩展方法
 */

fun View.click(function: (view: View) -> Unit) = setOnClickListener { function(it) }

fun Thread.run(function: () -> Unit) {

}

fun View.longClick(function: (view: View) -> Boolean) = setOnLongClickListener { function(it) }

fun View.halfWidth() = width / 2f

fun View.halfHeight() = height / 2f

infix fun View.setElevationResource(@DimenRes id: Int) {
    ViewCompat.setElevation(this, context.getDimensFloat(id))
}


fun View.hide(isGone: Boolean = false) {
    visibility = if (isGone) View.GONE else View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.createArcPath(offsetX: Float, offsetY: Float): Path {
    val startX = translationX
    val startY = translationY
    val dY = offsetY - translationY
    val pointX = if (dY < 0) offsetX else startX
    val pointY = if (dY < 0) startY else offsetY
    return Path().apply {
        moveTo(startX, startY)
        quadTo(pointX, pointY, offsetX, offsetY)
    }
}

fun View.getArcListener(path: Path): ValueAnimator.AnimatorUpdateListener {
    val point = FloatArray(2)
    val pathMeasure = PathMeasure(path, false)
    return ValueAnimator.AnimatorUpdateListener { animation ->
        val value = animation.animatedFraction
        pathMeasure.getPosTan(pathMeasure.length * value, point, null)
        translationX = point[0]
        translationY = point[1]
    }
}


fun EditText.forbiddenSpace(): EditText {
    val filter = InputFilter { source, _, _, _, _, _ ->
        return@InputFilter if (source == " ") "" else null
    }
    val result = filters.copyOf(filters.size + 1)
    result[result.size - 1] = filter
    this.filters = result
    return this
}


fun EditText.forbiddenEnter(): EditText {
    val filter = InputFilter { source, _, _, _, _, _ ->
        return@InputFilter if (source.toString().contentEquals("\n")) "" else null
    }
    val result = filters.copyOf(filters.size + 1)
    result[result.size - 1] = filter
    this.filters = result
    return this
}

fun EditText.onlyMoney():EditText {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val temp: String = s.toString()
            val posDot = temp.indexOf(".")
            if (posDot <= 0) return
            if (temp.length - posDot - 1 > 2) {
                s.delete(posDot + 3, posDot + 4)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
    return this
}

fun EditText.addEmptySelectTrue():EditText{
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
           this@addEmptySelectTrue.isSelected= s.toString()
               .isEmpty()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
    return this
}

fun View.makeDropDownMeasureSpec(): Pair<Int, Int> {
    return Pair(measureSpec(this.width), measureSpec(this.height))
}

private fun measureSpec(measureSpec: Int): Int {
    val mode: Int = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
        View.MeasureSpec.UNSPECIFIED
    } else {
        View.MeasureSpec.EXACTLY
    }
    return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
}