package com.marianpekar.teapp

class Adjustment(
    var seconds: Long
) {
    fun getSecondsAdjustmentFormatted(): String {
        if (seconds == 0L)
            return "0s"

        return " ${if (seconds >= 0) "+" else "-"}${seconds}s"
    }
}