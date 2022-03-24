package com.x3noku.mahaon.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.x3noku.mahaon.R
import com.x3noku.mahaon.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val store = Firebase.firestore
    private var user: User? = null

    private lateinit var scoreTextView: TextView
    private lateinit var increaseButton: Button
    private lateinit var decreaseButton: Button
    private lateinit var scoreProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

//        init auth
        auth = Firebase.auth

//        init views
        scoreTextView = findViewById(R.id.scoreTextView)
        increaseButton = findViewById(R.id.increaseButton)
        decreaseButton = findViewById(R.id.decreaseButton)
        scoreProgressBar = findViewById(R.id.scoreProgressBar)

//        handle button clicks
        increaseButton.setOnClickListener {
            if(user != null) {
                handleIsLoading(true)
                store.collection("users")
                    .document(user!!.uid!!)
                    .set(user!!.copy(score = user!!.score + 1))
            }
        }
        decreaseButton.setOnClickListener {
            if(user != null) {
                handleIsLoading(true)
                store.collection("users")
                    .document(user!!.uid!!)
                    .set(user!!.copy(score = user!!.score - 1))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if(currentUser != null) return fetchData(currentUser)

//        navigate to sign-up screen if user is not authorized
        startActivity(Intent(this, SignUpActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun fetchData(currentUser: FirebaseUser) {
//        check if document already exists
        handleIsLoading(true)
        store.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { handleUpdates(currentUser) }
            .addOnFailureListener {
//                document is broken, init new one
                val userDoc = User(
                    uid = currentUser.uid,
                    email = currentUser.email,
                    name = currentUser.displayName,
                    score = 0
                )

//                save user document to firestore
                store.collection("users")
                    .document(currentUser.uid)
                    .set(userDoc)
                    .addOnCompleteListener {
//                        reload activity
                        finish()
                        startActivity(intent)
                    }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdates(currentUser: FirebaseUser) {
        store.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                user = snapshot?.toObject<User>()

                if(e != null || snapshot == null || user == null) return@addSnapshotListener

                handleIsLoading(false)
                scoreTextView.text = "${user!!.score} points"
            }
    }

    private fun handleIsLoading(isLoading: Boolean) {
        if(isLoading) {
            increaseButton.apply {
                isClickable = false
                alpha = 0.3f
            }
            decreaseButton.apply {
                isClickable = false
                alpha = 0.3f
            }
            scoreProgressBar.visibility = View.VISIBLE
        }
        else {
            increaseButton.apply {
                isClickable = true
                alpha = 1f
            }
            decreaseButton.apply {
                isClickable = true
                alpha = 1f
            }
            scoreProgressBar.visibility = View.GONE
        }
    }
}