package com.marianpekar.teapp

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
            addRecord(Record("Sencha Satsuma", 3f, 250, 80, 120, 2))
            addRecord(Record("Sencha Miyazaki", 3f, 250, 60, 240, 3))
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

    fun removeRecord(index: Int) {
        records.removeAt(index)
        saveRecords()
    }

    private fun saveRecords() {
        val json = Gson().toJson(records)

        try {
            val outputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
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