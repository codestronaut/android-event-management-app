package com.aditya.eva.ui.content

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.aditya.eva.R
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_checkout.*
import java.io.ByteArrayOutputStream
import java.util.*

class CheckoutActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var mEventName: String
    private lateinit var mEventPrice: String
    private lateinit var mEventBuyer: String
    private lateinit var tickets: MutableMap<String, Any>

    companion object {
        const val EXTRA_EVENT_NAME = "EXTRA_EVENT_NAME"
        const val EXTRA_BUYER_NAME = "EXTRA_BUYER_NAME"
        const val EXTRA_EVENT_PRICE = "EXTRA_EVENT_PRICE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        initAppbar()
        initLoadingIndicator()
        initStorage()
        val intent = intent
        mEventName = intent.getStringExtra(EXTRA_EVENT_NAME)!!
        mEventBuyer = intent.getStringExtra(EXTRA_BUYER_NAME)!!
        mEventPrice = intent.getStringExtra(EXTRA_EVENT_PRICE)!!

        loadCheckoutSummary(
            intent.getStringExtra(EXTRA_EVENT_NAME)!!,
            intent.getStringExtra(EXTRA_BUYER_NAME)!!,
            intent.getStringExtra(EXTRA_EVENT_PRICE)!!
        )

        btn_buy_now.setOnClickListener(this)
    }

    private fun initAppbar() {
        setSupportActionBar(checkout_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initStorage() {
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        database = FirebaseFirestore.getInstance()
    }

    private fun initLoadingIndicator() {
        val threeBounce = ThreeBounce()
        checkout_loading.indeterminateDrawable = threeBounce
    }

    private fun loadCheckoutSummary(
        eName: String,
        eBuyer: String,
        ePrice: String,
        eTickets: Int = 1
    ) {
        checkout_event_name_text.text = eName
        checkout_customer_name_text.text = eBuyer
        checkout_event_price_text.text = ePrice
        checkout_tickets_text.text = eTickets.toString()
        checkout_summary_text.text = (eTickets * ePrice.toInt()).toString()
    }

    private fun uploadTicket(bitmap: Bitmap) {
        val ref = storageRef.child("ticket_qr_code/" + UUID.randomUUID() + ".jpg")
        val byteArray = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArray)
        val data = byteArray.toByteArray()
        val uploadTask = ref.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tickets = mutableMapOf(
                    "ticket_name" to mEventName,
                    "ticket_price" to mEventPrice,
                    "ticket_buyer" to mEventBuyer,
                    "ticket_qr_code" to task.result.toString()
                )

                database.collection("tickets")
                    .add(tickets)
                    .addOnSuccessListener {
                        checkout_loading.visibility = View.GONE
                        startActivity(Intent(this, SuccessActivity::class.java))
                        Toast.makeText(
                            this,
                            "Ticket created!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Upload ticket failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this,
                    "Upload failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateTicket() {
        checkout_loading.visibility = View.VISIBLE
        val text = mEventBuyer
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            uploadTicket(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_buy_now) {
            generateTicket()
        }
    }
}
