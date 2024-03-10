package com.marianpekar.teapp

class Record(
    private val name: String,
    private val grams: Float,
    private val milliliters: Int,
    private val temperature: Int,
    private val seconds: Long,
    private val infusions: Int
) {

    fun detailsFormatted(): String {
        return "${grams}g | ${milliliters}ml | ${temperature}° | ${timeFormatted()} | ${infusions}x"
    }

    private fun timeFormatted(): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun getTime(): Long {
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
}
