package com.example.techstore.activities

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.models.CarritoItem
import com.example.techstore.models.Producto
import com.example.techstore.network.SupabaseCarritoOrdenRepository
import com.example.techstore.network.SupabaseClientProvider
import com.google.android.material.appbar.MaterialToolbar
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors

class ProductDetailActivity : AppCompatActivity() {

    private val imageExecutor = Executors.newSingleThreadExecutor()
    private val carritoRepository = SupabaseCarritoOrdenRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetalleProducto)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val producto = productoDesdeIntent()
        mostrarProducto(producto)

        findViewById<Button>(R.id.btnAgregarCarritoDetalle).setOnClickListener {
            agregarAlCarrito(producto)
        }
        findViewById<Button>(R.id.btnVolverDetalleProducto).setOnClickListener {
            finish()
        }
    }

    private fun productoDesdeIntent(): Producto {
        return Producto(
            id = intent.getIntExtra(EXTRA_ID, 0).takeIf { it > 0 },
            nombre = intent.getStringExtra(EXTRA_NOMBRE).orEmpty(),
            descripcion = intent.getStringExtra(EXTRA_DESCRIPCION).orEmpty(),
            precio = intent.getDoubleExtra(EXTRA_PRECIO, 0.0),
            stock = intent.getIntExtra(EXTRA_STOCK, 0),
            imagenUrl = intent.getStringExtra(EXTRA_IMAGEN_URL).orEmpty()
        )
    }

    private fun mostrarProducto(producto: Producto) {
        findViewById<TextView>(R.id.tvDetalleNombreProducto).text = producto.nombre
        findViewById<TextView>(R.id.tvDetallePrecioProducto).text = getString(
            R.string.price_format,
            NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO")).format(producto.precio)
        )
        findViewById<TextView>(R.id.tvDetalleStockProducto).text = getString(R.string.stock_format, producto.stock)
        findViewById<TextView>(R.id.tvDetalleDescripcionProducto).text = producto.descripcion.ifBlank {
            getString(R.string.product_default_description)
        }
        cargarImagen(producto.imagenUrl, findViewById(R.id.imgDetalleProducto))
    }

    private fun agregarAlCarrito(producto: Producto) {
        val item = CarritoItem(
            usuarioId = usuarioId(),
            productoId = producto.id?.toString().orEmpty().ifBlank { producto.nombre },
            nombreProducto = producto.nombre,
            cantidad = 1,
            precioUnitario = producto.precio,
            imagenUrl = producto.imagenUrl
        )

        if (SupabaseClientProvider.isConfigured) {
            carritoRepository.agregarAlCarrito(item, {
                Toast.makeText(this, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                CartActivity.open(this)
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            CartActivity.guardarItemLocal(this, item)
            Toast.makeText(this, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
            CartActivity.open(this)
        }
    }

    private fun usuarioId(): String {
        return getSharedPreferences("sesion", MODE_PRIVATE).getString("email", null).orEmpty().ifBlank { "local_user" }
    }

    private fun cargarImagen(imagenUrl: String, imageView: ImageView) {
        imageView.tag = imagenUrl
        imageView.setImageResource(R.drawable.logo)
        if (imagenUrl.isBlank()) return

        imageExecutor.execute {
            try {
                val bitmap = URL(imagenUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
                imageView.post {
                    if (imageView.tag == imagenUrl && bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (_: Exception) {
                imageView.post {
                    if (imageView.tag == imagenUrl) {
                        imageView.setImageResource(R.drawable.logo)
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_ID = "producto_id"
        private const val EXTRA_NOMBRE = "producto_nombre"
        private const val EXTRA_DESCRIPCION = "producto_descripcion"
        private const val EXTRA_PRECIO = "producto_precio"
        private const val EXTRA_STOCK = "producto_stock"
        private const val EXTRA_IMAGEN_URL = "producto_imagen_url"

        fun open(context: Context, producto: Producto) {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                producto.id?.let { putExtra(EXTRA_ID, it) }
                putExtra(EXTRA_NOMBRE, producto.nombre)
                putExtra(EXTRA_DESCRIPCION, producto.descripcion)
                putExtra(EXTRA_PRECIO, producto.precio)
                putExtra(EXTRA_STOCK, producto.stock)
                putExtra(EXTRA_IMAGEN_URL, producto.imagenUrl)
            }
            context.startActivity(intent)
        }
    }
}
