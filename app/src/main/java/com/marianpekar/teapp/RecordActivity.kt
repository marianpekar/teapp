package com.marianpekar.teapp

import android.Manifest
import android.app.ActivityManager
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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class RecordActivity : AppCompatActivity(), CustomCountdownTimer.OnChangeHandler {

    private lateinit var record: Record

    private lateinit var timer: CustomCountdownTimer
    private lateinit var textViewStopWatch: TextView
    private lateinit var buttonStartStop: Button

    private lateinit var textViewInfusionsCounter: TextView
    private var infusions: Int = 0

    private lateinit var mediaPlayer: MediaPlayer
    private val notificationChannelId: String = "teapp_notification_channel_id"

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaPlayer = MediaPlayer.create(this@RecordActivity, R.raw.flute_shot)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "RecordActivity::WakelockTag"
        )
        wakeLock.acquire(30*60*1000L /*30 minutes*/)

        setRecord()
        setHeader()
        setStopWatch()
        setInfusionCounter()
        setRatioCalculator()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()

        if(wakeLock.isHeld)
        {
            wakeLock.release()
        }
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

                if (infusions <= 0) {
                    Toast.makeText(this@RecordActivity, R.string.no_infusions_left, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

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
        buttonStartStop.text = getString(R.string.start)
        timer.reset()
        removeOneInfusion()

        if (!isAppInForeground(this@RecordActivity)) {
            showPushNotification()
        } else {
            Toast.makeText(this@RecordActivity, R.string.your_tea_is_ready, Toast.LENGTH_LONG).show()
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
                setSound(Uri.parse("android.resource://${packageName}/${R.raw.flute_shot}"), null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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