package com.example.eshop.activities

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eshop.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        val txtVolver = findViewById<TextView>(R.id.txtVolver)
        txtVolver.setOnClickListener {
            finish() // o startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}