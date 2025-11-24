package com.example.eshop.activities

import android.os.Bundle
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

class AdminProductosActivity : AppCompatActivity() {

    private lateinit var listViewProductos: ListView
    private lateinit var btnAgregarProducto: Button
    private lateinit var productoDAO: ProductoDAO
    private lateinit var productos: MutableList<Producto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_productos)

        productoDAO = ProductoDAO(this)
        listViewProductos = findViewById(R.id.listViewProductos)
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto)

        btnAgregarProducto.setOnClickListener {
            mostrarDialogoCrearProducto()
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
        val nombres = productos.map { "${it.nombre} - $${it.precio} (Stock: ${it.stock})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
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
}