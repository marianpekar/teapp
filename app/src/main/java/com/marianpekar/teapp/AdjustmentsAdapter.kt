package com.marianpekar.teapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView

class AdjustmentsAdapter(
    private var adjustments: List<Adjustment>,
    private var context: Context
) : RecyclerView.Adapter<AdjustmentsAdapter.AdjustmentsViewHolder>() {

    class AdjustmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editTextMinutes: EditText = itemView.findViewById(R.id.editTextMinutes)
        val editTextSeconds: EditText = itemView.findViewById(R.id.editTextSeconds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjustmentsViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_adjustment, parent, false)
        return AdjustmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdjustmentsViewHolder, position: Int) {
        val totalSeconds = adjustments[position].seconds
        val minutes =  totalSeconds / 60
        val remainingSeconds = totalSeconds % 60

        holder.editTextMinutes.setText(minutes.toString())
        holder.editTextSeconds.setText(String.format("%02d", remainingSeconds))

        val secondsTextWatcher = SecondsTextWatcher(holder.editTextSeconds)
        holder.editTextSeconds.addTextChangedListener(secondsTextWatcher)

        holder.editTextMinutes.addTextChangedListener {
            setAdjustment(holder, position)
        }

        holder.editTextSeconds.addTextChangedListener {
            setAdjustment(holder, position)
        }
    }

    private fun setAdjustment(holder: AdjustmentsViewHolder, position: Int) {
        val minutesText = holder.editTextMinutes.text.toString()
        val secondsText = holder.editTextSeconds.text.toString()

        val minutes = if (minutesText.isNotEmpty()) minutesText.toLong() else 0
        val seconds = if (secondsText.isNotEmpty()) secondsText.toLong() else 0

        val totalSeconds = if (minutes >= 1) {
            (minutes * 60) + seconds
        } else {
            seconds
        }

        adjustments[position].seconds = totalSeconds
    }

    fun getAdjustments(): List<Adjustment> {
        return adjustments
    }

    override fun getItemCount(): Int {
        return adjustments.size
    }
}