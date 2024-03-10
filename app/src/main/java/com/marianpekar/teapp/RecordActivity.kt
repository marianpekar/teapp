package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener

class RecordActivity : AppCompatActivity(), CustomCountdownTimer.OnChangeHandler {

    private lateinit var record: Record

    private lateinit var timer: CustomCountdownTimer
    private lateinit var textViewStopWatch: TextView
    private lateinit var buttonStartStop: Button

    private lateinit var textViewInfusionsCounter: TextView
    private var infusions: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setRecord()
        setHeader()
        setStopWatch()
        setInfusionCounter()
        setRatioCalculator()
    }

    private fun setRecord() {
        val recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1) {
            throw Exception("Record index is -1")
        }

        record = RecordsStorage(this@RecordActivity).getRecord(recordIndex)
    }

    private fun setHeader() {
        val textRecordName: TextView = findViewById(R.id.textRecordName)
        val textRecordSummary: TextView = findViewById(R.id.textRecordSummary)
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)

        textRecordName.text = record.getName()
        textRecordSummary.text = record.detailsFormatted()

        imageButtonLeftArrow.setOnClickListener {
            val intent = Intent(this@RecordActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setStopWatch() {
        textViewStopWatch = findViewById(R.id.textViewStopWatch)

        textViewStopWatch.text = formatTime(record.getTime())

        timer = CustomCountdownTimer(record.getTime() * 1000, 1000, this)

        buttonStartStop = findViewById(R.id.buttonStopWatchStartStop)

        buttonStartStop.setOnClickListener {
            if (timer.isRunning()) {
                timer.pause()
                buttonStartStop.text = getString(R.string.start)
            } else {
                timer.start()
                buttonStartStop.text = getString(R.string.pause)
            }
        }

        val buttonReset: Button = findViewById(R.id.buttonStopWatchReset)

        buttonReset.setOnClickListener {
            timer.reset()
            buttonStartStop.text = getString(R.string.start)
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onTimeChanged(remainingTimeMillis: Long) {
        textViewStopWatch.text = formatTime(remainingTimeMillis / 1000)
    }

    override fun onTimerFinished() {
        //TODO: Play sound
        buttonStartStop.text = getString(R.string.start)
        timer.reset()
        removeOneInfusion()
        Toast.makeText(this@RecordActivity, R.string.yourTeaIsReady, Toast.LENGTH_LONG).show()
    }

    private fun setInfusionCounter() {
        infusions = record.getInfusions()
        textViewInfusionsCounter = findViewById(R.id.textViewCounter)
        updateInfusionCounterText()

        val plusOneButton: Button = findViewById(R.id.buttonCounterPlusOne)
        val minusOneButton: Button = findViewById(R.id.buttonCounterMinusOne)
        val resetButton: Button = findViewById(R.id.buttonCounterReset)

        plusOneButton.setOnClickListener {
            if (infusions >= record.getInfusions())
                return@setOnClickListener

            infusions++
            updateInfusionCounterText()
        }

        minusOneButton.setOnClickListener {
            removeOneInfusion()
        }

        resetButton.setOnClickListener {
            infusions = record.getInfusions()
            updateInfusionCounterText()
        }
    }

    private fun removeOneInfusion() {
        if (infusions <= 0)
            return

        infusions--
        updateInfusionCounterText()
    }

    private fun updateInfusionCounterText() {
        textViewInfusionsCounter.text = infusions.toString()
    }

    private fun setRatioCalculator() {
        val editTextGrams: EditText = findViewById(R.id.editTextGrams)
        val editTextMillis: EditText = findViewById(R.id.editTextMillis)

        fun resetValues()
        {
            editTextGrams.setText(String.format("%.1f", record.getGrams()))
            editTextMillis.setText(record.getMilliliters().toString())
        }

        resetValues()

        val ratio = record.getRatio()

        var gramsTextChangedByUser = false
        var millisTextChangedByUser = false

        editTextGrams.addTextChangedListener {
            if (gramsTextChangedByUser)
                return@addTextChangedListener

            val gramsText = it.toString()
            if (gramsText.isNotEmpty()) {
                val gramsValue = gramsText.toFloat()

                if (gramsValue <= 0.0f) {
                    return@addTextChangedListener
                }

                val newMillis = (gramsValue * ratio).toInt()
                millisTextChangedByUser = true
                editTextMillis.setText(newMillis.toString())
                millisTextChangedByUser = false
            }

        }

        editTextMillis.addTextChangedListener {
            if (millisTextChangedByUser)
                return@addTextChangedListener

            val millisText = it.toString()
            if (millisText.isNotEmpty()) {
                val millisValue = millisText.toFloat()

                if (millisValue <= 0.0f) {
                    return@addTextChangedListener
                }

                val newGrams = millisValue / ratio
                gramsTextChangedByUser = true
                editTextGrams.setText(String.format("%.1f", newGrams))
                gramsTextChangedByUser = false
            }
        }

        val buttonResetRatio: Button = findViewById(R.id.buttonRatioReset)
        buttonResetRatio.setOnClickListener {
            gramsTextChangedByUser =  true
            millisTextChangedByUser = true
            resetValues()
            gramsTextChangedByUser = false
            millisTextChangedByUser = false
        }
    }
}