package com.panov.timetable.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.core.widget.NestedScrollView
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BounceScrollView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : NestedScrollView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    init {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        isFillViewport = true
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    private lateinit var childView: View
    private lateinit var animator: ObjectAnimator

    private var touched = false
    private var touchPrevious = 0f
    private var overScrollDelta = 0f
    private var overScrollDirection = 0

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> onPointerDown(event)
                MotionEvent.ACTION_MOVE -> onPointerMove(event)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onPointerUp(event)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onScrollChanged(xCurrent: Int, yCurrent: Int, xPrevious: Int, yPrevious: Int) {
        super.onScrollChanged(xCurrent, yCurrent, xPrevious, yPrevious)
        onScrolled(yCurrent, yPrevious)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child != null) childView = child
    }

    private fun onPointerDown(event: MotionEvent) {
        if (!touched) {
            touched = true
            touchPrevious = event.y
            stopAnimation()
        }
    }

    private fun onPointerMove(event: MotionEvent) {
        if (touched) {
            val touchCurrent = event.y
            val touchOffset = touchCurrent - touchPrevious
            val scrollMax = getScrollMax()
            touchPrevious = touchCurrent

            if (overScrollDirection >= 0 && scrollY == 0) {
                if (touchOffset > 0) {
                    overScrollDirection = 1
                    overScrollDelta += getDamping(abs(touchOffset))
                } else {
                    overScrollDelta -= max(abs(touchOffset) - scrollMax, 0f)
                    if (overScrollDelta <= 0) overScrollDelta = 0f
                }
            }

            if (overScrollDirection <= 0 && scrollY == scrollMax) {
                if (touchOffset < 0) {
                    overScrollDirection = -1
                    overScrollDelta += getDamping(abs(touchOffset))
                } else {
                    overScrollDelta -= max(abs(touchOffset) - scrollMax, 0f)
                    if (overScrollDelta <= 0) overScrollDelta = 0f
                }
            }

            if (overScrollDelta == 0f) overScrollDirection = 0
            overScrollDelta = min(overScrollDelta, height / 2f)
            childView.translationY = overScrollDelta * overScrollDirection
        }
    }

    private fun onPointerUp(event: MotionEvent) {
        if (touched) {
            touched = false
            touchPrevious = event.y
            overScrollDelta = 0f
            overScrollDirection = 0
            moveToDefaultPosition()
        }
    }

    private fun onScrolled(yCurrent: Int, yPrevious: Int) {
        val yOffset = yCurrent - yPrevious

        if (touched) {
            if ((overScrollDirection > 0 && yOffset > 0) || (overScrollDirection < 0 && yOffset < 0)) {
                if (overScrollDelta > abs(yOffset)) {
                    scrollY -= yOffset
                    overScrollDelta -= abs(yOffset)
                } else {
                    scrollY -= floor(overScrollDelta).toInt()
                    overScrollDelta = 0f
                    overScrollDirection = 0
                }
                childView.translationY = overScrollDelta * overScrollDirection
            }
        } else if ((yCurrent == 0 && yOffset < 0) || (yCurrent == getScrollMax() && yOffset > 0)) {
            childView.translationY = -yOffset.toFloat()
            moveToDefaultPosition()
        }
    }

    private fun stopAnimation() {
        if (::animator.isInitialized && animator.isRunning) {
            animator.cancel()
        }
        if (::childView.isInitialized) {
            val delta = childView.translationY
            if (delta > 0) overScrollDirection = 1
            if (delta < 0) overScrollDirection = -1
            overScrollDelta = abs(delta)
        }
    }

    private fun moveToDefaultPosition() {
        stopAnimation()
        animator = ObjectAnimator.ofFloat(childView, View.TRANSLATION_Y, 0f)
        animator.setDuration(400).interpolator = QuartOutInterpolator
        animator.start()
    }

    private fun getDamping(offset: Float): Float {
        return offset / (2 / (1 - (overScrollDelta / (height / 2)).pow(2)))
    }

    private fun getScrollMax(): Int {
        val scrollMax = childView.height - height
        return max(scrollMax, 0)
    }


    private object QuartOutInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return 1 - (1 - input).pow(4)
        }
    }
}