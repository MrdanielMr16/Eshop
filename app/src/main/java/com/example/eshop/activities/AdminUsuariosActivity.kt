package com.example.eshop.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eshop.R
import com.example.eshop.adapters.UsuarioAdapter
import com.example.eshop.database.UsuarioDAO
import com.example.eshop.models.Usuarios
import com.google.android.material.appbar.MaterialToolbar

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var listViewUsuarios: ListView
    private lateinit var btnAgregarUsuario: Button
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var usuarios: MutableList<Usuarios>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        usuarioDAO = UsuarioDAO(this)
        listViewUsuarios = findViewById(R.id.listViewUsuarios)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuarios)

        btnAgregarUsuario.setOnClickListener {
            // Usamos tu RegisterActivity para crear un nuevo usuario
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Click corto: editar usuario
        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            val usuario = usuarios[position]
            mostrarDialogoEditarUsuario(usuario)
        }

        // Click largo: eliminar usuario
        listViewUsuarios.setOnItemLongClickListener { _, _, position, _ ->
            val usuario = usuarios[position]
            AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que deseas eliminar a ${usuario.nombre}?")
                .setPositiveButton("Sí") { _, _ ->
                    if (usuario.id != null && usuarioDAO.eliminarUsuario(usuario.id)) {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        // Para recargar la lista cuando regreses de RegisterActivity
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        usuarios = usuarioDAO.obtenerTodosLosUsuarios().toMutableList()
        val adapter = UsuarioAdapter(this, usuarios)
        listViewUsuarios.adapter = adapter
    }

    private fun mostrarDialogoEditarUsuario(usuario: Usuarios) {
        val view = layoutInflater.inflate(R.layout.dialog_editar_usuario, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreUsuarioDialog)
        val spinnerRol = view.findViewById<Spinner>(R.id.spinnerRolUsuarioDialog)

        // Rellenar campos
        editNombre.setText(usuario.nombre)

        // Configurar spinner de roles
        val roles = listOf("admin", "usuario")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapterSpinner

        val indexRol = roles.indexOf(usuario.rol)
        if (indexRol >= 0) spinnerRol.setSelection(indexRol)

        AlertDialog.Builder(this)
            .setTitle("Editar usuario")
            .setView(view)
            .setPositiveButton("Guardar cambios") { _: DialogInterface, _: Int ->
                val nuevoNombre = editNombre.text.toString().trim()
                val nuevoRol = spinnerRol.selectedItem.toString()

                if (nuevoNombre.isBlank()) {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                var ok = true

                if (usuario.id != null) {
                    ok = usuarioDAO.actualizarNombre(usuario.id, nuevoNombre) &&
                            usuarioDAO.actualizarRol(usuario.id, nuevoRol)
                }

                if (ok) {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)

        // Leer rol por si lo necesitas (en admin igual será "admin")
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val rol = prefs.getString("rol", "usuario")

        // Si quisieras ocultar algo dependiendo del rol, lo puedes hacer aquí
        // val itemUsuarios = menu.findItem(R.id.menuUsuarios)
        // itemUsuarios.isVisible = rol == "admin"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                // Ya estás en HomeActivity, no hace falta hacer nada especial
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menuUsuarios -> {
                // TODO: crea esta activity para CRUD de usuarios
                val intent = Intent(this, AdminUsuariosActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menuProductos -> {
                val intent = Intent(this, AdminProductosActivity::class.java)
                startActivity(intent)
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
}