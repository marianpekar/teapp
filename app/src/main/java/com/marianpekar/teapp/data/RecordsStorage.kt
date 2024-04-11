package com.marianpekar.teapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class RecordsStorage(private val context: Context) {
    private val records: MutableList<Record> = mutableListOf()
    private val fileName = "teapp.json"

    init {
        loadRecords()
    }

    fun getAllRecords(): List<Record> {
        return records
    }

    fun getRecord(index: Int): Record {
        return records[index]
    }

    fun addRecord(record: Record) {
        records.add(record)
        saveRecords()
    }

    fun replaceRecord(index: Int, record: Record) {
        records[index] = record
        saveRecords()
    }

    fun removeRecord(index: Int) {
        records.removeAt(index)
        saveRecords()
    }

    fun setNotes(index: Int, notes: String) {
        records[index].setNotes(notes)
        saveRecords()
    }

    fun getNotes(index: Int) : String {
        return records[index].getNotes()
    }

    fun areRecordParamsValid(
        name: String,
        weight: Float,
        volume: Float,
        temperature: Int,
        seconds: Long,
        infusions: Int
    ): Boolean {
        return name.isNotEmpty() && weight > 0 && volume > 0 && temperature > 0 && seconds > 0 && infusions > 0
    }

    fun areAdjustmentsValid(adjustments: List<Adjustment>, seconds: Long): Boolean {
        for (adjustment in adjustments) {
            if (!adjustment.getIsNegative())
                continue

            if (adjustment.seconds >= seconds)
                return false
        }

        return true
    }

    private fun saveRecords() {
        val json = Gson().toJson(records)

        try {
            val outputStream: FileOutputStream =
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(json.toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadRecords() {
        try {
            val inputStream: FileInputStream = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()

            val typeToken = object : TypeToken<List<Record>>() {}.type
            records.addAll(Gson().fromJson(json, typeToken))

            inputStream.close()
        } catch (e: FileNotFoundException) {
            createAndSaveDefaultRecords()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createAndSaveDefaultRecords() {
        addRecord(Record("Satsuma", 2f, 250.0f, 80, 90, 2, listOf(Adjustment(30)), false, false))
        addRecord(Record("Miyazaki", 2f, 250.0f, 60, 240, 3, listOf(Adjustment(0), Adjustment(0)),false, false))
        addRecord(Record("Natsu", 2f, 250.0f, 80, 60, 3, listOf(Adjustment(15), Adjustment(30)),false, false))
        saveRecords()
    }

    fun exportRecords(): String {
        return Gson().toJson(records)
    }

    fun importRecords(json: String) {
        try {
            records.clear()

            val typeToken = object : TypeToken<List<Record>>(){}.type
            val importedRecords: List<Record> = Gson().fromJson(json, typeToken)

            records.addAll(importedRecords)
            saveRecords()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCount(): Int {
        return records.size
    }
}