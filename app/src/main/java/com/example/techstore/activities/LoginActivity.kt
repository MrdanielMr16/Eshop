package com.example.techstore.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.techstore.R
import com.example.techstore.BuildConfig
import com.example.techstore.database.UsuarioDAO
import com.example.techstore.models.Usuarios
import com.example.techstore.network.SupabaseClientProvider
import com.example.techstore.network.SupabaseUsuarioRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtVolver: TextView
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var supabaseUsuarioRepository: SupabaseUsuarioRepository
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        usuarioDAO = UsuarioDAO(this)
        supabaseUsuarioRepository = SupabaseUsuarioRepository()
        credentialManager = CredentialManager.create(this)
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
            iniciarSesionConBiometria()
        }

        findViewById<Button?>(R.id.btnGoogleLogin)?.setOnClickListener {
            iniciarSesionConGoogle()
        }
    }

    private fun initViews() {
        editTextEmail = findViewById(R.id.editTextContrasena)
        editTextPassword = findViewById(R.id.editTextConfirmarContrasena)
        btnLogin = findViewById(R.id.btnRegistrar)
        txtVolver = findViewById(R.id.txtVolver)
    }

    private fun iniciarSesion() {
        val email = editTextEmail.text.toString().trim().lowercase()
        val password = editTextPassword.text.toString().trim()

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Correo electronico invalido"
            return
        }

        if (password.isBlank()) {
            editTextPassword.error = "La contrasena no puede estar vacia"
            return
        }

        if (SupabaseClientProvider.isConfigured) {
            btnLogin.isEnabled = false
            supabaseUsuarioRepository.validarLogin(email, password, { usuario ->
                btnLogin.isEnabled = true
                if (usuario != null) {
                    abrirSesion(usuario)
                } else {
                    Toast.makeText(this, "Correo o contrasena incorrectos en Supabase", Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                btnLogin.isEnabled = true
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            val esValido = usuarioDAO.validarLogin(email, password)
            val usuario = usuarioDAO.obtenerUsuario(email)
            if (esValido && usuario != null) {
                abrirSesion(usuario)
            } else {
                Toast.makeText(this, "Correo o contrasena incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesionConGoogle() {
        if (!SupabaseClientProvider.isConfigured) {
            Toast.makeText(this, "Configura Supabase antes de usar Google", Toast.LENGTH_LONG).show()
            return
        }

        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            Toast.makeText(this, "Configura GOOGLE_WEB_CLIENT_ID en local.properties", Toast.LENGTH_LONG).show()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                procesarCuentaGoogle(
                    email = credential.id.trim().lowercase(),
                    nombre = credential.displayName?.takeIf { it.isNotBlank() }
                )
            } catch (error: GetCredentialException) {
                Toast.makeText(this@LoginActivity, "No se pudo iniciar con Google: ${error.message}", Toast.LENGTH_LONG).show()
            } catch (error: Exception) {
                Toast.makeText(this@LoginActivity, "Error Google: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun procesarCuentaGoogle(email: String, nombre: String?) {
        if (email.isBlank()) {
            Toast.makeText(this, "La cuenta de Google no entrego correo", Toast.LENGTH_LONG).show()
            return
        }

        val nombreUsuario = nombre ?: email.substringBefore("@")
        supabaseUsuarioRepository.obtenerPorEmail(email, { usuarioExistente ->
            if (usuarioExistente != null) {
                abrirSesion(usuarioExistente)
            } else {
                val nuevoUsuario = Usuarios(
                    nombre = nombreUsuario,
                    email = email,
                    password = "google_login",
                    rol = resolverRol(email, "usuario")
                )
                supabaseUsuarioRepository.crear(nuevoUsuario, {
                    abrirSesion(nuevoUsuario)
                }, { error ->
                    Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
                })
            }
        }, { error ->
            Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
        })
    }

    private fun abrirSesion(usuario: Usuarios) {
        val rolSesion = resolverRol(usuario.email, usuario.rol)
        Toast.makeText(this, "Inicio de sesion exitoso, Bienvenido ${usuario.nombre}", Toast.LENGTH_LONG).show()
        guardarSesion(usuario, rolSesion)

        when (rolSesion) {
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
    }

    private fun iniciarSesionConBiometria() {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val nombre = prefs.getString("nombre", null)
        val rol = prefs.getString("rol", null)

        if (email.isNullOrBlank() || nombre.isNullOrBlank() || rol.isNullOrBlank()) {
            Toast.makeText(this, "Primero inicia sesion con correo y contrasena", Toast.LENGTH_LONG).show()
            return
        }

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> mostrarPromptBiometrico(email, nombre, rol, authenticators)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "Configura una huella, rostro o bloqueo de pantalla en el dispositivo", Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Este dispositivo no tiene sensor biometrico disponible", Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "El sensor biometrico no esta disponible ahora", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "La autenticacion biometrica no esta disponible", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarPromptBiometrico(email: String, nombre: String, rol: String, authenticators: Int) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    abrirSesion(Usuarios(nombre = nombre, email = email, password = "", rol = rol))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginActivity, errString, Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "No se pudo validar la huella", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ingreso a TechStore")
            .setSubtitle("Confirma tu identidad para continuar")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun resolverRol(email: String, rolGuardado: String): String {
        return when (email.trim().lowercase()) {
            "admin@techstore.com" -> "admin"
            "vendedor@techstore.com" -> "vendedor"
            else -> rolGuardado.trim().lowercase()
        }
    }

    private fun guardarSesion(usuario: Usuarios, rolSesion: String) {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        prefs.edit {
            putString("email", usuario.email)
            putString("nombre", usuario.nombre)
            putString("rol", rolSesion)
        }
    }
}
