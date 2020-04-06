package com.aditya.eva.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.aditya.eva.R
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.IOException
import java.util.*

/*
* PROFILE ACTIVITY
* Finished: March 25th 2020
* Required Action: **Refactor to remove deprecate method**
* */
@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    // Uri for file path
    private lateinit var uriProfile: Uri

    // Firebase auth instance
    private lateinit var auth: FirebaseAuth

    // Needed variables for whole class
    companion object {
        const val CHOOSE_PROFILE = 300
        var mPhotoUrl = ""
    }

    /*
    * onCreated Method: MAIN
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        configureAppBar()
        configureLoading()
        initFirebaseAuth()
        initProfileClickedItem()
        loadUserInformation()
    }

    /*
    * Executed when the app is show on the screen
    * Handle load user data
    * */
    override fun onStart() {
        super.onStart()
        loadUserInformation()
    }

    /*
    * Initialize FirebaseAuth
    * */
    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    /*
    * Customize loading animation
    * */
    private fun configureLoading() {
        val threeBounce = ThreeBounce()
        profile_upload_progress.indeterminateDrawable = threeBounce
    }

    /*
    * Configure custom app bar
    * */
    private fun configureAppBar() {
        setSupportActionBar(profile_page_app_bar)
        val appBar = supportActionBar
        appBar?.setDisplayShowTitleEnabled(false)
        appBar?.setDisplayHomeAsUpEnabled(true)
    }

    /*
    * Handle load user info
    * */
    private fun loadUserInformation() {
        val user = auth.currentUser
        if (user != null) {
            if (user.photoUrl != null) {
                Glide.with(this).load(user.photoUrl.toString()).into(user_profile_image)
            }
            if (user.displayName != null) {
                display_name_edit_text.text = user.displayName.toString().toEditable()
            }
        }
    }

    /*
    * Handle onClick for View in this activity
    * */
    private fun initProfileClickedItem() {
        user_profile_image.setOnClickListener(this)
        btn_save_profile.setOnClickListener(this)
    }

    /*
    * OnClick Listener
    * */
    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_save_profile) {
            saveUserInformation()
        } else if (v?.id == R.id.user_profile_image) {
            showImageChooser()
        }
    }

    /*
    * Handle image picker from file explorer or gallery
    * */
    private fun showImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Profile Image"
            ),
            CHOOSE_PROFILE
        )
    }

    /*
    * Handle result: Picked image
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_PROFILE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {
            uriProfile = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uriProfile)
                user_profile_image.setImageBitmap(bitmap)
                uploadProfileImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /*
    * Upload user profile image to Firebase Storage
    * */
    private fun uploadProfileImage() {
        val profileRef =
            FirebaseStorage.getInstance().getReference("profile_pics/" + UUID.randomUUID() + ".jpg")

        profile_upload_progress.visibility = View.VISIBLE
        profileRef.putFile(uriProfile).addOnSuccessListener {
            profile_upload_progress.visibility = View.GONE
            profileRef.downloadUrl.addOnSuccessListener {
                mPhotoUrl += it.toString()
            }
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    /*
    * Handle save user info
    * */
    private fun saveUserInformation() {
        val displayName = display_name_edit_text.text.toString()
        if (displayName.isEmpty()) {
            input_display_name.error = "Name is required"
            display_name_edit_text.requestFocus()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(Uri.parse(mPhotoUrl))
                .build()
            user.updateProfile(profile).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*
    * Function that used for converting static string into editable string that edit text accept
    * */
    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}
