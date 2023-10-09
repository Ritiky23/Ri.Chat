package com.example.test1
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE
import com.example.test1.SessionManager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import org.w3c.dom.Text

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
        setContentView(R.layout.activity_login)
        val login: Button=findViewById(R.id.loginbut)
        val email: EditText=findViewById(R.id.loginemail)
        val pass: EditText=findViewById(R.id.loginpass)
        val regbtn: TextView=findViewById(R.id.goregister)
        regbtn.setOnClickListener {
            val intent = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(intent)
        }
        login.setOnClickListener {
            val email1=email.text.toString()
            val pass1=pass.text.toString()
            if(email1.isNotEmpty() && pass1.isNotEmpty()){
            firebaseAuth.signInWithEmailAndPassword(email1,pass1).addOnCompleteListener {
                if(it.isSuccessful){
                    saveAuthenticationStateToStorage(true)
                    val intent = Intent(this@LoginActivity, homeactivity::class.java)
                    startActivity(intent)
                }
            }

        }
        }
    }
    private fun saveAuthenticationStateToStorage(isLoggedIn: Boolean) {
        val sessionManager = SessionManager(this)
        sessionManager.saveAuthenticationState(isLoggedIn)
    }

}