package com.example.techstore.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.adapters.UsuarioAdapter
import com.example.techstore.database.UsuarioDAO
import com.example.techstore.network.SupabaseClientProvider
import com.example.techstore.network.SupabaseUsuarioRepository
import com.example.techstore.models.Usuarios
import com.google.android.material.appbar.MaterialToolbar

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var listViewUsuarios: ListView
    private lateinit var btnAgregarUsuario: Button
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var supabaseUsuarioRepository: SupabaseUsuarioRepository
    private lateinit var usuarios: MutableList<Usuarios>
    private val usuarioRows = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        usuarioDAO = UsuarioDAO(this)
        supabaseUsuarioRepository = SupabaseUsuarioRepository()
        listViewUsuarios = findViewById(R.id.listViewUsuarios)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuarios)

        btnAgregarUsuario.setOnClickListener {
            mostrarDialogoCrearUsuario()
        }

        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            mostrarDialogoEditarUsuario(usuarios[position])
        }

        listViewUsuarios.setOnItemLongClickListener { _, _, position, _ ->
            confirmarEliminarUsuario(usuarios[position])
            true
        }
    }

    override fun onResume() {
        super.onResume()
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        if (SupabaseClientProvider.isConfigured) {
            supabaseUsuarioRepository.obtenerTodos({ registros ->
                usuarioRows.clear()
                usuarios = registros.map { record ->
                    usuarioRows[record.usuario.id] = record.rowId
                    record.usuario
                }.toMutableList()
                listViewUsuarios.adapter = UsuarioAdapter(this, usuarios)
            }, { error -> mostrarError(error) })
        } else {
            usuarios = usuarioDAO.obtenerTodosLosUsuarios().toMutableList()
            val adapter = UsuarioAdapter(this, usuarios)
            listViewUsuarios.adapter = adapter
        }
    }

    private fun mostrarDialogoCrearUsuario() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
        }
        val editNombre = EditText(this).apply { hint = "Nombre completo" }
        val editEmail = EditText(this).apply { hint = "Correo electronico" }
        val editPassword = EditText(this).apply { hint = "Contraseña" }
        val spinnerRol = Spinner(this)
        val roles = listOf("admin", "vendedor", "usuario")
        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        layout.addView(editNombre)
        layout.addView(editEmail)
        layout.addView(editPassword)
        layout.addView(spinnerRol)

        AlertDialog.Builder(this)
            .setTitle("Crear usuario")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val usuario = Usuarios(
                    nombre = editNombre.text.toString().trim(),
                    email = editEmail.text.toString().trim(),
                    password = editPassword.text.toString().trim(),
                    rol = spinnerRol.selectedItem.toString()
                )
                if (usuario.nombre.isBlank() || usuario.email.isBlank() || usuario.password.isBlank()) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (SupabaseClientProvider.isConfigured) {
                    supabaseUsuarioRepository.crear(usuario, {
                        Toast.makeText(this, "Usuario creado en Supabase", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }, { error -> mostrarError(error) })
                } else if (usuarioDAO.registrarUsuario(usuario)) {
                    Toast.makeText(this, "Usuario creado localmente", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(this, "Error al crear usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarUsuario(usuario: Usuarios) {
        val view = layoutInflater.inflate(R.layout.dialog_editar_usuario, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreUsuarioDialog)
        val spinnerRol = view.findViewById<Spinner>(R.id.spinnerRolUsuarioDialog)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarUsuarioDialog)

        editNombre.setText(usuario.nombre)

        val roles = listOf("admin", "vendedor", "usuario")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapterSpinner

        val indexRol = roles.indexOf(usuario.rol)
        if (indexRol >= 0) spinnerRol.setSelection(indexRol)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar usuario")
            .setView(view)
            .setPositiveButton("Guardar cambios") { _: DialogInterface, _: Int ->
                val nuevoNombre = editNombre.text.toString().trim()
                val nuevoRol = spinnerRol.selectedItem.toString()

                if (nuevoNombre.isBlank()) {
                    Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rowId = usuarioRows[usuario.id]
                if (SupabaseClientProvider.isConfigured && rowId != null) {
                    supabaseUsuarioRepository.actualizar(rowId, nuevoNombre, nuevoRol, {
                        Toast.makeText(this, "Usuario actualizado en Supabase", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }, { error -> mostrarError(error) })
                } else if (usuarioDAO.actualizarNombre(usuario.id, nuevoNombre) &&
                    usuarioDAO.actualizarRol(usuario.id, nuevoRol)
                ) {
                    Toast.makeText(this, "Usuario actualizado localmente", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        btnEliminar.setOnClickListener {
            confirmarEliminarUsuario(usuario) {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmarEliminarUsuario(usuario: Usuarios, onDeleted: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_user))
            .setMessage(getString(R.string.delete_user_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                eliminarUsuario(usuario, onDeleted)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun eliminarUsuario(usuario: Usuarios, onDeleted: (() -> Unit)? = null) {
        val rowId = usuarioRows[usuario.id]
        if (SupabaseClientProvider.isConfigured && rowId != null) {
            supabaseUsuarioRepository.eliminar(rowId, {
                Toast.makeText(this, "Usuario eliminado en Supabase", Toast.LENGTH_SHORT).show()
                cargarUsuarios()
                onDeleted?.invoke()
            }, { error -> mostrarError(error) })
        } else if (usuarioDAO.eliminarUsuario(usuario.id)) {
            Toast.makeText(this, "Usuario eliminado localmente", Toast.LENGTH_SHORT).show()
            cargarUsuarios()
            onDeleted?.invoke()
        } else {
            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        menu.findItem(R.id.menuCatalogo).isVisible = false
        menu.findItem(R.id.menuCarrito).isVisible = false
        menu.findItem(R.id.menuPagos).isVisible = false
        menu.findItem(R.id.menuPerfilComprador).isVisible = false
        menu.findItem(R.id.menuPedidosVendedor).isVisible = false
        menu.findItem(R.id.menuPerfilVendedor).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                startActivity(Intent(this, HomeActivity::class.java))
                return true
            }
            R.id.menuUsuarios -> {
                Toast.makeText(this, "Ya estas en Gestion de usuarios", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menuProductos -> {
                startActivity(Intent(this, AdminProductosActivity::class.java))
                return true
            }
            R.id.menuReportes -> {
                StaticScreenActivity.open(this, StaticScreenActivity.ADMIN_REPORTES)
                return true
            }
            R.id.menuBiometria -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BIOMETRIC_AUTH)
                return true
            }
            R.id.menuCerrarSesion -> {
                cerrarSesion()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarError(error: Exception) {
        Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
    }
}
