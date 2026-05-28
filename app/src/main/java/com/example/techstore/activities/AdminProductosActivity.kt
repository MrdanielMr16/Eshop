package com.example.techstore.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.database.ProductoDAO
import com.example.techstore.network.SupabaseClientProvider
import com.example.techstore.network.SupabaseProductoCrudRepository
import com.example.techstore.models.Producto
import com.google.android.material.appbar.MaterialToolbar

class AdminProductosActivity : AppCompatActivity() {

    private lateinit var listViewProductos: ListView
    private lateinit var btnAgregarProducto: Button
    private lateinit var productoDAO: ProductoDAO
    private lateinit var supabaseProductoRepository: SupabaseProductoCrudRepository
    private lateinit var productos: MutableList<Producto>
    private val productoRows = mutableMapOf<Int, Int>()
    private var imagenGaleria: Uri? = null
    private var imagenCamara: Bitmap? = null

    private val seleccionarImagenGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imagenGaleria = uri
        imagenCamara = null
        if (uri != null) Toast.makeText(this, "Imagen seleccionada desde galeria", Toast.LENGTH_SHORT).show()
    }

    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imagenCamara = bitmap
        imagenGaleria = null
        if (bitmap != null) Toast.makeText(this, "Imagen tomada con camara", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_productos)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        toolbar.title = if (esVendedor()) getString(R.string.seller_products) else getString(R.string.manage_products)
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)
        productoDAO = ProductoDAO(this)
        supabaseProductoRepository = SupabaseProductoCrudRepository(this)
        listViewProductos = findViewById(R.id.listViewProductos)
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto)

        btnAgregarProducto.setOnClickListener {
            mostrarDialogoCrearProducto()
        }

        listViewProductos.setOnItemClickListener { _, _, position, _ ->
            mostrarDialogoEditarProducto(productos[position])
        }

        listViewProductos.setOnItemLongClickListener { _, _, position, _ ->
            confirmarEliminarProducto(productos[position])
            true
        }

        cargarProductos()
    }

    private fun cargarProductos() {
        if (SupabaseClientProvider.isConfigured) {
            supabaseProductoRepository.obtenerTodos({ registros ->
                productoRows.clear()
                productos = registros.map { record ->
                    record.producto.id?.let { productoRows[it] = record.rowId }
                    record.producto
                }.toMutableList()
                listViewProductos.adapter = crearAdapterProductos()
            }, { error -> mostrarError(error) })
        } else {
            productos = productoDAO.obtenerTodos().toMutableList()
            listViewProductos.adapter = crearAdapterProductos()
        }
    }

    private fun crearAdapterProductos(): ProductoAdapter {
        return ProductoAdapter(
            this,
            productos,
            onEdit = { producto -> mostrarDialogoEditarProducto(producto) },
            onDelete = { producto -> confirmarEliminarProducto(producto) }
        )
    }

    private fun mostrarDialogoCrearProducto() {
        imagenGaleria = null
        imagenCamara = null
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreProducto)
        val editDescripcion = view.findViewById<EditText>(R.id.editDescripcionProducto)
        val editPrecio = view.findViewById<EditText>(R.id.editPrecioProducto)
        val editStock = view.findViewById<EditText>(R.id.editStockProducto)
        val btnGaleria = view.findViewById<Button>(R.id.btnSeleccionarGaleria)
        val btnCamara = view.findViewById<Button>(R.id.btnTomarFoto)

        btnGaleria.setOnClickListener { seleccionarImagenGaleria.launch("image/*") }
        btnCamara.setOnClickListener { tomarFoto.launch(null) }

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
                if (SupabaseClientProvider.isConfigured) {
                    supabaseProductoRepository.crear(producto, imagenGaleria, imagenCamara, {
                        Toast.makeText(this, "Producto creado en Supabase", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    }, { error -> mostrarError(error) })
                } else if (productoDAO.insertarProducto(producto)) {
                    Toast.makeText(this, "Producto creado localmente", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this, "Error al crear producto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarProducto(producto: Producto) {
        imagenGaleria = null
        imagenCamara = null
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val editNombre = view.findViewById<EditText>(R.id.editNombreProducto)
        val editDescripcion = view.findViewById<EditText>(R.id.editDescripcionProducto)
        val editPrecio = view.findViewById<EditText>(R.id.editPrecioProducto)
        val editStock = view.findViewById<EditText>(R.id.editStockProducto)
        val btnGaleria = view.findViewById<Button>(R.id.btnSeleccionarGaleria)
        val btnCamara = view.findViewById<Button>(R.id.btnTomarFoto)

        editNombre.setText(producto.nombre)
        editDescripcion.setText(producto.descripcion)
        editPrecio.setText(producto.precio.toString())
        editStock.setText(producto.stock.toString())
        btnGaleria.setOnClickListener { seleccionarImagenGaleria.launch("image/*") }
        btnCamara.setOnClickListener { tomarFoto.launch(null) }

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

                val rowId = producto.id?.let { productoRows[it] }
                if (SupabaseClientProvider.isConfigured && rowId != null) {
                    supabaseProductoRepository.actualizar(rowId, productoActualizado, imagenGaleria, imagenCamara, {
                        Toast.makeText(this, "Producto actualizado en Supabase", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    }, { error -> mostrarError(error) })
                } else if (productoDAO.actualizarProducto(productoActualizado)) {
                    Toast.makeText(this, "Producto actualizado localmente", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminarProducto(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_product))
            .setMessage(getString(R.string.delete_product_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                eliminarProducto(producto)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun eliminarProducto(producto: Producto) {
        val rowId = producto.id?.let { productoRows[it] }
        if (SupabaseClientProvider.isConfigured && rowId != null) {
            supabaseProductoRepository.eliminar(rowId, {
                Toast.makeText(this, "Producto eliminado en Supabase", Toast.LENGTH_SHORT).show()
                cargarProductos()
            }, { error -> mostrarError(error) })
        } else if (producto.id != null && productoDAO.eliminarProducto(producto.id)) {
            Toast.makeText(this, "Producto eliminado localmente", Toast.LENGTH_SHORT).show()
            cargarProductos()
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
        if (esVendedor()) {
            menu.findItem(R.id.menuUsuarios).isVisible = false
            menu.findItem(R.id.menuReportes).isVisible = false
            menu.findItem(R.id.menuPedidosVendedor).isVisible = true
            menu.findItem(R.id.menuPerfilVendedor).isVisible = true
        } else {
            menu.findItem(R.id.menuPedidosVendedor).isVisible = false
            menu.findItem(R.id.menuPerfilVendedor).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                if (esVendedor()) {
                    StaticScreenActivity.open(this, StaticScreenActivity.SELLER_DASHBOARD)
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
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
            R.id.menuPedidosVendedor -> {
                StaticScreenActivity.open(this, StaticScreenActivity.SELLER_ORDERS)
                return true
            }
            R.id.menuPerfilVendedor -> {
                StaticScreenActivity.open(this, StaticScreenActivity.SELLER_PROFILE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun esVendedor(): Boolean {
        return getSharedPreferences("sesion", MODE_PRIVATE).getString("rol", "") == "vendedor"
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
