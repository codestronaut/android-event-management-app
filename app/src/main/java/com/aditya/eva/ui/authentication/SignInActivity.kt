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
import kotlinx.android.synthetic.main.activity_auth.*

/*
* SIGN IN ACTIVITY
* Finished: March 25th 2020
* */
class SignInActivity : AppCompatActivity(), View.OnClickListener {
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
        setContentView(R.layout.activity_auth)

        initFirebaseAuth()
        setUpLoadingIndicator()
        initAuthButton()
    }

    /*
    * Customize loading animation
    * */
    private fun setUpLoadingIndicator() {
        val doubleBounce = DoubleBounce()
        sign_in_loading.indeterminateDrawable = doubleBounce
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
        email = email_edit_text.text.toString().trim()
        password = password_edit_text.text.toString().trim()

        if (email.isEmpty()) {
            sign_in_input_email.error = "Email is required"
            email_edit_text.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            sign_in_input_email.error = "Email is invalid"
            email_edit_text.requestFocus()
            return
        }

        if (password.isEmpty()) {
            sign_in_input_password.error = "Password is required"
            password_edit_text.requestFocus()
            return
        }

        if (password.length < 6) {
            sign_in_input_password.error = "Password must be at least 6 character"
            password_edit_text.requestFocus()
            return
        }
    }

    /*
    * Handle onClick for Buttons
    * */
    private fun initAuthButton() {
        btn_sign_in.setOnClickListener(this)
        btn_new_account.setOnClickListener(this)
    }

    /*
    * Handle Sign In with Email and Password
    * */
    private fun signIn() {
        initDataInput()
        sign_in_loading.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                sign_in_loading.visibility = View.GONE
                if (task.isSuccessful) {
                    finish()
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    // Make this activity is main activity again
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    /*
    * OnClickListener
    * */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_sign_in -> signIn()
            R.id.btn_new_account -> {
                finish()
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }
    }

    /*
    * Identify that user already login or not
    * */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            finish()
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}
