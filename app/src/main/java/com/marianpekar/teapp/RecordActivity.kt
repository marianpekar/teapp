package com.marianpekar.teapp

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener


class RecordActivity : AppCompatActivity(), CustomCountdownTimer.OnChangeHandler {

    private lateinit var record: Record

    private lateinit var timer: CustomCountdownTimer
    private lateinit var textViewStopWatch: TextView
    private lateinit var buttonStartStop: Button
    private lateinit var plusOneButton: Button
    private lateinit var minusOneButton: Button
    private lateinit var resetInfusionsButton: Button
    private lateinit var resetTimerButton: Button

    private lateinit var textViewInfusionsCounter: TextView
    private var infusions: Int = 0

    private lateinit var mediaPlayer: MediaPlayer
    private val notificationChannelId: String = "teapp_notification_channel_id"

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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mediaPlayer = MediaPlayer.create(this@RecordActivity, R.raw.flute_shot)

        preferences = applicationContext.getSharedPreferences(getString(R.string.app_name), 0)

        textViewStopWatch = findViewById(R.id.textViewStopWatch)
        buttonStartStop = findViewById(R.id.buttonStopWatchStartStop)
        textViewInfusionsCounter = findViewById(R.id.textViewCounter)
        plusOneButton = findViewById(R.id.buttonCounterPlusOne)
        minusOneButton = findViewById(R.id.buttonCounterMinusOne)
        resetInfusionsButton = findViewById(R.id.buttonCounterReset)
        resetTimerButton = findViewById(R.id.buttonStopWatchReset)

        setRecord()
        setInfusions()

        setBackButton()
        setOnBackPressedCallback()

        setHeader()

        setTimer()
        setTimerText()
        setTimerButtons()

        setInfusionCounter()
        setRatioCalculator()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()

        val editor = preferences.edit()
        editor.putInt(infusionsPrefKey, textViewInfusionsCounter.text.toString().toInt())
        editor.apply()
    }

    private fun setRecord() {
        val recordIndex = intent.getIntExtra("position", -1)
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
        textRecordSummary.text = record.detailsFormatted()
    }

    private fun setOnBackPressedCallback() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToMainActivity()
            }
        }
        this@RecordActivity.onBackPressedDispatcher.addCallback(this, callback);
    }

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            backToMainActivity()
        }
    }

    private fun backToMainActivity() {
        val intent = Intent(this@RecordActivity, MainActivity::class.java)
        startActivity(intent)
        timer.pause()
        finish()
    }

    private fun setTimerText() {
        if (infusions > 0) {
            var seconds = record.getTime()
            val adjustments = record.getAdjustments()
            if (adjustments.isNotEmpty() && infusions < record.getInfusions() && infusions > 0) {

                val adjustment = adjustments[record.getInfusions() - infusions - 1]
                val adjustmentSeconds = if (adjustment.getIsNegative()) adjustment.seconds * -1 else adjustment.seconds

                seconds += adjustmentSeconds
            }

            textViewStopWatch.text = formatTime(seconds)
        } else {
            textViewStopWatch.text = getString(R.string.default_stopwatch_value)
        }
    }

    private fun setTimer() {
        val adjustments = record.getAdjustments()
        var seconds = record.getTime()
        if (adjustments.isNotEmpty() && infusions < record.getInfusions() && infusions > 0) {

            val adjustment = adjustments[record.getInfusions() - infusions - 1]
            val adjustmentSeconds = if (adjustment.getIsNegative()) adjustment.seconds * -1 else adjustment.seconds

            seconds += adjustmentSeconds
        }

        timer = CustomCountdownTimer(seconds * 1000, 1000, this)
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

                minusOneButton.isEnabled = false
                plusOneButton.isEnabled = false
                resetInfusionsButton.isEnabled = false
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

            minusOneButton.isEnabled = true
            plusOneButton.isEnabled = true
            resetInfusionsButton.isEnabled = true
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

        plusOneButton.isEnabled = true
        minusOneButton.isEnabled = true
        resetInfusionsButton.isEnabled = true

        removeOneInfusion()

        if (infusions > 0) {
            setTimer()
            setTimerText()
        }

        if (!isAppInForeground(this@RecordActivity)) {
            showPushNotification()
        } else {
            Toast.makeText(this@RecordActivity, R.string.your_tea_is_ready, Toast.LENGTH_LONG)
                .show()
            mediaPlayer.start()
        }
    }

    private fun showPushNotification() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this@RecordActivity, notificationChannelId)
            .setSmallIcon(R.drawable.teacup)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.your_tea_is_ready))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(this@RecordActivity)) {
            if (ActivityCompat.checkSelfPermission(
                    this@RecordActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            notify(R.integer.notification_id, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    description = getString(R.string.notification_channel_description)
                    enableLights(true)
                    lightColor = R.color.green_tea_darker

                    // Enable custom notification sound
                    setSound(
                        Uri.parse("android.resource://${packageName}/${R.raw.flute_shot}"),
                        null
                    )
                }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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

    private fun setInfusionCounter() {
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
        val editTextGrams: EditText = findViewById(R.id.editTextGrams)
        val editTextMillis: EditText = findViewById(R.id.editTextMillis)

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