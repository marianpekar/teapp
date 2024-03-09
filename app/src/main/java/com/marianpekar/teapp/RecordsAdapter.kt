package com.marianpekar.teapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(
    private var records: List<Record>,
    var context: Context) : RecyclerView.Adapter<RecordsAdapter.RecordsViewHolder>() {

    class RecordsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var textRecordName : TextView = itemView.findViewById(R.id.textRecordName)
        var textRecordSummary : TextView = itemView.findViewById(R.id.textRecordSummary)
        var imageButtonCup : ImageButton = itemView.findViewById(R.id.imageButtonCup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.card_record, parent, false)
        return RecordsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        val record = records[position]
        holder.textRecordName.text = record.getName()
        holder.textRecordSummary.text = record.detailsFormatted()
        holder.imageButtonCup.setOnClickListener {
            val intent = Intent(context, RecordActivity::class.java)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return records.size
    }

}