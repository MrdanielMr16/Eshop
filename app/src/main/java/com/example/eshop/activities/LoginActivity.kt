package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.eshop.R
import com.example.eshop.database.UsuarioDAO
import com.example.eshop.models.Usuarios

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtVolver: TextView
    private lateinit var usuarioDAO: UsuarioDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        usuarioDAO = UsuarioDAO(this)
        initViews()
        setupListeners()
    }

    private fun setupListeners() {
        txtVolver.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        findViewById<TextView?>(R.id.txtRecuperarPassword)?.setOnClickListener {
            StaticScreenActivity.open(this, StaticScreenActivity.RECOVER_PASSWORD)
        }

        findViewById<Button?>(R.id.btnBiometria)?.setOnClickListener {
            StaticScreenActivity.open(this, StaticScreenActivity.BIOMETRIC_AUTH)
        }
    }

    private fun initViews() {
        editTextEmail = findViewById(R.id.editTextContrasena)
        editTextPassword = findViewById(R.id.editTextConfirmarContrasena)
        btnLogin = findViewById(R.id.btnRegistrar)
        txtVolver = findViewById(R.id.txtVolver)
    }

    private fun iniciarSesion() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Correo electronico invalido"
            return
        }

        if (password.isBlank()) {
            editTextPassword.error = "La contrasena no puede estar vacia"
            return
        }

        val esValido = usuarioDAO.validarLogin(email, password)
        val usuario = usuarioDAO.obtenerUsuario(email)
        if (esValido && usuario != null) {
            Toast.makeText(this, "Inicio de sesion exitoso, Bienvenido ${usuario.nombre}", Toast.LENGTH_LONG).show()
            guardarSesion(usuario)

            when (usuario.rol) {
                "admin" -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("NOMBRE_USUARIO", usuario.nombre)
                    startActivity(intent)
                }
                "vendedor" -> {
                    StaticScreenActivity.open(this, StaticScreenActivity.SELLER_DASHBOARD)
                }
                else -> {
                    val intent = Intent(this, HomeUserActivity::class.java)
                    intent.putExtra("NOMBRE_USUARIO", usuario.nombre)
                    startActivity(intent)
                }
            }
            finish()
        } else {
            Toast.makeText(this, "Correo o contrasena incorrectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarSesion(usuario: Usuarios) {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        prefs.edit {
            putString("email", usuario.email)
            putString("nombre", usuario.nombre)
            putString("rol", usuario.rol)
        }
    }
}
