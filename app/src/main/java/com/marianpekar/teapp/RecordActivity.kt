package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1)
        {
            throw Exception("Record index is -1")
        }

        val record = RecordsStorage(this@RecordActivity).getRecord(recordIndex)

        val textRecordName : TextView = findViewById(R.id.textRecordName)
        val textRecordSummary : TextView = findViewById(R.id.textRecordSummary)
        val imageButtonLeftArrow : ImageButton = findViewById(R.id.imageButtonLeftArrow)

        textRecordName.text = record.getName()
        textRecordSummary.text = record.detailsFormatted()

        imageButtonLeftArrow.setOnClickListener {
            val intent = Intent(this@RecordActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}