package com.aditya.eva.ui.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aditya.eva.R
import com.aditya.eva.model.Ticket
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_ticket.*

class MyTicketActivity : AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<Ticket, TicketHolder>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ticket)
        initFirebase()
        initAppBar()
        getTicketData()
    }

    private fun initAppBar() {
        setSupportActionBar(my_ticket_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initFirebase() {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun getTicketData() {
        val query = database.collection("tickets")
            .whereEqualTo("ticket_buyer", auth.currentUser?.displayName)

        val response = FirestoreRecyclerOptions.Builder<Ticket>()
            .setQuery(query, Ticket::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Ticket, TicketHolder>(response) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ticket_card_item, parent, false)
                return TicketHolder(view)
            }

            override fun onBindViewHolder(holder: TicketHolder, position: Int, model: Ticket) {
                holder.ticketEventName.text = model.ticket_name
                holder.ticketEventBuyer.text = model.ticket_buyer
                holder.ticketEventPrice.text =
                    getString(R.string.ticket_price_text, model.ticket_price)
                Glide.with(applicationContext)
                    .load(model.ticket_qr_code).into(holder.ticketQrCodeImage)
            }
        }

        adapter.notifyDataSetChanged()
        my_ticket_recycler_view.layoutManager = LinearLayoutManager(this)
        my_ticket_recycler_view.adapter = adapter
    }

    inner class TicketHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ticketEventName: TextView = view.findViewById(R.id.item_ticket_event_name)
        val ticketEventBuyer: TextView = view.findViewById(R.id.item_ticket_event_buyer)
        val ticketEventPrice: TextView = view.findViewById(R.id.item_ticket_event_price)
        val ticketQrCodeImage: ImageView = view.findViewById(R.id.item_qr_code_image)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
