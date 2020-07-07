package com.zjta.wyb.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.zjta.wyb.BuildConfig
import com.zjta.wyb.R
import com.zjta.wyb.kotlin.closeKeyboard
import com.zjta.wyb.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * 所有activity的基类
 */
@SuppressLint("Registered")
open class BaseActivity : RxAppCompatActivity() {


    private val mCompositeDisposable = CompositeDisposable()
    fun Disposable.addToManaged() {
        mCompositeDisposable.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*禁止截屏*/
        //window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        /*共享元素*/
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        /*透明状态栏*/
        SystemBarModeUtils.immersive(this)
        requestedOrientation = setScreenOrientation()
        /*activity的出现动画*/
        overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_to_left)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        /*logcat点击跳转对用activity*/
        logShowActivity()
    }

    override fun onStart() {
        super.onStart()
        logShowActivity()
    }

    /**
     * 获取登录状态
     * @return true 已登录  false 未登录
     */
    fun checkUserLogin(): Boolean {
        return SPUtils.getUserInfo() != null
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (finishWithAnim()) overridePendingTransition(R.anim.activity_in_from_left, R.anim.activity_out_to_right)
    }

    open fun finishWithAnim() = true

    @SuppressLint("SourceLockedOrientationActivity")
    open fun setScreenOrientation(): Int {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * 状态栏透明之后，状态栏不会顶上去，标题栏多大就是多大
     * SystemBarTintManager，这时可以通过框架设置标题栏的颜色
     *
     *  @param color The color of the background tint.
     */
    fun setStatusBarTintColor(color: Int) {
        //透明状态栏
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        winParams.flags = winParams.flags or bits
        win.attributes = winParams
        val tintManager = SystemBarTintUtils(this)
        tintManager.isStatusBarTintEnabled = true
        tintManager.setStatusBarTintColor(color) // 通知栏所需颜色
    }


    /**
     *状态栏透明之后，状态栏不会顶上去，标题栏多大就是多大
     * SystemBarTintManager，这时可以通过框架设置标题栏的颜色
     *
     * @param res The identifier of the resource.
     */
    fun setStatusBarTintResource(res: Int) {
        //透明状态栏
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        winParams.flags = winParams.flags or bits
        win.attributes = winParams
        val tintManager = SystemBarTintUtils(this)
        tintManager.isStatusBarTintEnabled = true
        tintManager.setNavigationBarTintResource(res) // 通知栏所需颜色
    }

    open fun onTitleLeftBtnClick(view: View) {
        onBackPressed()
    }


    override fun onResumeFragments() {
        super.onResumeFragments()
        logShowActivity()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logShowActivity()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        logShowActivity()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)
        logShowActivity()
    }

    override fun onLocalVoiceInteractionStarted() {
        super.onLocalVoiceInteractionStarted()
        logShowActivity()
    }

    override fun onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped()
        logShowActivity()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        logShowActivity()
    }

    override fun onPause() {
        super.onPause()
        logShowActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logShowActivity()
    }

    override fun onStop() {
        super.onStop()
        logShowActivity()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        logShowActivity()
    }


    override fun onRestart() {
        super.onRestart()
        logShowActivity()
    }

    override fun onResume() {
        super.onResume()
        logShowActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        logShowActivity()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logShowActivity()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        logShowActivity()
        return super.onCreateOptionsMenu(menu)
    }

    private fun logShowActivity() {
        val stackTraceElement = Thread.currentThread()
            .stackTrace

        var currentIndex = stackTraceElement.indices.firstOrNull {
            stackTraceElement[it].methodName.compareTo("logShowActivity") == 0
        }?.let { it + 1 } ?: -1

        currentIndex += 1
        val fullClassName = stackTraceElement[currentIndex].className
        if (!fullClassName.startsWith(BuildConfig.APPLICATION_ID)) return
        val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        val methodName = stackTraceElement[currentIndex].methodName
        val lineNumber = stackTraceElement[currentIndex].lineNumber.toString()
        Log.v("BaseActivity",
              LogUtils.FLAG + "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")")
    }
}
