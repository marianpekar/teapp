package com.marianpekar.teapp.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.marianpekar.teapp.utilities.CustomCountdownTimer
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.RecordsStorage
import com.marianpekar.teapp.data.Record
import com.marianpekar.teapp.services.NotificationService
import com.marianpekar.teapp.utilities.setupClearOnFocusBehavior
import kotlin.reflect.KClass


class RecordActivity : AppCompatActivityLocale(), CustomCountdownTimer.OnChangeHandler {

    private lateinit var record: Record
    private var recordIndex: Int = -1

    private lateinit var timer: CustomCountdownTimer
    private lateinit var textViewStopWatch: TextView
    private lateinit var buttonStartStop: Button
    private lateinit var plusOneButton: Button
    private lateinit var minusOneButton: Button
    private lateinit var resetInfusionsButton: Button
    private lateinit var resetTimerButton: Button
    private lateinit var editTextGrams: EditText
    private lateinit var editTextMillis: EditText
    private lateinit var buttonNotes: ImageButton

    private lateinit var textViewInfusionsCounter: TextView
    private var infusions: Int = 0

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var preferences: SharedPreferences
    private lateinit var infusionsPrefKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        stopService(Intent(this, NotificationService::class.java))

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mediaPlayer = MediaPlayer.create(this@RecordActivity, R.raw.flute_shot)

        preferences = applicationContext.getSharedPreferences(getString(R.string.app_name), 0)

        setUiReferences()

        setRecord()
        setInfusions()

        setBackButton()
        setOnBackPressedCallback()

        setNotesButton()
        setHeader()

        setTimer()
        setTimerText()
        setTimerButtons()

