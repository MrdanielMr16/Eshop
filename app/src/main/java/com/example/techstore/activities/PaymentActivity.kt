package com.example.techstore.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.example.techstore.models.CarritoItem
import com.example.techstore.models.Orden
import com.example.techstore.network.SupabaseCarritoOrdenRepository
import com.example.techstore.network.SupabaseClientProvider
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup
    private lateinit var tvProductos: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvEnvio: TextView
    private lateinit var tvTotal: TextView
    private lateinit var repository: SupabaseCarritoOrdenRepository
    private val items = mutableListOf<CarritoItem>()
    private val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        repository = SupabaseCarritoOrdenRepository()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarPagos)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        radioGroup = findViewById(R.id.radioGroupPagos)
        tvProductos = findViewById(R.id.tvPagoProductos)
        tvSubtotal = findViewById(R.id.tvPagoSubtotal)
        tvEnvio = findViewById(R.id.tvPagoEnvio)
        tvTotal = findViewById(R.id.tvPagoTotal)

        findViewById<Button>(R.id.btnVolverPagos).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnPagarAhora).setOnClickListener { registrarPago() }

        cargarCarrito()
    }

    private fun cargarCarrito() {
        if (SupabaseClientProvider.isConfigured) {
            repository.obtenerCarrito(usuarioId(), { carrito ->
                items.clear()
                items.addAll(carrito)
                mostrarResumen()
            }, {
                cargarCarritoLocal()
            })
        } else {
            cargarCarritoLocal()
        }
    }

    private fun cargarCarritoLocal() {
        items.clear()
        items.addAll(CartActivity.leerCarritoLocal(this))
        mostrarResumen()
    }

    private fun mostrarResumen() {
        tvProductos.text = if (items.isEmpty()) {
            getString(R.string.cart_empty)
        } else {
            items.joinToString("\n") { item ->
                getString(R.string.cart_item_format, item.nombreProducto, item.cantidad) +
                    " - $" + numberFormat.format(item.subtotal)
            }
        }

        val subtotal = subtotal()
        val envio = envio()
        val total = subtotal + envio
        tvSubtotal.text = "${getString(R.string.subtotal)}: $${numberFormat.format(subtotal)}"
        tvEnvio.text = "${getString(R.string.shipping)}: $${numberFormat.format(envio)}"
        tvTotal.text = "${getString(R.string.total)}: $${numberFormat.format(total)}"
    }

    private fun registrarPago() {
        if (items.isEmpty()) {
            Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val metodo = findViewById<RadioButton>(radioGroup.checkedRadioButtonId).text.toString()
        val total = subtotal() + envio()

        if (SupabaseClientProvider.isConfigured) {
            val orden = Orden(
                usuarioId = usuarioId(),
                clienteNombre = nombreUsuario(),
                estado = "pendiente",
                total = total,
                productos = items.toList()
            )
            repository.crearOrden(orden, {
                items.forEach { item ->
                    if (item.id.isNotBlank()) {
                        repository.eliminarDelCarrito(item.id, {}, {})
                    }
                }
                Toast.makeText(this, "${getString(R.string.order_created)}. $metodo", Toast.LENGTH_LONG).show()
                CartActivity.open(this)
                finish()
            }, { error ->
                Toast.makeText(this, "Error Supabase: ${error.message}", Toast.LENGTH_LONG).show()
            })
        } else {
            CartActivity.guardarCarritoLocal(this, emptyList())
            Toast.makeText(this, "${getString(R.string.order_created)}. $metodo", Toast.LENGTH_LONG).show()
            CartActivity.open(this)
            finish()
        }
    }

    private fun subtotal(): Double = items.sumOf { it.subtotal }

    private fun envio(): Double = if (subtotal() > 0.0) 50000.0 else 0.0

    private fun usuarioId(): String {
        return getSharedPreferences("sesion", MODE_PRIVATE).getString("email", null).orEmpty().ifBlank { "local_user" }
    }

    private fun nombreUsuario(): String {
        return getSharedPreferences("sesion", MODE_PRIVATE).getString("nombre", null).orEmpty().ifBlank { "Cliente TechStore" }
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, PaymentActivity::class.java))
        }
    }
}
