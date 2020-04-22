package com.aditya.eva.ui.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditya.eva.R
import com.aditya.eva.model.Event
import com.aditya.eva.ui.adapter.EventsAdapter
import com.aditya.eva.ui.authentication.SignInActivity
import com.aditya.eva.ui.profile.ProfileActivity
import com.aditya.eva.ui.user.MyEventActivity
import com.aditya.eva.ui.user.MyTicketActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

/*
* Dashboard Activity
* => Used to showcase events and displaying to public
* => Tools: Firebase Fire Store DB
* => SUCCESS: 19-04-2020 16:43 PM
* */
class DashboardActivity : AppCompatActivity() {
    // Variable Needed
    private val db = Firebase.firestore
    private val eventsRef = db.collection("events")
    private var adapter: EventsAdapter? = null
    private lateinit var auth: FirebaseAuth

    /*
    * onCreate (MAIN)
    * */
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

    /*
    * Generate greeting message for user
    * */
    private fun initGreetingMessage() {
        if (auth.currentUser != null) {
            greeting_text.text = getString(R.string.greeting, auth.currentUser?.displayName)
        } else {
            greeting_text.text = getString(R.string.anonym_greeting)
        }
    }

    /*
    * Prepare recycler view integrated with firebase
    * */
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

    /*
    * onStart(): Execute when activity is open
    * Start listening data from database
    * */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            adapter?.startListening()
        } else {
            finish()
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    /*
    * onStop(): Execute when activity is close by click back button
    * Stop listening data from database
    * */
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    /*
    * Open Create Activity
    * */
    private fun navigateToCreateActivity() {
        val intent = Intent(this, CreateNewActivity::class.java)
        startActivity(intent)
    }

    /*
    * Inflate menu to Dashboard Activity toolbar
    * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    /*
    * Handle menu onClick
    * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_menu -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.sign_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
            R.id.my_ticket_menu -> {
                startActivity(Intent(this, MyTicketActivity::class.java))
            }
            R.id.my_event_menu -> {
                startActivity(Intent(this, MyEventActivity::class.java))
            }
        }

        return true
    }
}
