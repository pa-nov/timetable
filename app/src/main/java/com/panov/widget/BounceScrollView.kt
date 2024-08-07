package com.panov.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Interpolator
import androidx.core.widget.NestedScrollView
import kotlin.math.abs
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

    private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var animator: ObjectAnimator = ObjectAnimator()
    private lateinit var content: View
    private var touched = false
    private var canScroll = false
    private var pointerId = 0
    private var pointerPrevious = 0f
    private var overScrollDelta = 0f
    private var overScrollDirection = 0


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return onPointerEvent(event, super.onInterceptTouchEvent(event))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onPointerEvent(event, super.onTouchEvent(event))
    }

    override fun onScrollChanged(xCurrent: Int, yCurrent: Int, xPrevious: Int, yPrevious: Int) {
        super.onScrollChanged(xCurrent, yCurrent, xPrevious, yPrevious)
        onScrolled(yCurrent, yPrevious)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child != null) content = child
    }


    private fun onPointerEvent(event: MotionEvent, source: Boolean): Boolean {
        if (getScrollMax() == 0) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> onPointerDown(event)
            MotionEvent.ACTION_MOVE -> onPointerMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> onPointerUp(event)
        }
        return if (abs(content.translationY) < scaledTouchSlop) source else true
    }

    private fun onPointerDown(event: MotionEvent) {
        val index = event.actionIndex
        if (animator.isRunning) animator.cancel()

        touched = true
        pointerId = event.getPointerId(index)
        pointerPrevious = event.getY(index)
    }

    private fun onPointerMove(event: MotionEvent) {
        val index = event.findPointerIndex(pointerId)

        if (index >= 0) {
            val pointerCurrent = event.getY(index)
            val offset = pointerCurrent - pointerPrevious

            if (!canScroll) {
                if (abs(offset) > scaledTouchSlop) {
                    canScroll = true
                    pointerPrevious = pointerCurrent
                    val delta = content.translationY
                    if (delta > 0) overScrollDirection = 1
                    if (delta < 0) overScrollDirection = -1
                    overScrollDelta = abs(delta)
                }
                return
            }

            val scrollMax = getScrollMax()
            pointerPrevious = pointerCurrent

            if (overScrollDirection >= 0 && scrollY == 0) {
                if (offset > 0) {
                    overScrollDirection = 1
                    overScrollDelta += getDamping(abs(offset))
                } else {
                    overScrollDelta -= max(abs(offset) - scrollMax, 0f)
                    if (overScrollDelta < 0) overScrollDelta = 0f
                }
            }

            if (overScrollDirection <= 0 && scrollY == scrollMax) {
                if (offset < 0) {
                    overScrollDirection = -1
                    overScrollDelta += getDamping(abs(offset))
                } else {
                    overScrollDelta -= max(abs(offset) - scrollMax, 0f)
                    if (overScrollDelta < 0) overScrollDelta = 0f
                }
            }

            if (overScrollDelta <= 0) overScrollDirection = 0
            overScrollDelta = min(overScrollDelta, height / 2f)
            content.translationY = overScrollDelta * overScrollDirection
        }
    }

    private fun onPointerUp(event: MotionEvent) {
        val index = event.actionIndex

        if (event.getPointerId(index) == pointerId) {
            if (event.pointerCount > 1) {
                pointerId = event.getPointerId(if (index == 0) 1 else 0)
                pointerPrevious = event.getY(if (index == 0) 1 else 0)
            } else {
                touched = false
                canScroll = false
                overScrollDelta = 0f
                overScrollDirection = 0
                moveToDefaultPosition()
            }
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
                    scrollY -= overScrollDelta.toInt()
                    overScrollDelta = 0f
                    overScrollDirection = 0
                }
                content.translationY = overScrollDelta * overScrollDirection
            }
        } else {
            if ((yCurrent == 0 && yOffset < 0) || (yCurrent == getScrollMax() && yOffset > 0)) {
                content.translationY = -yOffset.toFloat()
                moveToDefaultPosition()
            }
        }
    }

    private fun moveToDefaultPosition() {
        if (animator.isRunning) animator.cancel()
        animator = ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, 0f)
        animator.interpolator = QuartOutInterpolator
        animator.setDuration(400)
        animator.start()
    }

    private fun getDamping(offset: Float): Float {
        return offset / (1.5f / (1 - (overScrollDelta / (height / 2)).pow(2)))
    }

    private fun getScrollMax(): Int {
        return max(content.height - height, 0)
    }


    private object QuartOutInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return 1 - (1 - input).pow(4)
        }
    }
}