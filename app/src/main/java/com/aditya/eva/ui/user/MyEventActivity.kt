package com.aditya.eva.ui.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aditya.eva.R
import com.aditya.eva.model.Event
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_event.*

class MyEventActivity : AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<Event, MyEventHolder>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_event)
        initAppBar()
        initFirebase()
        getMyEventData()
    }

    private fun initAppBar() {
        setSupportActionBar(my_event_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initFirebase() {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun getMyEventData() {
        val query = database.collection("events")
            .whereEqualTo("event_user", auth.currentUser?.uid)

        val response = FirestoreRecyclerOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Event, MyEventHolder>(response) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.event_card_item, parent, false)
                return MyEventHolder(view)
            }

            override fun onBindViewHolder(holder: MyEventHolder, position: Int, model: Event) {
                holder.myEventName.text = model.event_name
                holder.myEventPlace.text = model.event_place
                holder.myEventDate.text = model.event_date
                Glide.with(applicationContext)
                    .load(model.image_url).into(holder.myEventImage)
            }
        }

        adapter.notifyDataSetChanged()
        my_event_recycler_view.layoutManager = GridLayoutManager(this, 2)
        my_event_recycler_view.adapter = adapter
    }

    inner class MyEventHolder(view: View) : RecyclerView.ViewHolder(view) {
        val myEventName: TextView = view.findViewById(R.id.event_name)
        val myEventPlace: TextView = view.findViewById(R.id.event_place)
        val myEventDate: TextView = view.findViewById(R.id.event_date)
        val myEventImage: ImageView = view.findViewById(R.id.event_image)
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
