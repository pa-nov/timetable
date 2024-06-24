package com.panov.timetable.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.core.widget.NestedScrollView
import kotlin.math.abs
import kotlin.math.pow

class BounceScrollView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : NestedScrollView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val bounceDamping = 2f
    private val bounceDelay = 400L

    private var touchStart = -1000f
    private var deltaPrevious = 0
    private var overScrolledDistance = 0

    private lateinit var childView: View
    private lateinit var animator: ObjectAnimator

    init {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        isFillViewport = true
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!::childView.isInitialized && childCount > 0 || childView != getChildAt(0)) {
            childView = getChildAt(0)
        }
        if (touchStart == -1000f) {
            touchStart = event.y
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::childView.isInitialized) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStart = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val touchCurrent = event.y
                val delta = touchStart - touchCurrent
                val dampingDelta = (delta / calculateDumping()).toInt()
                touchStart = touchCurrent

                var onePointerTouch = true
                if (deltaPrevious <= 0 && dampingDelta > 0) onePointerTouch = false
                if (deltaPrevious >= 0 && dampingDelta < 0) onePointerTouch = false
                deltaPrevious = dampingDelta

                if (onePointerTouch && canMove(dampingDelta)) {
                    overScrolledDistance += dampingDelta
                    childView.translationY = overScrolledDistance * -1f
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchStart = -1000f
                deltaPrevious = 0
                overScrolledDistance = 0

                moveToDefaultPosition()
            }
        }

        return super.onTouchEvent(event)
    }

    private fun calculateDumping(): Float = bounceDamping / (1f - (abs(childView.translationY) / childView.measuredHeight).pow(2))

    private fun canMove(delta: Int): Boolean = if (delta < 0) canMoveFromStart() else canMoveFromEnd()

    private fun canMoveFromStart(): Boolean = scrollY == 0

    private fun canMoveFromEnd(): Boolean {
        var offset = childView.measuredHeight - height
        offset = if (offset < 0) 0 else offset
        return scrollY == offset
    }

    private fun moveToDefaultPosition() {
        if (::animator.isInitialized && animator.isRunning) animator.cancel()
        animator = ObjectAnimator.ofFloat(childView, View.TRANSLATION_Y, 0f)
        animator.setDuration(bounceDelay).interpolator = QuartOutInterpolator
        animator.start()
    }


    private object QuartOutInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return (1f - (1 - input).pow(4))
        }
    }
}