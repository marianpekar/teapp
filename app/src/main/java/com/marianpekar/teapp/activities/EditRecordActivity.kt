package com.marianpekar.teapp.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marianpekar.teapp.utilities.EditTextWatcherIntegerBoundaries
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.RecordsStorage
import com.marianpekar.teapp.utilities.EditTextWatcherSeconds
import com.marianpekar.teapp.adapters.AdjustmentsAdapter
import com.marianpekar.teapp.data.Adjustment
import com.marianpekar.teapp.data.Record
import com.marianpekar.teapp.utilities.setupClearOnFocusBehavior

class EditRecordActivity : AppCompatActivityLocale() {

    private lateinit var records : RecordsStorage
    private lateinit var record: Record
    private var recordIndex: Int = -1

    private lateinit var editTextInfusions: EditText
    private lateinit var editTextTemperature: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextMinutes: EditText
    private lateinit var editTextSeconds: EditText
    private lateinit var editTextGrams: EditText
    private lateinit var editTextMillis: EditText

    private lateinit var recyclerAdjustments: RecyclerView
    private lateinit var adapterAdjustments : AdjustmentsAdapter
    private var adjustments : MutableList<Adjustment> = mutableListOf()

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

    private fun setUiReferences()
    {
        editTextName = findViewById(R.id.editTextRecordName)
        editTextName.setupClearOnFocusBehavior()

        editTextMinutes = findViewById(R.id.editTextMinutes)
        editTextMinutes.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextMinutes, 99))
        editTextMinutes.setupClearOnFocusBehavior()

        editTextSeconds = findViewById(R.id.editTextSeconds)
        editTextSeconds.addTextChangedListener(EditTextWatcherSeconds(editTextSeconds))
        editTextSeconds.setupClearOnFocusBehavior()

        editTextGrams = findViewById(R.id.editTextGrams)
        editTextGrams.setupClearOnFocusBehavior()

        editTextMillis = findViewById(R.id.editTextMillis)
        editTextMillis.setupClearOnFocusBehavior()

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextInfusions, 99, 1))
        editTextInfusions.setupClearOnFocusBehavior()

        editTextTemperature = findViewById(R.id.editTextTemperature)
        editTextTemperature.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextTemperature, if (isTempInFahrenheit) 212 else 100))
        editTextTemperature.setupClearOnFocusBehavior()
    }

    private fun setTemperatureUnitsLabel() {
        val temperatureUnits: TextView = findViewById(R.id.textTemperatureUnit)
        temperatureUnits.text = if (isTempInFahrenheit) getString(R.string.deg_fahrenheit) else getString(R.string.deg_celsius)
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

        editTextGrams.setText(record.getGrams().toString())
        editTextMillis.setText(record.getMilliliters().toString())

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

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            backToMainActivity()
        }
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

            val gramsText = editTextGrams.text.toString()
            val grams = if (gramsText.isNotEmpty()) gramsText.toFloat() else 0.0f

            val millisText = editTextMillis.text.toString()
            val millis = if (millisText.isNotEmpty()) millisText.toInt() else 0

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

            records.replaceRecord(recordIndex, Record(name, grams, millis, temperature, totalSeconds, infusions, adjustments, isTempInFahrenheit))

            Toast.makeText(this@EditRecordActivity, R.string.record_saved, Toast.LENGTH_LONG).show()

            val editor = preferences.edit()
            editor.putInt(infusionsPrefKey, infusions)
            editor.apply()

            backToMainActivity()
        }
    }

    private fun backToMainActivity() {
        val intent = Intent(this@EditRecordActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setInfusionConvenientButtons() {
        val plusOneButton: Button = findViewById(R.id.buttonCounterPlusOne)
        val minusOneButton: Button = findViewById(R.id.buttonCounterMinusOne)

        plusOneButton.setOnClickListener {
            val infusionsText = editTextInfusions.text.toString()
            var infusions = if (infusionsText.isNotEmpty()) infusionsText.toInt() else 0

            infusions++
            editTextInfusions.setText(infusions.toString())
        }

        minusOneButton.setOnClickListener {
            val infusionsText = editTextInfusions.text.toString()
            var infusions = if (infusionsText.isNotEmpty()) infusionsText.toInt() else 0

            if (infusions <= 0)
                return@setOnClickListener

            infusions--
            editTextInfusions.setText(infusions.toString())
        }
    }

    private fun setTemperatureConvenientButtons() {
        val deg60Button: Button = findViewById(R.id.button60deg)
        val deg80Button: Button = findViewById(R.id.button80deg)
        val deg90Button: Button = findViewById(R.id.button90deg)

        deg60Button.setOnClickListener {
            editTextTemperature.setText(R.string.plain60)
        }

        deg80Button.setOnClickListener {
            editTextTemperature.setText(R.string.plain80)
        }

        deg90Button.setOnClickListener {
            editTextTemperature.setText(R.string.plain90)
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