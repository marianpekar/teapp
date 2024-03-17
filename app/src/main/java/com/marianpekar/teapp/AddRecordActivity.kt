package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AddRecordActivity : AppCompatActivity() {

    private lateinit var records : RecordsStorage

    private lateinit var editTextInfusions: EditText
    private lateinit var editTextTemperature: EditText

    private lateinit var recyclerAdjustments: RecyclerView
    private lateinit var adapterAdjustments : AdjustmentsAdapter
    private val adjustments : MutableList<Adjustment> = mutableListOf()

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

        recyclerAdjustments = findViewById(R.id.recyclerTimeAdjustments)
        recyclerAdjustments.layoutManager = LinearLayoutManager(this@AddRecordActivity)

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.addTextChangedListener {
            setAdjustmentsRecycler()
        }

        editTextTemperature = findViewById(R.id.editTextTemperature)
        editTextTemperature.setupClearOnFocusBehavior()

        setBackButton()
        setAddRecordButton()
        setInfusionConvenientButtons()
        setTemperatureConvenientButtons()
        setTemperatureEditText()
    }

    private fun setAdjustmentsRecycler() {
        adjustments.clear()

        val infusionsText = editTextInfusions.text.toString()
        val infusions = if (infusionsText.isNotEmpty()) infusionsText.toInt() else 0

        if (infusions >= 2)
        {
            var i = 0
            while (i < infusions - 1)
            {
                adjustments.add(Adjustment(0))
                i++
            }
        }

        adapterAdjustments = AdjustmentsAdapter(adjustments, this@AddRecordActivity)

        recyclerAdjustments.adapter = adapterAdjustments
    }

    private fun setTemperatureEditText() {
        val temperatureTextWatcher = TemperatureTextWatcher(editTextTemperature)
        editTextTemperature.addTextChangedListener(temperatureTextWatcher)
    }

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            backToMainActivity()
        }
    }

    private fun setAddRecordButton() {
        val editTextName: EditText = findViewById(R.id.editTextRecordName)
        editTextName.setupClearOnFocusBehavior()

        val editTextMinutes: EditText = findViewById(R.id.editTextMinutes)
        editTextMinutes.setupClearOnFocusBehavior()

        val editTextSeconds: EditText = findViewById(R.id.editTextSeconds)
        editTextSeconds.setupClearOnFocusBehavior()

        val secondsTextWatcher = SecondsTextWatcher(editTextSeconds)
        editTextSeconds.addTextChangedListener(secondsTextWatcher)

        val editTextGrams: EditText = findViewById(R.id.editTextGrams)
        editTextGrams.setupClearOnFocusBehavior()

        val editTextMillis: EditText = findViewById(R.id.editTextMillis)
        editTextMillis.setupClearOnFocusBehavior()

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

            records.addRecord(Record(name, grams, millis, temperature, totalSeconds, infusions, adapterAdjustments.getAdjustments()))

            Toast.makeText(this@AddRecordActivity, R.string.new_record_added, Toast.LENGTH_LONG).show()

            backToMainActivity()
        }
    }

    private fun backToMainActivity() {
        val intent = Intent(this@AddRecordActivity, MainActivity::class.java)
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
}