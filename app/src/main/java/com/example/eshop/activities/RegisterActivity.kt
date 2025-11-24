package com.example.eshop.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.eshop.R
import com.example.eshop.database.UsuarioDAO
import com.example.eshop.models.Usuarios

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var editTextConfirmarContrasena: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtVolver: TextView
    private val usuarioDAO = UsuarioDAO(this)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        var usuarioDAO = UsuarioDAO(this)
        initViews()
        setupListeners()
    }

    private fun setupListeners() {
        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
        txtVolver.setOnClickListener {
            finish() // Vuelve al login o pantalla anterior
        }
    }

    private fun initViews() {
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextCorreo = findViewById(R.id.editTextCorreo)
        editTextContrasena = findViewById(R.id.editTextContrasena)
        editTextConfirmarContrasena = findViewById(R.id.editTextConfirmarContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        txtVolver = findViewById(R.id.txtVolver)
    }

    private fun registrarUsuario() {
        val nombre = editTextNombre.text.toString().trim()
        val email = editTextCorreo.text.toString().trim()
        val password = editTextContrasena.text.toString().trim()
        val confirmar = editTextConfirmarContrasena.text.toString().trim()

        // Si se registra con este correo, será admin
        val rol = if (email == "admin@eshop.com") {
            "admin"
        } else {
            "usuario"
        }

        // Validaciones simples
        if (nombre.isBlank()) {
            editTextNombre.error = "El nombre no puede estar vacío"
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextCorreo.error = "Correo electrónico inválido"
            return
        }

        if (password.length < 6) {
            editTextContrasena.error = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (password != confirmar) {
            editTextConfirmarContrasena.error = "Las contraseñas no coinciden"
            return
        }

        // Verificar si el usuario ya existe
        val usuarioExistente = usuarioDAO.obtenerUsuario(email)
        if (usuarioExistente != null) {
            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear y registrar usuario
        var nuevoUsuario = Usuarios(nombre = nombre, email = email, password = password, rol = rol)
        var exito = usuarioDAO.registrarUsuario(nuevoUsuario)

        if (exito) {
            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_LONG).show()
            limpiarCampos()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_LONG).show()
        }
    }
    private fun limpiarCampos() {
        editTextNombre.text.clear()
        editTextCorreo.text.clear()
        editTextContrasena.text.clear()
        editTextConfirmarContrasena.text.clear()
    }
}


