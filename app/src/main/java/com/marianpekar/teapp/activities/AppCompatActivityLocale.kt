package com.marianpekar.teapp.activities

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.marianpekar.teapp.R
import java.util.Locale

open class AppCompatActivityLocale : AppCompatActivity() {
    private var _isTempInFahrenheit = false
    protected val isTempInFahrenheit: Boolean
        get() = _isTempInFahrenheit

    private var _areUnitsImperial = false
    protected val areUnitsImperial: Boolean
        get() = _areUnitsImperial

    fun setLocale(lang: String) {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
        prefs.putString("Language", lang)
        prefs.apply()
    }

    fun setIsTempInFahrenheit(value: Boolean) {
        _isTempInFahrenheit = value
        putBoolean("isTempInFahrenheit", isTempInFahrenheit)
    }

    fun setAreUnitsImperial(value: Boolean) {
        _areUnitsImperial = value
        putBoolean("areUnitsImperial", areUnitsImperial)
    }

    private fun putBoolean(key: String, value: Boolean) {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit()
        prefs.putBoolean(key, value)
        prefs.apply()
    }

    private fun Context.updateLocale(lang: String): Context {
        val locale = Locale(lang)
        val config = Configuration(this.resources.configuration)
        config.setLocale(locale)
        return this.createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(newBase.getString(R.string.app_name), Context.MODE_PRIVATE)

        val lang = prefs.getString("Language", Locale.getDefault().language)
        val langContext = newBase.updateLocale(lang ?: Locale.getDefault().language)
        super.attachBaseContext(langContext)

        _isTempInFahrenheit = prefs.getBoolean("isTempInFahrenheit", false)
        _areUnitsImperial = prefs.getBoolean("areUnitsImperial", false)
    }
}