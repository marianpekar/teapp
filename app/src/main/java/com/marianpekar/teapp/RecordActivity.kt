package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RecordActivity : AppCompatActivity(), CustomCountdownTimer.OnChangeHandler {

    private lateinit var timer: CustomCountdownTimer
    private lateinit var textViewStopWatch: TextView
    private lateinit var buttonStartStop: Button
    private lateinit var record: Record

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
    }

    private fun setRecord()
    {
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
        val buttonReset : Button = findViewById(R.id.buttonStopWatchReset)

        buttonStartStop.setOnClickListener {
            if (timer.isRunning()) {
                timer.pause()
                buttonStartStop.text = getString(R.string.start)
            }
            else {
                timer.start()
                buttonStartStop.text = getString(R.string.pause)
            }
        }

        buttonReset.setOnClickListener {
            timer.reset()
            buttonStartStop.text = getString(R.string.start)
        }
    }

    private fun formatTime(seconds: Long) : String {
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
    }
}