package com.marianpekar.teapp

class Adjustment(
    var seconds: Long,
) {
    private var isNegative: Boolean = false

    fun getSecondsAdjustmentFormatted(): String {
        if (seconds == 0L)
            return " 0s"

        return " ${if (seconds >= 0) "+" else "-"}${seconds}s"
    }

    fun flipSign() {
        isNegative = !isNegative
    }

    fun getIsNegative(): Boolean {
        return isNegative
    }
}