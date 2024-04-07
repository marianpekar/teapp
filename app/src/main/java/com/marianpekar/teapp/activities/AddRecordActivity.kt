package com.marianpekar.teapp.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.RecordsStorage
import com.marianpekar.teapp.data.Record

class AddRecordActivity : SetupRecordActivityBase() {

    private lateinit var records : RecordsStorage

    private lateinit var preferences: SharedPreferences
    private lateinit var infusionsPrefKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        records = RecordsStorage(this@AddRecordActivity)

        preferences = applicationContext.getSharedPreferences(getString(R.string.app_name), 0)

        setUiReferences()
        setTemperatureUnitsLabel()
        setBackButton()
        setAddRecordButton()
        setInfusionConvenientButtons()
        setTemperatureConvenientButtons()
    }

    private fun setAddRecordButton() {
        val buttonAddRecord: Button = findViewById(R.id.buttonSaveRecord)

        buttonAddRecord.setOnClickListener {
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

            val gramsText = editTextGrams.text.toString()
            val grams = if (gramsText.isNotEmpty()) gramsText.toFloat() else 0.0f

            val millisText = editTextMillis.text.toString()
            val millis = if (millisText.isNotEmpty()) millisText.toInt() else 0

            val temperatureText = editTextTemperature.text.toString()
            val temperature = if (temperatureText.isNotEmpty()) temperatureText.toInt() else 0

            if (!records.areRecordParamsValid(name, grams, millis, temperature, totalSeconds, infusions)) {
                Toast.makeText(this@AddRecordActivity, R.string.cant_add_record, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!records.areAdjustmentsValid(adjustments, totalSeconds))
            {
                Toast.makeText(this@AddRecordActivity, R.string.cant_add_adjustment_longer_than_base, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            records.addRecord(Record(name, grams, millis, temperature, totalSeconds, infusions, adapterAdjustments.getAdjustments(), isTempInFahrenheit))

            Toast.makeText(this@AddRecordActivity, R.string.new_record_added, Toast.LENGTH_LONG).show()


            val recordIndex = records.getCount() - 1
            infusionsPrefKey = "infusions_${recordIndex}"
            val editor = preferences.edit()
            editor.putInt(infusionsPrefKey, infusions)
            editor.apply()

            backToMainActivity()
        }
    }
}