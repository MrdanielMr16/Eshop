package com.example.techstore.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.database.UsuarioDAO
import com.example.techstore.models.Usuarios
import com.example.techstore.network.SupabaseClientProvider
import com.example.techstore.network.SupabaseUsuarioRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var editTextConfirmarContrasena: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtVolver: TextView
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var supabaseUsuarioRepository: SupabaseUsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        usuarioDAO = UsuarioDAO(this)
        supabaseUsuarioRepository = SupabaseUsuarioRepository()
        initViews()
        setupListeners()
    }

    private fun setupListeners() {
        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
        txtVolver.setOnClickListener {
            finish()
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
        val email = editTextCorreo.text.toString().trim().lowercase()
        val password = editTextContrasena.text.toString().trim()
        val confirmar = editTextConfirmarContrasena.text.toString().trim()

        val rol = when (email) {
            "admin@techstore.com" -> "admin"
            "vendedor@techstore.com" -> "vendedor"
            else -> "usuario"
        }

        if (nombre.isBlank()) {
            editTextNombre.error = "El nombre no puede estar vacio"
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextCorreo.error = "Correo electronico invalido"
            return
        }

        if (password.length < 6) {
            editTextContrasena.error = "La contrasena debe tener al menos 6 caracteres"
            return
        }

        if (password != confirmar) {
            editTextConfirmarContrasena.error = "Las contrasenas no coinciden"
            return
        }

        val nuevoUsuario = Usuarios(nombre = nombre, email = email, password = password, rol = rol)

        if (SupabaseClientProvider.isConfigured) {
            btnRegistrar.isEnabled = false
            supabaseUsuarioRepository.obtenerPorEmail(email, { usuarioExistente ->
                if (usuarioExistente != null) {
                    btnRegistrar.isEnabled = true
                    Toast.makeText(this, "El correo ya esta registrado en Supabase", Toast.LENGTH_SHORT).show()
                    return@obtenerPorEmail
                }

                supabaseUsuarioRepository.crear(nuevoUsuario, {
                    btnRegistrar.isEnabled = true
                    Toast.makeText(this, "Usuario registrado en Supabase como $rol", Toast.LENGTH_LONG).show()
                    limpiarCampos()
                    finish()
                }, { error ->
                    btnRegistrar.isEnabled = true
                    Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
                })
            }, { error ->
                btnRegistrar.isEnabled = true
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            val usuarioExistente = usuarioDAO.obtenerUsuario(email)
            if (usuarioExistente != null) {
                Toast.makeText(this, "El correo ya esta registrado", Toast.LENGTH_SHORT).show()
                return
            }

            val exito = usuarioDAO.registrarUsuario(nuevoUsuario)

            if (exito) {
                Toast.makeText(this, "Usuario registrado correctamente como $rol", Toast.LENGTH_LONG).show()
                limpiarCampos()
                finish()
            } else {
                Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun limpiarCampos() {
        editTextNombre.text.clear()
        editTextCorreo.text.clear()
        editTextContrasena.text.clear()
        editTextConfirmarContrasena.text.clear()
    }
}
