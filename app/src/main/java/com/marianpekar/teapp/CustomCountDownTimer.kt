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

    interface OnChangeHandler {
        fun onTimeChanged(remainingTimeMillis: Long)
        fun onTimerFinished()
    }

    init {
        setupCountDownTimer()
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
        countDownTimer.start()
        isRunning = true
    }

    fun pause() {
        countDownTimer.cancel()
        isRunning = false
    }

    fun reset() {
        countDownTimer.cancel()
        remainingTimeMillis = initialTimeMillis
        onChangeHandler.onTimeChanged(remainingTimeMillis)
        isRunning = false
        setupCountDownTimer()
    }

    fun isRunning(): Boolean {
        return isRunning
    }
}


