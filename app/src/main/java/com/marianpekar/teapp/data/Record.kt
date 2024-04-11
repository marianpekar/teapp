package com.marianpekar.teapp.data

class Record {

    private val name: String
    private val weight: Float
    private val volume: Float
    private val temperature: Int
    private val seconds: Long
    private val infusions: Int
    private val adjustments: List<Adjustment>

    constructor(
        name: String,
        weight: Float,
        volume: Float,
        temperature: Int,
        seconds: Long,
        infusions: Int,
        adjustments: List<Adjustment>,
        isTempInFahrenheit: Boolean,
        areUnitsImperial: Boolean
    ) {
        this.name = name
        this.weight = if (areUnitsImperial) weight * 28.3495f else weight
        this.volume = if (areUnitsImperial) volume * 28.4130f else volume
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

    fun summaryFormatted(isTempInFahrenheit: Boolean, areUnitsImperial: Boolean): String {
        return "${weightFormatted(areUnitsImperial)} | ${volumeFormatted(areUnitsImperial)} | ${getDisplayTemperature(isTempInFahrenheit)} | ${timeFormatted()} | ${infusions}x"
    }

    private fun getDisplayTemperature(isTempInFahrenheit: Boolean): String {
        return if (isTempInFahrenheit) {
            "${(temperature * 1.8).toInt() + 32}°F"
        } else {
            "$temperature°C"
        }
    }

    fun summaryWithAdjustmentsFormatted(isTempInFahrenheit: Boolean, areUnitsImperial: Boolean): String {

        for (adjustment in adjustments) {
            if (adjustment.seconds <= 0)
                continue

            var summary = "${weightFormatted(areUnitsImperial)} | ${volumeFormatted(areUnitsImperial)} | ${getDisplayTemperature(isTempInFahrenheit)} | ${timeFormatted()}"
            adjustments.forEach { summary += it.getSecondsFormatted()}
            return summary
        }

        return summaryFormatted(isTempInFahrenheit, areUnitsImperial)
    }

    private fun weightFormatted(areUnitsImperial: Boolean): String {
        val unit = if (areUnitsImperial) "oz" else "g"
        val decimalPlaces = if (areUnitsImperial) 3 else 2
        val format = "%.${decimalPlaces}f"
        val weight = getWeight(areUnitsImperial)
        return "${String.format(format, weight)}$unit"
    }

    private fun volumeFormatted(areUnitsImperial: Boolean): String {
        val unit = if (areUnitsImperial) "fl oz" else "ml"
        val decimalPlaces = if (areUnitsImperial) 1 else 0
        val format = "%.${decimalPlaces}f"
        val volume = getVolume(areUnitsImperial)
        return "${String.format(format, volume)}$unit"
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
        return volume / weight
    }

    fun getWeight(areUnitsImperial: Boolean): Float {
        return if (areUnitsImperial) weight / 28.3495f else weight
    }

    fun getVolume(areUnitsImperial: Boolean): Float {
        return if (areUnitsImperial) volume / 28.4130f else volume
    }

    fun getTemperature(inFahrenheit: Boolean): Int {
        return if (inFahrenheit) { (temperature * 1.8).toInt() + 32 } else { temperature }
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
