package com.marianpekar.teapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter : RecordsAdapter
    private lateinit var records : RecordsStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        records = RecordsStorage(this@MainActivity)

        setRecycler()
        setAddNewRecordButton()
    }

    private fun setRecycler()
    {
        recycler = findViewById(R.id.recyclerRecords)
        recycler.layoutManager = LinearLayoutManager(this@MainActivity)

        adapter = RecordsAdapter(records.getAllRecords(), this@MainActivity)

        recycler.adapter = adapter
    }

    private fun setAddNewRecordButton() {
        val addRecordButton: FloatingActionButton = findViewById(R.id.buttonAddRecord)

        addRecordButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddRecordActivity::class.java)
            startActivity(intent)
        }
    }
}