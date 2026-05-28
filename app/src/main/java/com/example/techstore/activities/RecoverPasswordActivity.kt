package com.example.techstore.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.database.UsuarioDAO
import com.example.techstore.network.SupabaseClientProvider
import com.example.techstore.network.SupabaseUsuarioRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlin.random.Random

class RecoverPasswordActivity : AppCompatActivity() {
    private lateinit var editEmail: EditText
    private lateinit var editCodigo: EditText
    private lateinit var editNuevaPassword: EditText
    private lateinit var editConfirmarPassword: EditText
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var supabaseUsuarioRepository: SupabaseUsuarioRepository
    private var codigoGenerado: String = ""
    private var emailValidado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        usuarioDAO = UsuarioDAO(this)
        supabaseUsuarioRepository = SupabaseUsuarioRepository()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarRecoverPassword)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        editEmail = findViewById(R.id.editRecoverEmail)
        editCodigo = findViewById(R.id.editCodigoRecuperacion)
        editNuevaPassword = findViewById(R.id.editNuevaPassword)
        editConfirmarPassword = findViewById(R.id.editConfirmarNuevaPassword)

        findViewById<Button>(R.id.btnEnviarCodigo).setOnClickListener { enviarCodigo() }
        findViewById<Button>(R.id.btnActualizarPassword).setOnClickListener { actualizarPassword() }
        findViewById<Button>(R.id.btnVolverRecover).setOnClickListener { finish() }
    }

    private fun enviarCodigo() {
        val email = editEmail.text.toString().trim().lowercase()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Correo electronico invalido"
            return
        }

        if (SupabaseClientProvider.isConfigured) {
            supabaseUsuarioRepository.obtenerPorEmail(email, { usuario ->
                if (usuario == null) {
                    Toast.makeText(this, "No existe una cuenta con ese correo", Toast.LENGTH_LONG).show()
                } else {
                    generarCodigo(email)
                }
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            val usuario = usuarioDAO.obtenerUsuario(email)
            if (usuario == null) {
                Toast.makeText(this, "No existe una cuenta con ese correo", Toast.LENGTH_LONG).show()
            } else {
                generarCodigo(email)
            }
        }
    }

    private fun generarCodigo(email: String) {
        codigoGenerado = Random.nextInt(100000, 999999).toString()
        emailValidado = email
        Toast.makeText(this, "Codigo de recuperacion: $codigoGenerado", Toast.LENGTH_LONG).show()
    }

    private fun actualizarPassword() {
        val email = editEmail.text.toString().trim().lowercase()
        val codigo = editCodigo.text.toString().trim()
        val nuevaPassword = editNuevaPassword.text.toString().trim()
        val confirmarPassword = editConfirmarPassword.text.toString().trim()

        if (codigoGenerado.isBlank() || email != emailValidado) {
            Toast.makeText(this, "Primero solicita un codigo para este correo", Toast.LENGTH_SHORT).show()
            return
        }

        if (codigo != codigoGenerado) {
            editCodigo.error = "Codigo incorrecto"
            return
        }

        if (nuevaPassword.length < 6) {
            editNuevaPassword.error = "La contrasena debe tener al menos 6 caracteres"
            return
        }

        if (nuevaPassword != confirmarPassword) {
            editConfirmarPassword.error = "Las contrasenas no coinciden"
            return
        }

        if (SupabaseClientProvider.isConfigured) {
            supabaseUsuarioRepository.actualizarPassword(email, nuevaPassword, {
                Toast.makeText(this, "Contrasena actualizada", Toast.LENGTH_LONG).show()
                volverAlLogin()
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            val usuario = usuarioDAO.obtenerUsuario(email)
            if (usuario != null && usuarioDAO.actualizarPassword(usuario.id, nuevaPassword)) {
                Toast.makeText(this, "Contrasena actualizada", Toast.LENGTH_LONG).show()
                volverAlLogin()
            } else {
                Toast.makeText(this, "No se pudo actualizar la contrasena", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun volverAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, RecoverPasswordActivity::class.java))
        }
    }
}
