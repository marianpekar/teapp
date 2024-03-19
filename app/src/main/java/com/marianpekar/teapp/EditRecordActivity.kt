package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditRecordActivity : AppCompatActivity() {

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

        setUiReferences()

        setRecord()

        setAdjustmentsRecycler()

        setEditTexts()
        setBackButton()
        setSecondsEditText()
        setTemperatureEditText()
        setSaveRecordButton()
        setInfusionConvenientButtons()
        setTemperatureConvenientButtons()
        setDeleteButton()
    }

    private fun setUiReferences()
    {
        editTextName = findViewById(R.id.editTextRecordName)
        editTextName.setupClearOnFocusBehavior()

        editTextMinutes = findViewById(R.id.editTextMinutes)
        editTextMinutes.setupClearOnFocusBehavior()

        editTextSeconds = findViewById(R.id.editTextSeconds)
        editTextSeconds.setupClearOnFocusBehavior()

        editTextGrams = findViewById(R.id.editTextGrams)
        editTextGrams.setupClearOnFocusBehavior()

        editTextMillis = findViewById(R.id.editTextMillis)
        editTextMillis.setupClearOnFocusBehavior()

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.setupClearOnFocusBehavior()

        editTextTemperature = findViewById(R.id.editTextTemperature)
        editTextTemperature.setupClearOnFocusBehavior()

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.setupClearOnFocusBehavior()

        recyclerAdjustments = findViewById(R.id.recyclerTimeAdjustments)
        recyclerAdjustments.layoutManager = LinearLayoutManager(this@EditRecordActivity)
    }

    private fun setAdjustmentsRecycler() {
        adapterAdjustments = AdjustmentsAdapter(adjustments, this@EditRecordActivity)
        recyclerAdjustments.adapter = adapterAdjustments
    }

    private fun setSecondsEditText() {
        val secondsTextWatcher = SecondsTextWatcher(editTextSeconds)
        editTextSeconds.addTextChangedListener(secondsTextWatcher)
    }

    private fun setTemperatureEditText() {
        val temperatureTextWatcher = TemperatureTextWatcher(editTextTemperature)
        editTextTemperature.addTextChangedListener(temperatureTextWatcher)
    }

    private fun setRecord() {
        recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1) {
            throw Exception("Record index is -1")
        }

        record = RecordsStorage(this@EditRecordActivity).getRecord(recordIndex)
        adjustments = record.getAdjustments().toMutableList()
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

            val currentAdjustments = adjustments.toList()

            adjustments.clear()

            val infusionsText = editTextInfusions.text.toString()
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

        editTextTemperature.setText(record.getTemperature().toString())
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

            records.replaceRecord(recordIndex, Record(name, grams, millis, temperature, totalSeconds, infusions, adjustments))

            Toast.makeText(this@EditRecordActivity, R.string.record_saved, Toast.LENGTH_LONG).show()

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