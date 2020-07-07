package com.zjta.wyb.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.zjta.wyb.R
import com.zjta.wyb.base.GlobalApplication


/**
 * Toast工具
 */
@SuppressLint("ShowToast")
object ToastUtils {
    private val mToast by lazy {
        val toast = Toast.makeText(GlobalApplication.getContext(), "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.view.setBackgroundResource(R.drawable.toast_bg)
        toast.view.findViewById<TextView>(android.R.id.message).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
        }
        toast
    }

    fun showToast(message: String) {
        mToast.setText(message)
        mToast.show()
    }
}