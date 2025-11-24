package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eshop.R
import com.example.eshop.database.ProductoDAO
import com.example.eshop.models.Producto
import com.google.android.material.appbar.MaterialToolbar

class AdminProductosActivity : AppCompatActivity() {

    private lateinit var listViewProductos: ListView
    private lateinit var btnAgregarProducto: Button
    private lateinit var productoDAO: ProductoDAO
    private lateinit var productos: MutableList<Producto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_productos)


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        productoDAO = ProductoDAO(this)
        listViewProductos = findViewById(R.id.listViewProductos)
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto)

        btnAgregarProducto.setOnClickListener {
            mostrarDialogoCrearProducto()
        }

        listViewProductos.setOnItemClickListener { _, _, position, _ ->
            val producto = productos[position]
            mostrarDialogoEditarProducto(producto)
        }

        listViewProductos.setOnItemLongClickListener { _, _, position, _ ->
            val producto = productos[position]
            AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que deseas eliminar ${producto.nombre}?")
                .setPositiveButton("Sí") { _, _ ->
                    if (producto.id != null && productoDAO.eliminarProducto(producto.id)) {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

        cargarProductos()
    }

    private fun cargarProductos() {
        productos = productoDAO.obtenerTodos().toMutableList()
        val adapter = ProductoAdapter(this, productos)
        listViewProductos.adapter = adapter
    }

    private fun mostrarDialogoCrearProducto() {
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreProducto)
        val editDescripcion = view.findViewById<EditText>(R.id.editDescripcionProducto)
        val editPrecio = view.findViewById<EditText>(R.id.editPrecioProducto)
        val editStock = view.findViewById<EditText>(R.id.editStockProducto)

        AlertDialog.Builder(this)
            .setTitle("Nuevo producto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val desc = editDescripcion.text.toString().trim()
                val precio = editPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val stock = editStock.text.toString().toIntOrNull() ?: 0

                if (nombre.isBlank()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val producto = Producto(
                    nombre = nombre,
                    descripcion = desc,
                    precio = precio,
                    stock = stock
                )

                if (productoDAO.insertarProducto(producto)) {
                    Toast.makeText(this, "Producto creado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this, "Error al crear producto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarProducto(producto: Producto) {
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreProducto)
        val editDescripcion = view.findViewById<EditText>(R.id.editDescripcionProducto)
        val editPrecio = view.findViewById<EditText>(R.id.editPrecioProducto)
        val editStock = view.findViewById<EditText>(R.id.editStockProducto)

        // Rellenar con los datos actuales
        editNombre.setText(producto.nombre)
        editDescripcion.setText(producto.descripcion)
        editPrecio.setText(producto.precio.toString())
        editStock.setText(producto.stock.toString())

        AlertDialog.Builder(this)
            .setTitle("Editar producto")
            .setView(view)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val nuevoNombre = editNombre.text.toString().trim()
                val nuevaDesc = editDescripcion.text.toString().trim()
                val nuevoPrecio = editPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val nuevoStock = editStock.text.toString().toIntOrNull() ?: 0

                if (nuevoNombre.isBlank()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Crear objeto actualizado (usando copy si es data class)
                val productoActualizado = producto.copy(
                    nombre = nuevoNombre,
                    descripcion = nuevaDesc,
                    precio = nuevoPrecio,
                    stock = nuevoStock
                )

                val exito = productoDAO.actualizarProducto(productoActualizado)

                if (exito) {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    cargarProductos()  // vuelves a llenar el ListView
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
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