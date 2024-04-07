package com.marianpekar.teapp.data

class Record {

    private val name: String
    private val grams: Float
    private val milliliters: Int
    private val temperature: Int
    private val seconds: Long
    private val infusions: Int
    private val adjustments: List<Adjustment>

    constructor(
        name: String,
        grams: Float,
        milliliters: Int,
        temperature: Int,
        seconds: Long,
        infusions: Int,
        adjustments: List<Adjustment>,
        isTempInFahrenheit: Boolean
    ) {
        this.name = name
        this.grams = grams
        this.milliliters = milliliters
        this.temperature = if (isTempInFahrenheit) ((temperature - 32) / 1.8).toInt() else temperature
        this.seconds = seconds
        this.infusions = infusions
        this.adjustments = adjustments
    }

    private var notes = ""

    fun setNotes(newNotes: String) {
        notes = newNotes
    }

    fun getNotes(): String {
        return notes
    }

    fun summaryFormatted(isTempInFahrenheit: Boolean): String {
        return "${grams}g | ${milliliters}ml | ${getDisplayTemperature(isTempInFahrenheit)} | ${timeFormatted()} | ${infusions}x"
    }

    private fun getDisplayTemperature(isTempInFahrenheit: Boolean): String {
        return if (isTempInFahrenheit) {
            "${(temperature * 1.8).toInt() + 32}°F"
        } else {
            "$temperature°C"
        }
    }

    fun summaryWithAdjustmentsFormatted(isTempInFahrenheit: Boolean): String {

        for (adjustment in adjustments) {
            if (adjustment.seconds <= 0)
                continue

            var summary = "${grams}g | ${milliliters}ml | ${getDisplayTemperature(isTempInFahrenheit)} | ${timeFormatted()}"
            adjustments.forEach { summary += it.getSecondsFormatted()}
            return summary
        }

        return summaryFormatted(isTempInFahrenheit)
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
