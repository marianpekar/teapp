package com.marianpekar.teapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.marianpekar.teapp.R
import com.marianpekar.teapp.data.Record
import com.marianpekar.teapp.data.RecordsStorage

class RecordNotesActivity : AppCompatActivityLocale() {

    private lateinit var record: Record
    private var recordIndex: Int = -1
    private lateinit var recordStorage : RecordsStorage

    private lateinit var editTextNotes : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record_notes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recordStorage = RecordsStorage(this@RecordNotesActivity)

        setRecord()
        setHeader()
        setEditText()
        setOnBackPressedCallback()
        setBackButton()
        setCupButton()
    }

    override fun onStop() {
        super.onStop()
        recordStorage.setNotes(recordIndex, editTextNotes.text.toString())
    }

    private fun setRecord() {
        recordIndex = intent.getIntExtra("position", -1)
        if (recordIndex == -1) {
            throw Exception("Record index is -1")
        }

        record = recordStorage.getRecord(recordIndex)
    }

    private fun setHeader() {
        val textRecordName: TextView = findViewById(R.id.textRecordName)
        val textRecordSummary: TextView = findViewById(R.id.textRecordSummary)

        textRecordName.text = record.getName()
        textRecordSummary.text = record.summaryWithAdjustmentsFormatted(isTempInFahrenheit)
    }

    private fun setEditText() {
        editTextNotes = findViewById(R.id.editTextNotes)

        val notes = record.getNotes()

        // null check is for backward compatibility with records saved before notes were added
        if (notes.isNullOrEmpty())
            return

        editTextNotes.setText(notes)
    }

    private fun setOnBackPressedCallback() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@RecordNotesActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
        this@RecordNotesActivity.onBackPressedDispatcher.addCallback(this, callback);
    }

    private fun setBackButton() {
        val imageButtonLeftArrow: ImageButton = findViewById(R.id.imageButtonLeftArrow)
        imageButtonLeftArrow.setOnClickListener {
            val intent = Intent(this@RecordNotesActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setCupButton() {
        val imageButtonCup: ImageButton = findViewById(R.id.imageButtonToolbarCup)
        imageButtonCup.setOnClickListener {
            val intent = Intent(this@RecordNotesActivity, RecordActivity::class.java)
            intent.putExtra("position", recordIndex)
            startActivity(intent)
        }
    }
}