package com.marianpekar.teapp

class Record(
    private val name: String,
    private val grams: Int,
    private val milliliters: Int,
    private val temperature: Int,
    private val seconds: Int,
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

    fun getName(): String {
        return name
    }
}
