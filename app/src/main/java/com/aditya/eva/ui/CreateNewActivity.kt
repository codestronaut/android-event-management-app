package com.aditya.eva.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.aditya.eva.R
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_create_new.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/*
* CreateNew Activity
* => Used to creating new events and publishing to public
* => Tools: Firebase Fire Store DB
* => SUCCESS: 18-03-2020 23:26 PM
* */
@Suppress("DEPRECATION")
class CreateNewActivity : AppCompatActivity() {
    // Firebase Component
    private val db = Firebase.firestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var uploadTask: UploadTask

    // Needed Variable
    private lateinit var eventName: String
    private lateinit var eventOrganizer: String
    private lateinit var eventDate: String
    private lateinit var eventPlace: String
    private lateinit var eventDescription: String
    private lateinit var eventPrice: String
    private var isEventPaid: Boolean = false
    private lateinit var events: MutableMap<String, Any>
    private lateinit var filePath: Uri

    companion object {
        const val PICK_IMAGE_REQUEST = 100
    }

    /*
    * OnCreate (MAIN)
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)

        // Setup firebase component
        storage = Firebase.storage
        storageRef = storage.reference

        // Setup progress bar indicator
        val doubleBounce = DoubleBounce()
        loading_progress.indeterminateDrawable = doubleBounce

        // Configure the custom App Bar
        configureAppBar()

        // Pick date from calendar
        btn_open_calendar.setOnClickListener {
            openDatePicker()
        }

        // Events type chooser (Paid or Free)
        price_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_paid -> isEventPaid = true
                R.id.rb_free -> isEventPaid = false
            }
        }

        // Handle upload and get url download event poster then save to db
        btn_upload_image.setOnClickListener {
            pickImage()
        }
    }

    /*
    * AppBar configuration function
    * */
    private fun configureAppBar() {
        setSupportActionBar(create_page_app_bar)
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.setDisplayShowTitleEnabled(false)
    }

    /*
    * Get event data from the user and wrap it into a collection
    * */
    private fun getDataEvent() {
        eventName = event_name_edit_text.text.toString()
        eventOrganizer = event_organizer_edit_text.text.toString()
        eventDate = date_edit_text.text.toString()
        eventPlace = event_place_edit_text.text.toString()
        eventDescription = event_desc_edit_text.text.toString()
        eventPrice = event_price_edit_text.text.toString()
        events = mutableMapOf(
            "event_name" to eventName,
            "event_organizer" to eventOrganizer,
            "event_date" to eventDate,
            "event_place" to eventPlace,
            "event_description" to eventDescription,
            "is_event_paid" to isEventPaid,
            "event_price" to eventPrice
        )
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ), PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            filePath = data.data!!
            try {
                // TASK: Find the new version of deprecated bellow
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                preview_image.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadData() {
        loading_progress.visibility = View.VISIBLE
        form_content_scroll_view.visibility = View.GONE

        val ref = storageRef.child("images/" + UUID.randomUUID())
        uploadTask = ref.putFile(filePath)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getDataEvent()
                val downloadUri = task.result
                events["image_url"] = downloadUri.toString()
                writeToDb()
            } else {
                Toast.makeText(this, "URL Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
    * Write to database function
    * */
    private fun writeToDb() {
        val contextView: View = findViewById(android.R.id.content)
        db.collection("events")
            .add(events)
            .addOnSuccessListener {
                finish()
                Snackbar.make(contextView, "Event has been published: $it", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                loading_progress.visibility = View.GONE
                form_content_scroll_view.visibility = View.VISIBLE
                Snackbar.make(contextView, "Event publishing fail: $it", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }

    /*
    * Open date picker function
    * */
    private fun openDatePicker() {
        val newCalendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, month, dayOfMonth)
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                date_edit_text.text = dateFormatter.format(newDate.time).toEditable()
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    /*
    * Function that used for converting static string into editable string that edit text accept
    * */
    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    /*
    * OPTION MENU
    * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Create and inflate menu
        menuInflater.inflate(R.menu.create_page_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Save button for publish the event
        if (item.itemId == R.id.save) {
            if (event_name_edit_text.text.toString().trim() == ""
                && event_organizer_edit_text.text.toString().trim() == ""
                && date_edit_text.text.toString().trim() == ""
                && event_place_edit_text.text.toString().trim() == ""
                && event_desc_edit_text.text.toString().trim() == ""
                && event_price_edit_text.text.toString().trim() == ""
            ) {
                input_event_name.error = "This field can't empty"
                input_organizer.error = "This field can't empty"
                input_date.error = "This field can't empty"
                input_place.error = "This field can't empty"
                input_description.error = "This field can't empty"
                input_price.error = "This field can't empty"
            } else {
                uploadData()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
