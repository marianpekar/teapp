package com.marianpekar.teapp

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class MaxNumberTextWatcher(private val editText: EditText, private val maxNumber: Int) : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            val input = it.toString()

            if (input.isNotEmpty()) {
                val number = input.toInt()

                val formattedNumber = when {
                    number > maxNumber -> maxNumber.toString()
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
