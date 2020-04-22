package com.aditya.eva.ui.content

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.aditya.eva.R
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail.*

/*
* Detail Activity
* => Used to see event detail and buy tickets
* => Tools: Firebase Fire Store DB
* => SUCCESS: 19-04-2020 16:26 PM
* */
class DetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var bundle: Bundle? = null
    private lateinit var eventName: String
    private lateinit var eventPrice: String

    companion object {
        const val EXTRA_QUERY_TITLE = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        bundle = intent.extras
        initAuth()
        initAppBar()
        initLoadingIndicator()
        initFireStore()
        getEventData()

        btn_buy.setOnClickListener(this)
    }

    private fun initAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initFireStore() {
        db = FirebaseFirestore.getInstance()
    }

    private fun initLoadingIndicator() {
        val threeBounce = ThreeBounce()
        detail_loading.indeterminateDrawable = threeBounce
    }

    private fun initAppBar() {
        setSupportActionBar(detail_toolbar)
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.title = bundle?.getString(EXTRA_QUERY_TITLE)
        appBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        detail_collapse_toolbar.setExpandedTitleColor(
            ContextCompat.getColor(
                this,
                android.R.color.transparent
            )
        )
    }

    private fun getEventData() {
        content_container.visibility = View.GONE
        detail_loading.visibility = View.VISIBLE

        val dbRef = db.collection("events")
        val query = dbRef.whereEqualTo("event_name", bundle?.getString(EXTRA_QUERY_TITLE))
        query.get().addOnSuccessListener { documents ->
            content_container.visibility = View.VISIBLE
            detail_loading.visibility = View.GONE
            for (document in documents) {
                eventName = document["event_name"].toString()
                eventPrice = document["event_price"].toString()
                Glide.with(this).load(document["image_url"]).into(detail_event_poster)
                detail_event_description.text = document["event_description"].toString()
                detail_event_organizer.text = document["event_organizer"].toString()
                detail_event_date.text = document["event_date"].toString()
                detail_event_place.text = document["event_place"].toString()
                detail_event_price.text = document["event_price"].toString()
                event_name_text.text = document["event_name"].toString()
                document["event_price"].let { price ->
                    if (price != null) {
                        if (price != "0") {
                            detail_free_paid.text = getString(R.string.price_paid, price)
                        } else {
                            detail_free_paid.text = getString(R.string.price_free)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_buy) {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra(CheckoutActivity.EXTRA_EVENT_NAME, eventName)
            intent.putExtra(CheckoutActivity.EXTRA_EVENT_PRICE, eventPrice)
            intent.putExtra(CheckoutActivity.EXTRA_BUYER_NAME, auth.currentUser?.displayName)
            startActivity(intent)
        }
    }
}
