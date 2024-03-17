package com.marianpekar.teapp

import android.os.CountDownTimer

class CustomCountdownTimer(
    private val initialTimeMillis: Long,
    private val intervalMillis: Long,
    private val onChangeHandler: OnChangeHandler
) {

    private var remainingTimeMillis: Long = initialTimeMillis
    private lateinit var countDownTimer: CountDownTimer
    private var isRunning: Boolean = false
    private var remainingTimeMillisWhenPaused: Long = initialTimeMillis

    init {
        setupCountDownTimer()
    }

    interface OnChangeHandler {
        fun onTimeChanged(remainingTimeMillis: Long)
        fun onTimerFinished()
    }

    private fun setupCountDownTimer() {
        countDownTimer = object : CountDownTimer(remainingTimeMillis, intervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                onChangeHandler.onTimeChanged(remainingTimeMillis)
            }

            override fun onFinish() {
                onChangeHandler.onTimerFinished()
            }
        }
    }

    fun start() {
        remainingTimeMillis = remainingTimeMillisWhenPaused
        setupCountDownTimer()
        countDownTimer.start()
        isRunning = true
    }

    fun pause() {
        remainingTimeMillisWhenPaused = remainingTimeMillis
        countDownTimer.cancel()
        isRunning = false
    }

    fun reset() {
        remainingTimeMillis = initialTimeMillis
        remainingTimeMillisWhenPaused = initialTimeMillis
        onChangeHandler.onTimeChanged(remainingTimeMillis)
        countDownTimer.cancel()
        setupCountDownTimer()
        isRunning = false
    }

    fun isRunning(): Boolean {
        return isRunning
    }
}


