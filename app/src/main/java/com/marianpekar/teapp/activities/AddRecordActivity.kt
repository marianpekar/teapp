package com.marianpekar.teapp.activities

import android.content.Intent
import android.content.SharedPreferences
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
import com.marianpekar.teapp.adapters.AdjustmentsAdapter
import com.marianpekar.teapp.utilities.EditTextWatcherIntegerBoundaries
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.RecordsStorage
import com.marianpekar.teapp.utilities.EditTextWatcherSeconds
import com.marianpekar.teapp.data.Adjustment
import com.marianpekar.teapp.data.Record
import com.marianpekar.teapp.utilities.setupClearOnFocusBehavior

class AddRecordActivity : AppCompatActivity() {

    private lateinit var records : RecordsStorage

    private lateinit var editTextName: EditText
    private lateinit var editTextMinutes: EditText
    private lateinit var editTextSeconds: EditText
    private lateinit var editTextGrams: EditText
    private lateinit var editTextMillis: EditText
    private lateinit var editTextInfusions: EditText
    private lateinit var editTextTemperature: EditText

    private lateinit var recyclerAdjustments: RecyclerView
    private lateinit var adapterAdjustments : AdjustmentsAdapter
    private val adjustments : MutableList<Adjustment> = mutableListOf()

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
        setBackButton()
        setAddRecordButton()
        setInfusionConvenientButtons()
        setTemperatureConvenientButtons()
    }

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            backToMainActivity()
        }
    }

    private fun setUiReferences() {
        recyclerAdjustments = findViewById(R.id.recyclerTimeAdjustments)
        recyclerAdjustments.layoutManager = LinearLayoutManager(this@AddRecordActivity)

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextInfusions, 99, 1))
        editTextInfusions.addTextChangedListener {
            setAdjustmentsRecycler()
        }

        editTextTemperature = findViewById(R.id.editTextTemperature)
        editTextTemperature.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextTemperature, 100))
        editTextTemperature.setupClearOnFocusBehavior()

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
    }

    private fun setAdjustmentsRecycler() {
        val infusionsText = editTextInfusions.text.toString()

        if (infusionsText.isEmpty())
            return

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

        adapterAdjustments = AdjustmentsAdapter(adjustments, this@AddRecordActivity)

        recyclerAdjustments.adapter = adapterAdjustments
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

            records.addRecord(Record(name, grams, millis, temperature, totalSeconds, infusions, adapterAdjustments.getAdjustments()))

            Toast.makeText(this@AddRecordActivity, R.string.new_record_added, Toast.LENGTH_LONG).show()


            val recordIndex = records.getCount() - 1
            infusionsPrefKey = "infusions_${recordIndex}"
            val editor = preferences.edit()
            editor.putInt(infusionsPrefKey, infusions)
            editor.apply()

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