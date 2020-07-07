package com.zjta.wyb.widget.banner

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
import androidx.annotation.DrawableRes
import androidx.viewpager.widget.ViewPager
import com.zjta.wyb.R
import com.zjta.wyb.kotlin.dip
import com.zjta.wyb.kotlin.screenWidth
import com.zjta.wyb.widget.banner.holder.BannerViewHolder
import com.zjta.wyb.widget.banner.transformer.CoverModeTransformer
import com.zjta.wyb.widget.banner.transformer.ScaleYTransformer


/**
 *  广告轮播图控件
 */
class BannerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    private val mSettings: BannerViewSettings = BannerViewSettings(context, attrs)
    private val mIndicatorContainer: LinearLayout
    private val mViewPager = BannerViewPager(context)
    private var mAdapter: BannerPagerAdapter<*>? = null
    private val mPagerPadding = dip(30)//左右漏出模式下的padding值
    private var mDelayedTime = 3000// Banner 切换时间间隔
    private val mIndicatorRes = intArrayOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_background)
    private var mOnPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var mBannerPageClickListener: BannerPageClickListener? = null
    private val mHandler = Handler()
    private val mIndicators = ArrayList<ImageView>()
    private val mViewPagerScroller: ViewPagerScroller? by lazy {
        try {
            val field = ViewPager::class.java.getDeclaredField("mScroller")
            field.isAccessible = true
            val pagerScroller = ViewPagerScroller(context)
            field.set(mViewPager, pagerScroller)
            pagerScroller
        } catch (e: Exception) {
            null
        }
    }


    init {
        clipChildren = false

        val pagerParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        if (mSettings.isOpenMZEffect) {
            pagerParams.marginStart = mPagerPadding
            pagerParams.marginEnd = mPagerPadding
            mViewPager.clipChildren = false

            /* CoverModeTransformer:中间页面覆盖两边，和魅族APP 的banner 效果一样。
            * ScaleYTransformer:中间页面不覆盖，页面并排，只是Y轴缩小。*/
            mViewPager.setPageTransformer(mSettings.isMiddlePageCover,
                if (mSettings.isMiddlePageCover) CoverModeTransformer(mViewPager) else ScaleYTransformer())
        }
        addView(mViewPager, pagerParams)

        mIndicatorContainer = LinearLayout(context)
        mIndicatorContainer.setPadding(dip(16), 0, dip(16), mSettings.indicatorMarginBottom)
        val containerParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        containerParams.addRule(when (mSettings.indicatorAlign) {
            0 -> RelativeLayout.ALIGN_PARENT_LEFT
            1 -> RelativeLayout.CENTER_HORIZONTAL
            2 -> RelativeLayout.ALIGN_PARENT_RIGHT
            else -> Gravity.CENTER
        })
        if (mSettings.isOpenMZEffect) {
            containerParams.marginStart = mPagerPadding
            containerParams.marginEnd = mPagerPadding
        }
        containerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        mIndicatorContainer.orientation = LinearLayout.HORIZONTAL
        addView(mIndicatorContainer, containerParams)
        //隐藏指示器
        if (mSettings.isHiddenIndicator) mIndicatorContainer.visibility = View.GONE
    }

    /**
     * 初始化数据
     */
    private fun <T> initData(datas: ArrayList<T>, holder: BannerViewHolder<T>) {
        when (datas.size) {
            0 -> return
            1 -> {
                mSettings.isCanLoop = false
                mSettings.isAutoLoop = false
                mSettings.isOpenMZEffect = false
                val params = mViewPager.layoutParams as RelativeLayout.LayoutParams
                params.marginEnd = 0
                params.marginStart = 0
                mViewPager.layoutParams = params
                setIndicatorVisibility(View.GONE)
            }
        }
        //如果在播放，就先让播放停止
        pause()

        // 2017.7.20 fix：将Indicator初始化放在Adapter的初始化之前，解决更新数据变化更新时crush.
        //初始化Indicator
        initIndicator(datas.size)
        setDuration(1000)


        while (mSettings.isCanLoop && datas.size < 5) {
            datas.addAll(datas)
        }

        mAdapter = BannerPagerAdapter(datas, holder, mSettings.isCanLoop)
        mAdapter!!.setUpViewViewPager(mViewPager)
        mAdapter!!.setPageClickListener(mBannerPageClickListener)

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mOnPageChangeListener?.onPageScrolled(position % mIndicators.size,
                    positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                mCurrentItem = position

                val realPos = mCurrentItem % mIndicators.size
                // 切换indicator
                if (mIndicatorContainer.visibility == View.VISIBLE) {
                    mIndicators.forEachIndexed { index, it ->
                        it.setImageResource(if (index == realPos) mIndicatorRes[1] else mIndicatorRes[0])
                    }
                }
                // 不能直接将mOnPageChangeListener 设置给ViewPager ,否则拿到的position 是原始的positon
                mOnPageChangeListener?.onPageSelected(realPos)
            }

            override fun onPageScrollStateChanged(state: Int) {
                mOnPageChangeListener?.onPageScrollStateChanged(state)
            }
        })
    }

    private var mCurrentItem = 0//当前位置
    private val mLoopRunnable = object : Runnable {
        override fun run() {
            if (mSettings.isAutoLoop) {
                mCurrentItem = mViewPager.currentItem
                mCurrentItem++
                if (mCurrentItem == mIndicators.size - 1) {
                    mCurrentItem = 0
                    mViewPager.setCurrentItem(mCurrentItem, false)
                    mHandler.postDelayed(this, mDelayedTime.toLong())
                } else {
                    mViewPager.currentItem = mCurrentItem
                    mHandler.postDelayed(this, mDelayedTime.toLong())
                }
            } else {
                mHandler.postDelayed(this, mDelayedTime.toLong())
            }
        }
    }

    /**
     * 初始化指示器Indicator
     *
     * 这里要引用一个类 {@link AspectRatioImageView} <br/>
     */
    private fun initIndicator(count: Int) {
        mIndicatorContainer.removeAllViews()
        mIndicators.clear()

        (0 until count).forEach {
            val imageView = ImageView(context)
            imageView.setImageResource(if (it == mCurrentItem % count) mIndicatorRes[1] else mIndicatorRes[0])
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            val params = LinearLayout.LayoutParams(mSettings.indicatorSize,
                mSettings.indicatorSize)
            params.setMargins(mSettings.indicatorMargin, 0, mSettings.indicatorMargin, 0)
            imageView.layoutParams = params

            mIndicators.add(imageView)
            mIndicatorContainer.addView(imageView)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!mSettings.isAutoLoop) return super.dispatchTouchEvent(ev)

        when (ev?.action) {
            //按住Banner的时候，停止自动轮播
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_DOWN -> {
                val paddingLeft = mViewPager.left
                val touchX = ev.rawX
                // 如果是魅族模式，去除两边的区域
                if (touchX >= paddingLeft && touchX < context.screenWidth() - paddingLeft) {
                    pause()
                }
            }
            MotionEvent.ACTION_UP -> start()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize =
            if (hMode == View.MeasureSpec.AT_MOST || hMode == View.MeasureSpec.UNSPECIFIED) {//相当于我们设置为wrap_content
                (wSize / mSettings.aspectRatio()).toInt()
            } else {//相当于我们设置为match_parent或者为一个具体的值
                MeasureSpec.getSize(heightMeasureSpec)
            }
        mViewPager.layoutParams.apply {
            this.height = hSize
            this.width = wSize
        }
        setMeasuredDimension(wSize, hSize)

        /* val width = View.MeasureSpec.getSize(widthMeasureSpec)
         val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
         val height = (width / mSettings.aspectRatio()).toInt()
         val heightMeasure = View.MeasureSpec.makeMeasureSpec(height, heightMode)
         super.onMeasure(widthMeasureSpec, heightMeasure)*/
    }


    /******************************************************************************************************/
    /**                             对外API                                                               **/
    /******************************************************************************************************/

    /**
     * 设置数据，这是最重要的一个方法。
     * 其他的配置应该在这个方法之前调用
     *
     * @param datas  Banner 展示的数据集合
     * @param holder ViewHolder生成器
     */
    fun <T> setPages(datas: ArrayList<T>, holder: BannerViewHolder<T>) {
        initData(datas, holder)
    }

    /**
     * 开始轮播
     *
     * <p>应该确保在调用用了[BannerView.setPages]之后调用这个方法开始轮播</p>
     */
    fun start() {
        // 如果Adapter为null, 说明还没有设置数据，这个时候不应该轮播Banner
        if (mIndicators.isEmpty()) return

        if (mSettings.isAutoLoop) {
            mHandler.postDelayed(mLoopRunnable, mDelayedTime.toLong())
        }
    }

    /**
     * 停止轮播
     */
    public fun pause() {
        mHandler.removeCallbacks(mLoopRunnable)
    }

    /**
     * 是否支持轮播
     */
    fun setCanLoop(canLoop: Boolean) {
        mSettings.isCanLoop = canLoop
    }

    /**
     * 是否支持轮播
     */
    fun setAutoLoop(autoLoop: Boolean) {
        mSettings.isAutoLoop = autoLoop
    }


    /**
     * 设置BannerView 的切换时间间隔
     *
     * @param delayedTime
     */
    fun setDelayedTime(delayedTime: Int) {
        mDelayedTime = delayedTime
    }

    fun addPageChangeLisnter(onPageChangeListener: ViewPager.OnPageChangeListener?) {
        mOnPageChangeListener = onPageChangeListener
    }

    /**
     * 添加Page点击事件
     *
     * @param bannerPageClickListener [BannerPageClickListener]
     */
    fun setBannerPageClickListener(bannerPageClickListener: BannerPageClickListener?) {
        mBannerPageClickListener = bannerPageClickListener
    }

    /**
     * 是否显示Indicator
     *
     * @param visible true 显示Indicator，否则不显示
     */
    fun setIndicatorVisible(visible: Boolean) {
        mIndicatorContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * 返回ViewPager
     *
     * @return [ViewPager]
     */
    fun getViewPager(): ViewPager = mViewPager

    /**
     * 设置indicator 图片资源
     *
     * @param unSelectRes 未选中状态资源图片
     * @param selectRes   选中状态资源图片
     */
    fun setIndicatorRes(@DrawableRes unSelectRes: Int, @DrawableRes selectRes: Int) {
        mIndicatorRes[0] = unSelectRes
        mIndicatorRes[1] = selectRes
    }

    /**
     * 设置ViewPager切换的速度
     *
     * @param duration 切换动画时间
     */
    fun setDuration(duration: Int) {
        mViewPagerScroller?.duration = duration
    }

    fun setIndicatorVisibility(visibility: Int) {
        mIndicatorContainer.visibility = visibility
    }

    /**
     * 添加page改变监听
     */
    fun addOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mViewPager.addOnPageChangeListener(listener)
    }
}