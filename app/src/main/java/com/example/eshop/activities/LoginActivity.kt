package com.example.eshop.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eshop.R
import com.example.eshop.database.UsuarioDAO
import com.example.eshop.models.Usuarios
import androidx.core.content.edit

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

        // Inicializamos el DAO
        usuarioDAO = UsuarioDAO(this)
        initViews()
        setupListeners()

    }

    private fun setupListeners() {
        // Botón de volver
        txtVolver.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }

        // Botón de inicio de sesión
        btnLogin.setOnClickListener {
            iniciarSesion()
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

        // Validaciones básicas
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Correo electrónico inválido"
            return
        }

        if (password.isBlank()) {
            editTextPassword.error = "La contraseña no puede estar vacía"
            return
        }

        // Validar credenciales con el DAO
        val esValido = usuarioDAO.validarLogin(email, password)
        val usuario = usuarioDAO.obtenerUsuario(email)
        if (esValido && usuario != null) {
            Toast.makeText(this, "Inicio de sesión exitoso, Bienvenido ${usuario.nombre}", Toast.LENGTH_LONG).show()
            guardarSesion(usuario)

            // Redirección según el rol
            if (usuario.rol == "admin") {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("NOMBRE_USUARIO", usuario.nombre)
                startActivity(intent)
            } else {
                val intent = Intent(this, HomeUserActivity::class.java)
                intent.putExtra("NOMBRE_USUARIO", usuario.nombre)
                startActivity(intent)
            }

            // Opcional: cerrar Login para que no pueda volver con atrás
            finish()
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }

    }

    private fun guardarSesion(usuario: Usuarios) {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        prefs.edit {
            putString("email", usuario.email)
            putString("nombre", usuario.nombre)
            putString("rol", usuario.rol)
            // apply() lo hace internamente
        }
    }
}


