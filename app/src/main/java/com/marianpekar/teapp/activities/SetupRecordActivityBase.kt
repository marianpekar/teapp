package com.marianpekar.teapp.activities

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marianpekar.teapp.R
import com.marianpekar.teapp.adapters.AdjustmentsAdapter
import com.marianpekar.teapp.data.Adjustment
import com.marianpekar.teapp.utilities.EditTextWatcherIntegerBoundaries
import com.marianpekar.teapp.utilities.EditTextWatcherSeconds
import com.marianpekar.teapp.utilities.setupClearOnFocusBehavior

open class SetupRecordActivityBase : AppCompatActivityLocale() {
    protected lateinit var editTextName: EditText
    protected lateinit var editTextMinutes: EditText
    protected lateinit var editTextSeconds: EditText
    protected lateinit var editTextWeight: EditText
    protected lateinit var editTextVolume: EditText
    protected lateinit var editTextInfusions: EditText
    protected lateinit var editTextTemperature: EditText

    protected lateinit var recyclerAdjustments: RecyclerView
    protected lateinit var adapterAdjustments : AdjustmentsAdapter

    protected var adjustments : MutableList<Adjustment> = mutableListOf()

    protected fun setUiReferences() {
        recyclerAdjustments = findViewById(R.id.recyclerTimeAdjustments)
        recyclerAdjustments.layoutManager = LinearLayoutManager(this)

        editTextInfusions = findViewById(R.id.editTextCounter)
        editTextInfusions.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextInfusions, 99, 1))
        editTextInfusions.addTextChangedListener {
            setAdjustmentsRecycler()
        }

        editTextTemperature = findViewById(R.id.editTextTemperature)
        editTextTemperature.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextTemperature, if (isTempInFahrenheit) 212 else 100))
        editTextTemperature.setupClearOnFocusBehavior()

        editTextName = findViewById(R.id.editTextRecordName)
        editTextName.setupClearOnFocusBehavior()

        editTextMinutes = findViewById(R.id.editTextMinutes)
        editTextMinutes.addTextChangedListener(EditTextWatcherIntegerBoundaries(editTextMinutes, 99))
        editTextMinutes.setupClearOnFocusBehavior()

        editTextSeconds = findViewById(R.id.editTextSeconds)
        editTextSeconds.addTextChangedListener(EditTextWatcherSeconds(editTextSeconds))
        editTextSeconds.setupClearOnFocusBehavior()

        editTextWeight = findViewById(R.id.editTextWeight)
        editTextWeight.setupClearOnFocusBehavior()

        editTextVolume = findViewById(R.id.editTextVolume)
        editTextVolume.setupClearOnFocusBehavior()
    }

    protected fun setUnitsLabels() {
        val textViewWeight = findViewById<TextView>(R.id.textViewWeight)
        textViewWeight.text = if (areUnitsImperial) getString(R.string.ounces_short) else getString(R.string.grams_short)

        val textViewVolume = findViewById<TextView>(R.id.textViewVolume)
        textViewVolume.text = if (areUnitsImperial) getString(R.string.ounces_fluid_short) else getString(R.string.millis_short)
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

        adapterAdjustments = AdjustmentsAdapter(adjustments, this)

        recyclerAdjustments.adapter = adapterAdjustments
    }

    protected fun setTemperatureUnitsLabel() {
        val temperatureUnits: TextView = findViewById(R.id.textTemperatureUnit)
        temperatureUnits.text = if (isTempInFahrenheit) getString(R.string.deg_fahrenheit) else getString(
            R.string.deg_celsius)
    }

    protected fun setInfusionConvenientButtons() {
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

    protected fun setTemperatureConvenientButtons() {
        val deg60Button: Button = findViewById(R.id.button60deg)
        val deg80Button: Button = findViewById(R.id.button80deg)
        val deg90Button: Button = findViewById(R.id.button90deg)

        deg60Button.text = if (isTempInFahrenheit) "140°" else "60°"
        deg80Button.text = if (isTempInFahrenheit) "176°" else "80°"
        deg90Button.text = if (isTempInFahrenheit) "194°" else "90°"

        deg60Button.setOnClickListener {
            editTextTemperature.setText(if (isTempInFahrenheit) "140" else "60")
        }

        deg80Button.setOnClickListener {
            editTextTemperature.setText(if (isTempInFahrenheit) "176" else "80")
        }

        deg90Button.setOnClickListener {
            editTextTemperature.setText(if (isTempInFahrenheit) "194" else "90")
        }
    }

    protected fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            backToMainActivity()
        }
    }

    protected fun backToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}