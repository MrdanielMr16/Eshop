package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
            mostrarDialogoEditarProducto(productos[position])
        }

        listViewProductos.setOnItemLongClickListener { _, _, position, _ ->
            val producto = productos[position]
            AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("Seguro que deseas eliminar ${producto.nombre}?")
                .setPositiveButton("Si") { _, _ ->
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

                val producto = Producto(nombre = nombre, descripcion = desc, precio = precio, stock = stock)
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

        editNombre.setText(producto.nombre)
        editDescripcion.setText(producto.descripcion)
        editPrecio.setText(producto.precio.toString())
        editStock.setText(producto.stock.toString())

        AlertDialog.Builder(this)
            .setTitle("Editar producto")
            .setView(view)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val productoActualizado = producto.copy(
                    nombre = editNombre.text.toString().trim(),
                    descripcion = editDescripcion.text.toString().trim(),
                    precio = editPrecio.text.toString().toDoubleOrNull() ?: 0.0,
                    stock = editStock.text.toString().toIntOrNull() ?: 0
                )

                if (productoActualizado.nombre.isBlank()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (productoDAO.actualizarProducto(productoActualizado)) {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
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
                startActivity(Intent(this, AdminUsuariosActivity::class.java))
                return true
            }
            R.id.menuProductos -> {
                Toast.makeText(this, "Ya estas en Gestion de productos", Toast.LENGTH_SHORT).show()
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