        setInfusionsCounter()
        setRatioCalculator()
    }

    private fun setUiReferences() {
        textViewStopWatch = findViewById(R.id.textViewStopWatch)
        buttonStartStop = findViewById(R.id.buttonStopWatchStartStop)
        textViewInfusionsCounter = findViewById(R.id.textViewCounter)
        plusOneButton = findViewById(R.id.buttonCounterPlusOne)
        minusOneButton = findViewById(R.id.buttonCounterMinusOne)
        resetInfusionsButton = findViewById(R.id.buttonCounterReset)
        resetTimerButton = findViewById(R.id.buttonStopWatchReset)
        buttonNotes = findViewById(R.id.buttonNotes)

        editTextGrams = findViewById(R.id.editTextGrams)
        editTextGrams.setupClearOnFocusBehavior()

        editTextMillis = findViewById(R.id.editTextMillis)
        editTextMillis.setupClearOnFocusBehavior()
    }

    override fun onStop() {
        super.onStop()

        val editor = preferences.edit()
        editor.putInt(infusionsPrefKey, textViewInfusionsCounter.text.toString().toInt())
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.release()
    }

    private fun setRecord() {
        recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1) {
            throw Exception("Record index is -1")
        }

        record = RecordsStorage(this@RecordActivity).getRecord(recordIndex)

        infusionsPrefKey = "infusions_${recordIndex}"
    }

    private fun setInfusions() {
        infusions = preferences.getInt(infusionsPrefKey, 0)

        if (infusions == 0) {
            infusions = record.getInfusions()
        }
    }

    private fun setHeader() {
        val textRecordName: TextView = findViewById(R.id.textRecordName)
        val textRecordSummary: TextView = findViewById(R.id.textRecordSummary)

        textRecordName.text = record.getName()
        textRecordSummary.text = record.summaryWithAdjustmentsFormatted()
    }

    private fun setNotesButton() {
        buttonNotes.setOnClickListener {
            toActivityWithDialogWhenTimerIsRunning(RecordNotesActivity::class)
        }
    }

    private fun setOnBackPressedCallback() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                toActivityWithDialogWhenTimerIsRunning(MainActivity::class)
            }
        }
        this@RecordActivity.onBackPressedDispatcher.addCallback(this, callback);
    }

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            toActivityWithDialogWhenTimerIsRunning(MainActivity::class)
        }
    }

    private fun toActivityWithDialogWhenTimerIsRunning(targetActivity: KClass<*>) {
        if (timer.isRunning()) {
            val alertDialogBuilder = AlertDialog.Builder(this)

            alertDialogBuilder.setTitle(R.string.are_you_sure)
            alertDialogBuilder.setMessage(R.string.leaving_activity_stops_timer)

            alertDialogBuilder.setPositiveButton(R.string.yes) { dialog, _ ->
                leaveActivity(targetActivity)
                dialog.dismiss()
            }

            alertDialogBuilder.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        else {
            leaveActivity(targetActivity)
        }
    }

    private fun leaveActivity(targetActivity: KClass<*>) {
        val intent = Intent(this@RecordActivity, targetActivity.java)
        intent.putExtra("position", recordIndex)
        startActivity(intent)
        timer.pause()
        finish()
    }

    private fun setTimerText() {
        if (infusions > 0) {
            textViewStopWatch.text = formatTime(record.getSecondsAdjusted(infusions))
        } else {
            textViewStopWatch.text = getString(R.string.default_stopwatch_value)
        }
    }

    private fun setTimer() {
        timer = CustomCountdownTimer(record.getSecondsAdjusted(infusions) * 1000, 1000, this)
    }

    private fun setTimerButtons() {
        buttonStartStop.setOnClickListener {
            if (timer.isRunning()) {
                timer.pause()
                buttonStartStop.text = getString(R.string.start)
            } else {

                if (infusions <= 0) {
                    Toast.makeText(
                        this@RecordActivity,
                        R.string.no_infusions_left,
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                timer.start()
                buttonStartStop.text = getString(R.string.pause)

                disableInfusionButtons()
            }
        }

        resetTimerButton.setOnClickListener {
            if (infusions <= 0) {
                Toast.makeText(this@RecordActivity, R.string.no_infusions_left, Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            timer.reset()
            buttonStartStop.text = getString(R.string.start)

            enableInfusionsButtons()
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
        buttonStartStop.text = getString(R.string.start)

        enableInfusionsButtons()

        removeOneInfusion()

        if (infusions > 0) {
            setTimer()
            setTimerText()
        }

        if (!isAppInForeground(this@RecordActivity)) {
            pushNotification()
        } else {
            Toast.makeText(this@RecordActivity, R.string.your_tea_is_ready, Toast.LENGTH_LONG).show()
            playSoundOrVibrate()
        }
    }

    private fun pushNotification() {
        val serviceIntent = Intent(this@RecordActivity, NotificationService::class.java)
        serviceIntent.putExtra("position", recordIndex)
        ContextCompat.startForegroundService(this@RecordActivity, serviceIntent)
    }

    private fun playSoundOrVibrate() {
        val audioManager = this@RecordActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (currentVolume == 0) {
            vibrate()
        } else {
            mediaPlayer.start()
        }
    }

    private fun vibrate() {
        val duration = 1000L
        val vibrator = getSystemService(Vibrator::class.java)

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0 (API level 26) and above
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // For devices below Android 8.0
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }

    private fun enableInfusionsButtons() {
        setInfusionButtonsEnabled(true)
    }

    private fun disableInfusionButtons() {
        setInfusionButtonsEnabled(false)
    }

    private fun setInfusionButtonsEnabled(value: Boolean) {
        plusOneButton.isEnabled = value
        minusOneButton.isEnabled = value
        resetInfusionsButton.isEnabled = value
    }


    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = context.packageName

        for (appProcess in activityManager.runningAppProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun setInfusionsCounter() {
        updateInfusionsText()

        plusOneButton.setOnClickListener {
            if (infusions >= record.getInfusions())
                return@setOnClickListener

            infusions++
            updateInfusionsText()
            setTimer()
            setTimerText()
        }

        minusOneButton.setOnClickListener {
            removeOneInfusion()
            setTimer()
            setTimerText()
        }

        resetInfusionsButton.setOnClickListener {
            infusions = record.getInfusions()
            updateInfusionsText()
            setTimer()
            setTimerText()
        }
    }

    private fun removeOneInfusion() {
        if (infusions <= 0)
            return

        infusions--
        updateInfusionsText()
    }

    private fun updateInfusionsText() {
        textViewInfusionsCounter.text = infusions.toString()
    }

    private fun setRatioCalculator() {

        fun resetValues() {
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
            gramsTextChangedByUser = true
            millisTextChangedByUser = true
            resetValues()
            gramsTextChangedByUser = false
            millisTextChangedByUser = false
        }
    }
}