package com.x3noku.mahaon.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.x3noku.mahaon.R
import com.x3noku.mahaon.models.User

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val store = Firebase.firestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var continueProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)

//        init auth
        auth = Firebase.auth

//        init views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        continueButton = findViewById(R.id.continueButton)
        continueProgressBar = findViewById(R.id.continueProgressBar)

//        handle inputs' text changes
        emailEditText.doOnTextChanged { _, _, _, _ -> handleInputState() }
        passwordEditText.doOnTextChanged { _, _, _, _ -> handleInputState() }

//        handle button clicks
        continueButton.setOnClickListener { sendRequest() }

//        init button state with single function call
        handleInputState()
    }

    private fun handleInputState() {
//        handle button state to avoid pressing without filled inputs
        if(emailEditText.text.isNotBlank() && passwordEditText.text.isNotBlank()) {
            continueButton.isClickable = true
            continueButton.alpha = 1f
        }
        else {
            continueButton.isClickable = false
            continueButton.alpha = 0.3f
        }
    }

    private fun sendRequest() {
//        change button state to Loading
        continueButton.isClickable = false
        continueButton.text = ""
        continueButton.alpha = 0.3f
        continueProgressBar.visibility = View.VISIBLE

//        get inputs' values
        var email = emailEditText.text.toString()
        var password = passwordEditText.text.toString()

//        send sign-up request
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
//                cleanup variables with sensetive data to avoid it saving in memory
                email = ""
                password = ""

//                handle result of auth
                handleResult(task.isSuccessful)
            }
    }

    private fun handleResult(isSuccess: Boolean) {
//        get current user
        val currentUser = auth.currentUser

        if(isSuccess && currentUser != null) {
//            Авторизация также может не пройти, если пароль оказался
//            слишком слабым. Это определяет сам Firebase

//            cleanup fields to avoid saving data in memory
            emailEditText.setText("")
            passwordEditText.setText("")


//            init user document
            val userDoc = User(
                uid = currentUser.uid,
                email = currentUser.email,
                name = currentUser.displayName,
                score = 0
            )

//            save user document to firestore
            store.collection("users")
                .document(currentUser.uid)
                .set(userDoc)
                .addOnCompleteListener { task ->
//                    notify that saving document failed
                    if (!task.isSuccessful) {
                        Toast.makeText(this, "Failed to Save User Document", Toast.LENGTH_SHORT).show()
                    }

/*

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
*/

                    startActivity(Intent(this, MainActivity::class.java).apply {
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }

            return
        }


//        notify that sign-up failed
        Toast.makeText(this, "Failed to Sign Up", Toast.LENGTH_SHORT).show()
//        return button state to initial
        continueButton.isClickable = true
        continueButton.text = "Sign Up"
        continueButton.alpha = 1f
        continueProgressBar.visibility = View.GONE
    }
}