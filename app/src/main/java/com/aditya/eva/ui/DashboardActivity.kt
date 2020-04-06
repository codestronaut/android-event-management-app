package com.aditya.eva.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditya.eva.R
import com.aditya.eva.model.Event
import com.aditya.eva.ui.adapter.EventsAdapter
import com.aditya.eva.ui.auth.ProfileActivity
import com.aditya.eva.ui.auth.SignInActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class DashboardActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val eventsRef = db.collection("events")
    private var adapter: EventsAdapter? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        setSupportActionBar(dashboard_app_bar)

        dashboard_fab.setOnClickListener {
            navigateToCreateActivity()
        }

        initGreetingMessage()
        setupRecyclerView()
    }

    private fun initGreetingMessage() {
        if (auth.currentUser != null) {
            greeting_text.text = getString(R.string.greeting, auth.currentUser?.displayName)
        } else {
            greeting_text.text = getString(R.string.anonym_greeting)
        }
    }

    private fun setupRecyclerView() {
        val query: Query = eventsRef.orderBy("event_date", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .build()

        adapter =
            EventsAdapter(options, applicationContext)
        val linearLayoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        events_recycler_view.layoutManager = linearLayoutManager
        events_recycler_view.hasFixedSize()
        events_recycler_view.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            adapter?.startListening()
        } else {
            finish()
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    private fun navigateToCreateActivity() {
        val intent = Intent(this, CreateNewActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_menu -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.sign_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }
}
