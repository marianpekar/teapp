package com.marianpekar.teapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.marianpekar.teapp.R
import com.marianpekar.teapp.adapters.RecordsAdapter
import com.marianpekar.teapp.data.RecordsStorage

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecordsAdapter
    private lateinit var records: RecordsStorage

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    inner class NavigationClicksListener : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            return onNavigationItemSelectedDelegate(item)
        }

        private fun onNavigationItemSelectedDelegate(item: MenuItem): Boolean {
            when (item.itemId){
                android.R.id.home -> toggleDrawer()
                R.id.settings -> TODO()
            }
            return false
        }
    }

    private val navigationClicksListener = NavigationClicksListener()

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

        setNavigation()

        records = RecordsStorage(this@MainActivity)

        setRecycler()
        setAddNewRecordButton()
    }

    private fun setNavigation() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener(navigationClicksListener)
    }

    private fun toggleDrawer(): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    private fun setRecycler() {
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