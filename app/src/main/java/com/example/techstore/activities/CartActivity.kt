package com.example.techstore.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.models.CarritoItem
import com.example.techstore.network.SupabaseCarritoOrdenRepository
import com.example.techstore.network.SupabaseClientProvider
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {
    private lateinit var listViewCarrito: ListView
    private lateinit var tvCarritoVacio: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvEnvio: TextView
    private lateinit var tvTotal: TextView
    private lateinit var repository: SupabaseCarritoOrdenRepository
    private val items = mutableListOf<CarritoItem>()
    private val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        repository = SupabaseCarritoOrdenRepository()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCarrito)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        listViewCarrito = findViewById(R.id.listViewCarrito)
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio)
        tvSubtotal = findViewById(R.id.tvSubtotalCarrito)
        tvEnvio = findViewById(R.id.tvEnvioCarrito)
        tvTotal = findViewById(R.id.tvTotalCarrito)

        findViewById<Button>(R.id.btnContinuarPagoCarrito).setOnClickListener {
            PaymentActivity.open(this)
        }

        cargarCarrito()
    }

    private fun cargarCarrito() {
        val usuarioId = usuarioId()
        if (SupabaseClientProvider.isConfigured) {
            repository.obtenerCarrito(usuarioId, { carrito ->
                items.clear()
                items.addAll(carrito)
                mostrarCarrito()
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
                cargarCarritoLocal()
            })
        } else {
            cargarCarritoLocal()
        }
    }

    private fun cargarCarritoLocal() {
        items.clear()
        items.addAll(leerCarritoLocal(this))
        mostrarCarrito()
    }

    private fun mostrarCarrito() {
        listViewCarrito.adapter = CartAdapter(this, items) { item ->
            eliminarItem(item)
        }

        val estaVacio = items.isEmpty()
        tvCarritoVacio.visibility = if (estaVacio) View.VISIBLE else View.GONE
        val subtotal = items.sumOf { it.subtotal }
        val envio = if (subtotal > 0.0) 50000.0 else 0.0
        val total = subtotal + envio
        tvSubtotal.text = "${getString(R.string.subtotal)}: $${numberFormat.format(subtotal)}"
        tvEnvio.text = "${getString(R.string.shipping)}: $${numberFormat.format(envio)}"
        tvTotal.text = "${getString(R.string.total)}: $${numberFormat.format(total)}"
    }

    private fun eliminarItem(item: CarritoItem) {
        if (SupabaseClientProvider.isConfigured && item.id.isNotBlank()) {
            repository.eliminarDelCarrito(item.id, {
                Toast.makeText(this, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
                cargarCarrito()
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            val actualizado = leerCarritoLocal(this).filterNot { it.id == item.id }
            guardarCarritoLocal(this, actualizado)
            Toast.makeText(this, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
            cargarCarritoLocal()
        }
    }

    private fun usuarioId(): String {
        return getSharedPreferences("sesion", MODE_PRIVATE).getString("email", null).orEmpty().ifBlank { "local_user" }
    }

    companion object {
        private const val PREFS_CART = "carrito_local"
        private const val KEY_ITEMS = "items"

        fun open(context: Context) {
            context.startActivity(Intent(context, CartActivity::class.java))
        }

        fun guardarItemLocal(context: Context, item: CarritoItem) {
            val items = leerCarritoLocal(context).toMutableList()
            val index = items.indexOfFirst { it.productoId == item.productoId }
            if (index >= 0) {
                val existente = items[index]
                items[index] = existente.copy(cantidad = existente.cantidad + 1)
            } else {
                items.add(item.copy(id = item.id.ifBlank { System.currentTimeMillis().toString() }))
            }
            guardarCarritoLocal(context, items)
        }

        fun leerCarritoLocal(context: Context): List<CarritoItem> {
            val raw = context.getSharedPreferences(PREFS_CART, Context.MODE_PRIVATE).getString(KEY_ITEMS, "[]")
            val array = JSONArray(raw)
            return (0 until array.length()).map { index ->
                val json = array.getJSONObject(index)
                CarritoItem(
                    id = json.optString("id"),
                    usuarioId = json.optString("usuarioId"),
                    productoId = json.optString("productoId"),
                    nombreProducto = json.optString("nombreProducto"),
                    cantidad = json.optInt("cantidad", 1),
                    precioUnitario = json.optDouble("precioUnitario", 0.0),
                    imagenUrl = json.optString("imagenUrl")
                )
            }
        }

        fun guardarCarritoLocal(context: Context, items: List<CarritoItem>) {
            val array = JSONArray()
            items.forEach { item ->
                array.put(JSONObject().apply {
                    put("id", item.id)
                    put("usuarioId", item.usuarioId)
                    put("productoId", item.productoId)
                    put("nombreProducto", item.nombreProducto)
                    put("cantidad", item.cantidad)
                    put("precioUnitario", item.precioUnitario)
                    put("imagenUrl", item.imagenUrl)
                })
            }
            context.getSharedPreferences(PREFS_CART, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS, array.toString())
                .apply()
        }
    }
}
