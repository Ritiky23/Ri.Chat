package com.example.test1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()
        val loginp:TextView = findViewById(R.id.textView2)
        val n: EditText = findViewById(R.id.editTextTextPersonName)
        val e: EditText = findViewById(R.id.editTextTextEmailAddress)
        val p: EditText = findViewById(R.id.loginpass)
        val cp: EditText = findViewById(R.id.editTextTextPassword2)
        val b: Button = findViewById(R.id.button)
        sessionManager = SessionManager(this)
        // Check if the user is already logged in
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this@MainActivity, homeactivity::class.java)
            startActivity(intent)
            finish()
        }
        else{

        }

        loginp.setOnClickListener{
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        b.setOnClickListener {
            val name = n.text.toString()

            val email = e.text.toString()
            val pass = p.text.toString()
            val cpass = cp.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && cpass.isNotEmpty()) {
                if (pass == cpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            addUserToDatabase(name,email, firebaseAuth.currentUser?.uid!!)
                            val intent = Intent(this@MainActivity, homeactivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@MainActivity, it.exception?.message, Toast.LENGTH_SHORT).show()

                        }

                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty fields Are not Allowed!!", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun addUserToDatabase(name: String,email: String,uid: String){
        database=FirebaseDatabase.getInstance().getReference()
        database.child("user").child(uid).setValue(User(name,email,uid,profilePictureUrl = null))
    }

}