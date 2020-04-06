package com.aditya.eva.ui.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.aditya.eva.R
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var bundle: Bundle? = null

    companion object {
        const val EXTRA_QUERY_TITLE = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoiYWRpdHlhMTUwIiwiYSI6ImNrNDkwaXo1NjAwMXczbmtiN2Vsc2s3anAifQ.92NMAqpBzBxOirmdhtdayA"
        )
        setContentView(R.layout.activity_detail)
        bundle = intent.extras

        configureAppBar()
        initLoadingIndicator()
        initDb()
        getEventData()

        detail_event_location.onCreate(savedInstanceState)
        detail_event_location.getMapAsync { mapBoxMap ->
            mapBoxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                // Implement Later (Load Data to Map)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        detail_event_location.onStart()
    }

    override fun onResume() {
        super.onResume()
        detail_event_location.onResume()
    }

    override fun onPause() {
        super.onPause()
        detail_event_location.onPause()
    }

    override fun onStop() {
        super.onStop()
        detail_event_location.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        detail_event_location.onDestroy()
    }

    private fun initDb() {
        db = FirebaseFirestore.getInstance()
    }

    private fun initLoadingIndicator() {
        val threeBounce = ThreeBounce()
        detail_poster_progress.indeterminateDrawable = threeBounce
    }

    private fun configureAppBar() {
        setSupportActionBar(detail_toolbar)
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.title = bundle?.getString(EXTRA_QUERY_TITLE)
        appBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        detail_collapse_toolbar.setExpandedTitleColor(resources.getColor(android.R.color.transparent))
    }

    private fun getEventData() {
        val dbRef = db.collection("events")
        val query = dbRef.whereEqualTo("event_name", bundle?.getString(EXTRA_QUERY_TITLE))
        detail_poster_progress.visibility = View.VISIBLE
        query.get().addOnSuccessListener { documents ->
            detail_poster_progress.visibility = View.GONE
            for (document in documents) {
                Glide.with(this).load(document["image_url"]).into(detail_event_poster)
                detail_event_description.text = document["event_description"].toString()
                detail_event_organizer.text = document["event_organizer"].toString()
                detail_event_date.text = document["event_date"].toString()
                detail_event_place.text = document["event_place"].toString()
                detail_event_price.text = document["event_price"].toString()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detail_event_location.onSaveInstanceState(outState)
    }
}
