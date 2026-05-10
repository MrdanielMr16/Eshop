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
import androidx.appcompat.app.AppCompatActivity
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
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            mostrarDialogoEditarUsuario(usuarios[position])
        }

        listViewUsuarios.setOnItemLongClickListener { _, _, position, _ ->
            val usuario = usuarios[position]
            AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("Seguro que deseas eliminar a ${usuario.nombre}?")
                .setPositiveButton("Si") { _, _ ->
                    if (usuarioDAO.eliminarUsuario(usuario.id)) {
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

        editNombre.setText(usuario.nombre)

        val roles = listOf("admin", "vendedor", "usuario")
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
                    Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val ok = usuarioDAO.actualizarNombre(usuario.id, nuevoNombre) &&
                        usuarioDAO.actualizarRol(usuario.id, nuevoRol)

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
}
