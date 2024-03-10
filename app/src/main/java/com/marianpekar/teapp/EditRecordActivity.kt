package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        records = RecordsStorage(this@EditRecordActivity)

        editTextName = findViewById(R.id.editTextRecordName)
        editTextMinutes = findViewById(R.id.editTextMinutes)
        editTextSeconds = findViewById(R.id.editTextSeconds)
        editTextGrams = findViewById(R.id.editTextGrams)
        editTextMillis = findViewById(R.id.editTextMillis)
        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextTemperature = findViewById(R.id.editTextTemperature)

        setRecord()
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
    }

    private fun setEditTexts() {
        editTextName.setText(record.getName())

        val totalSeconds = record.getTime()
        val minutes =  totalSeconds / 60
        val remainingSeconds = totalSeconds % 60

        editTextMinutes.setText(minutes.toString())
        editTextSeconds.setText(remainingSeconds.toString())

        editTextGrams.setText(record.getGrams().toString())
        editTextMillis.setText(record.getMilliliters().toString())
        editTextInfusions.setText(record.getInfusions().toString())
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

            val totalSeconds = if (minutes > 1) {
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

            if (!records.areRecordParamsValid(name, grams, millis, temperature, totalSeconds, infusions))
            {
                Toast.makeText(this@EditRecordActivity, R.string.cant_add_record, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            records.replaceRecord(recordIndex, Record(name, grams, millis, temperature, totalSeconds, infusions))

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