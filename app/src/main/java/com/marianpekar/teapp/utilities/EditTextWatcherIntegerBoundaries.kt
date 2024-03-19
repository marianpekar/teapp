package com.marianpekar.teapp.utilities

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class EditTextWatcherIntegerBoundaries(private val editText: EditText, private val max: Int, private val min: Int = 0) : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            val input = it.toString()

            if (input.isNotEmpty()) {
                val number = input.toInt()

                val formattedNumber = when {
                    number > max -> max.toString()
                    number < min -> min.toString()
                    else -> number.toString()
                }

                if (input != formattedNumber) {
                    editText.removeTextChangedListener(this)
                    editText.setText(formattedNumber)
                    editText.setSelection(formattedNumber.length)
                    editText.addTextChangedListener(this)
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
