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


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val pointer = onPointerEvent(event)
        val source = super.onInterceptTouchEvent(event)
        return pointer ?: source
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointer = onPointerEvent(event)
        val source = super.onTouchEvent(event)
        return pointer ?: source
    }

    override fun onScrollChanged(xCurrent: Int, yCurrent: Int, xPrevious: Int, yPrevious: Int) {
        super.onScrollChanged(xCurrent, yCurrent, xPrevious, yPrevious)
        onScrolled(yCurrent, yPrevious)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child != null) content = child
    }


    private fun onPointerEvent(event: MotionEvent): Boolean? {
        if (getScrollMax() == 0) {
            if (content.translationY != 0f && !animator.isRunning) moveToDefaultPosition()
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> onPointerDown(event)
            MotionEvent.ACTION_MOVE -> onPointerMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> onPointerUp(event)
        }
        return if (abs(content.translationY) > scaledTouchSlop) true else null
    }

    private fun onPointerDown(event: MotionEvent) {
        val index = event.actionIndex
        if (animator.isRunning && abs(content.translationY) > scaledTouchSlop) animator.cancel()

        touched = true
        pointerId = event.getPointerId(index)
        pointerPrevious = event.getY(index)
    }

    private fun onPointerMove(event: MotionEvent) {
        val index = event.findPointerIndex(pointerId)

        if (index >= 0) {
            val pointerCurrent = event.getY(index)
            var pointerOffset = pointerCurrent - pointerPrevious

            if (!canScroll) {
                if (abs(pointerOffset) > scaledTouchSlop) {
                    canScroll = true
                    if (animator.isRunning) animator.cancel()
                    if (pointerOffset > 0) pointerOffset -= scaledTouchSlop
                    if (pointerOffset < 0) pointerOffset += scaledTouchSlop
                } else {
                    return
                }
            }

            pointerPrevious = pointerCurrent

            if (scrollY == 0) {
                if (pointerOffset > 0) {
                    content.translationY += getDamping(pointerOffset)
                } else {
                    content.translationY += pointerOffset
                }
                content.translationY = min(max(content.translationY, 0f), height / 2f)
            } else if (scrollY == getScrollMax()) {
                if (pointerOffset < 0) {
                    content.translationY += getDamping(pointerOffset)
                } else {
                    content.translationY += pointerOffset
                }
                content.translationY = min(max(content.translationY, height / -2f), 0f)
            }
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
                moveToDefaultPosition()
            }
        }
    }

    private fun onScrolled(yCurrent: Int, yPrevious: Int) {
        val yOffset = yCurrent - yPrevious
        val scrollMax = getScrollMax()
        val overScrollDirection = getOverScrollDirection()

        if (!touched && overScrollDirection == 0 && ((yCurrent == 0 && yOffset < 0) || (yCurrent == scrollMax && yOffset > 0))) {
            content.translationY = -yOffset.toFloat()
            moveToDefaultPosition()
        } else if ((overScrollDirection > 0 && yOffset > 0) || (overScrollDirection < 0 && yOffset < 0)) {
            scrollY -= yOffset
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
        return offset / (1.5f / (1 - (abs(content.translationY) / (height / 2)).pow(2)))
    }

    private fun getScrollMax(): Int {
        return max(content.height - height, 0)
    }

    private fun getOverScrollDirection(): Int {
        if (content.translationY > 0) return 1
        if (content.translationY < 0) return -1
        return 0
    }


    private object QuartOutInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return 1 - (1 - input).pow(4)
        }
    }
}