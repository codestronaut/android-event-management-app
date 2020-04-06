package com.aditya.eva.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.aditya.eva.R
import com.aditya.eva.model.Event
import com.aditya.eva.ui.detail.DetailActivity
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.event_card_item.view.*

class EventsAdapter(
    options: FirestoreRecyclerOptions<Event>,
    private var context: Context
) :
    FirestoreRecyclerAdapter<Event, EventsAdapter.EventHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.event_card_item, parent, false)
        return EventHolder(view)
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int, model: Event) {
        holder.bindItem(model)
        holder.itemView.setOnClickListener {
            Toast.makeText(context, model.event_name, Toast.LENGTH_SHORT).show()
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_QUERY_TITLE, model.event_name)
            context.startActivity(intent)
        }
    }

    class EventHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(event: Event) {
            itemView.apply {
                event_name.text = event.event_name
                event_date.text = event.event_date
                event_place.text = event.event_place
                Glide.with(context).load(event.image_url).into(event_image)
            }
        }
    }
}