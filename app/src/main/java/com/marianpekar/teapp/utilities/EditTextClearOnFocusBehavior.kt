package com.marianpekar.teapp.utilities

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.setupClearOnFocusBehavior() {
    var previousValue: CharSequence? = null
    val initialHint: CharSequence? = if (this.text.isNullOrBlank()) this.hint else this.text

    this.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            previousValue = this.text
            this.hint = if (this.text.isNullOrBlank()) initialHint else previousValue

            this.text = null

            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        } else {
            if (this.text.isNullOrEmpty()) {
                this.setText(previousValue)
            }
        }
    }

    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {}
    })
}