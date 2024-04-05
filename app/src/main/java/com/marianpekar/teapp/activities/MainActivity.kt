package com.marianpekar.teapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.marianpekar.teapp.R
import com.marianpekar.teapp.adapters.RecordsAdapter
import com.marianpekar.teapp.data.RecordsStorage

class MainActivity : AppCompatActivityLocale() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecordsAdapter
    private lateinit var records: RecordsStorage

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val navigationClicksListener = NavigationClicksListener()

    private lateinit var importLauncher: ActivityResultLauncher<String>
    private lateinit var exportLauncher: ActivityResultLauncher<String>

    inner class NavigationClicksListener : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            return onNavigationItemSelectedDelegate(item)
        }

        private fun onNavigationItemSelectedDelegate(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.language -> {
                    val languages = arrayOf(
                        getString(R.string.english),
                        getString(R.string.czech),
                        getString(R.string.german)
                    )
                    val languageCodes = arrayOf("en", "cs", "de")
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle(getString(R.string.choose_language))
                    builder.setSingleChoiceItems(languages, -1) { dialog, which ->
                        val chosenLanguage = languageCodes[which]
                        setLocale(chosenLanguage)
                        dialog.dismiss()
                        recreate()
                    }
                    builder.setNegativeButton(getString(R.string.back), null)
                    builder.show()
                }
                R.id.exportRecords -> {
                    exportLauncher.launch("teapp.json")
                }
                R.id.importRecords -> {
                    importLauncher.launch("application/json")
                }

            }
            return false
        }
    }

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

        setImportAndExportLaunchers()
        setRecycler()
        setDrawer()
        setAddNewRecordButton()
    }

    private fun setImportAndExportLaunchers() {
        importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                val jsonRecords = contentResolver.openInputStream(it)?.bufferedReader().use { it?.readText() }
                jsonRecords?.let {
                    records.importRecords(it)
                    recreate()
                }
            }
        }

        exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            uri?.let {
                val jsonRecords = records.exportRecords()
                jsonRecords.let {
                    contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer -> writer?.write(jsonRecords) }
                }
            }
        }
    }

    private fun setRecycler() {
        recycler = findViewById(R.id.recyclerRecords)
        recycler.layoutManager = LinearLayoutManager(this@MainActivity)

        adapter = RecordsAdapter(records.getAllRecords(), this@MainActivity)

        recycler.adapter = adapter
    }

    private fun setDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener(navigationClicksListener)
    }

    private fun setAddNewRecordButton() {
        val addRecordButton: ImageButton = findViewById(R.id.buttonAddRecord)

        addRecordButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddRecordActivity::class.java)
            startActivity(intent)
        }
    }
}