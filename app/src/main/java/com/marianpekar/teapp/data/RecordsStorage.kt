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

        if (records.isEmpty()) {
            // Add a default records when the class is initialized with no existing records
            addRecord(Record("Satsuma", 2f, 250, 80, 90, 2, listOf(Adjustment(30))))
            addRecord(Record("Miyazaki", 2f, 250, 60, 240, 3, listOf(Adjustment(0), Adjustment(0))))
            addRecord(Record("Natsu", 2f, 250, 80, 60, 3, listOf(Adjustment(15), Adjustment(30))))
            saveRecords()
        }
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

    fun areRecordParamsValid(
        name: String,
        grams: Float,
        millis: Int,
        temperature: Int,
        seconds: Long,
        infusions: Int
    ): Boolean {
        return name.isNotEmpty() && grams > 0 && millis > 0 && temperature > 0 && seconds > 0 && infusions > 0
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
            // File not found, ignore
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}