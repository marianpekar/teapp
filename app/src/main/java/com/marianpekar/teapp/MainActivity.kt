package com.marianpekar.teapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter : RecordsAdapter
    private var records = ArrayList<Record>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recycler = findViewById(R.id.recyclerRecords)

        recycler.layoutManager = LinearLayoutManager(this@MainActivity)

        records.add(Record("Sencha Natsu", 2, 250, 80, 120, 2))
        records.add(Record("Sencha Miyazaki", 2, 250, 60, 300, 4))
        records.add(Record("Genmaicha", 3, 250, 70, 90, 2))

        adapter = RecordsAdapter(records, this@MainActivity)
        recycler.adapter = adapter
    }
}