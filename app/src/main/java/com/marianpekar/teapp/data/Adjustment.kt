package com.marianpekar.teapp.data

class Adjustment(
    var seconds: Long,
) {
    private var isNegative: Boolean = false

    fun getSecondsFormatted(): String {
        if (seconds == 0L)
            return " 0s"

        return " ${if (isNegative) "-" else "+"}${seconds}s"
    }

    fun flipSign() {
        isNegative = !isNegative
    }

    fun getIsNegative(): Boolean {
        return isNegative
    }
}