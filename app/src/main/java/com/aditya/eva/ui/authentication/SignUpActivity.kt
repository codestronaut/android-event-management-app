package com.aditya.eva.ui.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.aditya.eva.R
import com.aditya.eva.ui.content.DashboardActivity
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.android.synthetic.main.activity_sign_up.*

/*
* SIGN UP ACTIVITY
* Finished: March 25th 2020
* */
class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    // Firebase auth instance
    private lateinit var auth: FirebaseAuth

    // Variable for store user input
    private lateinit var email: String
    private lateinit var password: String

    /*
    * onCreate Method: MAIN
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initFirebaseAuth()
        setUpLoadingIndicator()
        initAuthButton()
    }

    /*
    * Customize loading animation
    * */
    private fun setUpLoadingIndicator() {
        val doubleBounce = DoubleBounce()
        sign_up_loading.indeterminateDrawable = doubleBounce
    }

    /*
    * Initialize FirebaseAuth
    * */
    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    /*
    * Read data and validating
    * */
    private fun initDataInput() {
        email = new_email_edit_text.text.toString().trim()
        password = new_password_edit_text.text.toString().trim()

        if (email.isEmpty()) {
            sign_up_input_email.error = "Email is required"
            new_email_edit_text.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            sign_up_input_email.error = "Email is invalid"
            new_email_edit_text.requestFocus()
            return
        }

        if (password.isEmpty()) {
            sign_up_input_password.error = "Password is required"
            new_password_edit_text.requestFocus()
            return
        }

        if (password.length < 6) {
            sign_up_input_password.error = "Password must be at least 6 character"
            new_password_edit_text.requestFocus()
            return
        }
    }

    /*
    * Handle onClick for Buttons
    * */
    private fun initAuthButton() {
        btn_sign_up.setOnClickListener(this)
        btn_have_account.setOnClickListener(this)
    }

    /*
    * Handle Sign Up with Email and Password
    * */
    private fun signUp() {
        initDataInput()
        sign_up_loading.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                sign_up_loading.visibility = View.GONE
                if (task.isSuccessful) {
                    finish()
                    // Show success status to the user
                    Toast.makeText(
                        applicationContext,
                        "User registered successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    // Make this activity is main activity again
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    // Verify user already registered or not
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            applicationContext,
                            "Email already registered",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Other
                        Toast.makeText(
                            applicationContext,
                            task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    /*
    * OnClickListener
    * */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_sign_up -> signUp()
            R.id.btn_have_account -> {
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }
}
