package com.marianpekar.teapp

class Record(
    private val name: String,
    private val grams: Float,
    private val milliliters: Int,
    private val temperature: Int,
    private val seconds: Long,
    private val infusions: Int,
    private val adjustments: List<Adjustment>
) {

    fun summaryFormatted(): String {
        return "${grams}g | ${milliliters}ml | ${temperature}° | ${timeFormatted()} | ${infusions}x"
    }

    fun summaryWithAdjustmentsFormatted(): String {

        for (adjustment in adjustments) {
            if (adjustment.seconds <= 0)
                continue

            var summary = "${grams}g | ${milliliters}ml | ${temperature}° | ${timeFormatted()}"
            adjustments.forEach { summary += it.getSecondsFormatted()}
            return summary
        }

        return summaryFormatted()
    }

    private fun timeFormatted(): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun getSeconds(): Long {
        return seconds
    }

    fun getName(): String {
        return name
    }

    fun getInfusions(): Int {
        return infusions
    }

    fun getRatio(): Float {
        return milliliters / grams
    }

    fun getGrams(): Float {
        return grams
    }

    fun getMilliliters(): Int {
        return milliliters
    }

    fun getTemperature(): Int {
        return temperature
    }

    fun getAdjustments(): List<Adjustment> {
        return adjustments
    }

    fun getSecondsAdjusted(infusion: Int) : Long {
        if(!shouldAdjust(adjustments, infusion))
            return getSeconds()

        val adjustment = adjustments[getInfusions() - infusion - 1]
        return getSeconds() + if (adjustment.getIsNegative()) adjustment.seconds * -1 else adjustment.seconds
    }

    private fun shouldAdjust(adjustments: List<Adjustment>, infusion: Int) : Boolean {
        return adjustments.isNotEmpty() && infusion < getInfusions() && infusion > 0
    }
}
