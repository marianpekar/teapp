import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class SecondsTextWatcher(private val editText: EditText) : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            val input = it.toString()

            if (input.isNotEmpty()) {
                val number = input.toInt()

                val formattedNumber = when {
                    number < 10 -> "0$number"
                    number > 59 -> "59"
                    else -> String.format("%02d", number)
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
