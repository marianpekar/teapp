package com.marianpekar.teapp.activities

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.marianpekar.teapp.R
import java.util.Locale

open class AppCompatActivityLocale : AppCompatActivity() {
    fun setLocale(lang: String) {
        val preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
        preferences.putString("Language", lang)
        preferences.apply()
    }

    private fun Context.updateLocale(lang: String): Context {
        val locale = Locale(lang)
        val config = Configuration(this.resources.configuration)
        config.setLocale(locale)
        return this.createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences(newBase.getString(R.string.app_name), Context.MODE_PRIVATE)
            .getString("Language", Locale.getDefault().language)
        val langContext = newBase.updateLocale(lang ?: Locale.getDefault().language)
        super.attachBaseContext(langContext)
    }
}