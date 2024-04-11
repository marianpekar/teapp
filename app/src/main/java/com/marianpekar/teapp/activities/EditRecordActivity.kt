package com.marianpekar.teapp.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.RecordsStorage
import com.marianpekar.teapp.adapters.AdjustmentsAdapter
import com.marianpekar.teapp.data.Adjustment
import com.marianpekar.teapp.data.Record

class EditRecordActivity : SetupRecordActivityBase() {

    private lateinit var records : RecordsStorage
    private lateinit var record: Record
    private var recordIndex: Int = -1

    private lateinit var preferences: SharedPreferences
    private lateinit var infusionsPrefKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        records = RecordsStorage(this@EditRecordActivity)

        preferences = applicationContext.getSharedPreferences(getString(R.string.app_name), 0)

        setRecord()
        setUiReferences()
        setUnitsLabels()
        setTemperatureUnitsLabel()
        setAdjustmentsRecycler()
        setEditTexts()
        setBackButton()
        setSaveRecordButton()
        setInfusionConvenientButtons()
        setTemperatureConvenientButtons()
        setDeleteButton()
    }

    private fun setRecord() {
        recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1) {
            throw Exception("Record index is -1")
        }

        record = RecordsStorage(this@EditRecordActivity).getRecord(recordIndex)

        adjustments = record.getAdjustments().toMutableList()

        infusionsPrefKey = "infusions_${recordIndex}"
    }

    private fun setAdjustmentsRecycler() {
        recyclerAdjustments = findViewById(R.id.recyclerTimeAdjustments)
        recyclerAdjustments.layoutManager = LinearLayoutManager(this@EditRecordActivity)
        adapterAdjustments = AdjustmentsAdapter(adjustments, this@EditRecordActivity)
        recyclerAdjustments.adapter = adapterAdjustments
    }

    private fun setEditTexts() {
        editTextName.setText(record.getName())

        val totalSeconds = record.getSeconds()
        val minutes =  totalSeconds / 60
        val remainingSeconds = totalSeconds % 60

        editTextMinutes.setText(minutes.toString())

        editTextSeconds.setText(String.format("%02d", remainingSeconds))

        val weightDecimalPlaces = if (areUnitsImperial) 3 else 2
        val volumeDecimalPlaces = if (areUnitsImperial) 1 else 0
        editTextWeight.setText(String.format("%.${weightDecimalPlaces}f",record.getWeight(areUnitsImperial)))
        editTextVolume.setText(String.format("%.${volumeDecimalPlaces}f", record.getVolume(areUnitsImperial)))

        editTextInfusions.setText(record.getInfusions().toString())
        editTextInfusions.addTextChangedListener {
            val infusionsText = editTextInfusions.text.toString()

            if (infusionsText.isEmpty())
                return@addTextChangedListener

            val currentAdjustments = adjustments.toList()

            adjustments.clear()

            val infusions = if (infusionsText.isNotEmpty()) infusionsText.toInt() else 0

            if (infusions >= 2)
            {
                var i = 0
                while (i < infusions - 1)
                {
                    val currentAdjustment = currentAdjustments.getOrNull(i)
                    if (currentAdjustment != null)
                    {
                        adjustments.add(currentAdjustment)
                    }
                    else
                    {
                        adjustments.add(Adjustment(0))
                    }
                    i++
                }
            }

            setAdjustmentsRecycler()
        }

        editTextTemperature.setText(record.getTemperature(isTempInFahrenheit).toString())
    }

    private fun setSaveRecordButton() {
        val buttonSaveRecord: Button = findViewById(R.id.buttonSaveRecord)

        buttonSaveRecord.setOnClickListener {
            val name = editTextName.text.toString()

            val minutesText = editTextMinutes.text.toString()
            val secondsText = editTextSeconds.text.toString()

            val minutes = if (minutesText.isNotEmpty()) minutesText.toLong() else 0
            val seconds = if (secondsText.isNotEmpty()) secondsText.toLong() else 0

            val totalSeconds = if (minutes >= 1) {
                (minutes * 60) + seconds
            } else {
                seconds
            }

            val infusionsText = editTextInfusions.text.toString()
            val infusions = if (infusionsText.isNotEmpty()) infusionsText.toInt() else 0

            val gramsText = editTextWeight.text.toString()
            val grams = if (gramsText.isNotEmpty()) gramsText.toFloat() else 0.0f

            val millisText = editTextVolume.text.toString()
            val millis = if (millisText.isNotEmpty()) millisText.toFloat() else 0.0f

            val temperatureText = editTextTemperature.text.toString()
            val temperature = if (temperatureText.isNotEmpty()) temperatureText.toInt() else 0

            if (!records.areRecordParamsValid(name, grams, millis, temperature, totalSeconds, infusions)) {
                Toast.makeText(this@EditRecordActivity, R.string.cant_add_record, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!records.areAdjustmentsValid(adjustments, totalSeconds))
            {
                Toast.makeText(this@EditRecordActivity, R.string.cant_add_adjustment_longer_than_base, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            records.replaceRecord(recordIndex, Record(name, grams, millis, temperature, totalSeconds, infusions, adjustments, isTempInFahrenheit, areUnitsImperial))

            Toast.makeText(this@EditRecordActivity, R.string.record_saved, Toast.LENGTH_LONG).show()

            val editor = preferences.edit()
            editor.putInt(infusionsPrefKey, infusions)
            editor.apply()

            backToMainActivity()
        }
    }

    private fun setDeleteButton() {
        val deleteButton: Button = findViewById(R.id.buttonDeleteRecord)

        deleteButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)

            alertDialogBuilder.setTitle(R.string.delete_record)
            alertDialogBuilder.setMessage(R.string.delete_record_are_you_sure)

            alertDialogBuilder.setPositiveButton(R.string.yes) { dialog, _ ->
                records.removeRecord(recordIndex)
                backToMainActivity()
                dialog.dismiss()
                Toast.makeText(this@EditRecordActivity, R.string.record_removed, Toast.LENGTH_LONG).show()
            }

            alertDialogBuilder.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}