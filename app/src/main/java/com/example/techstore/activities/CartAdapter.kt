package com.example.techstore.activities

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.techstore.R
import com.example.techstore.models.CarritoItem
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors

class CartAdapter(
    context: Context,
    private val items: List<CarritoItem>,
    private val onDelete: (CarritoItem) -> Unit
) : ArrayAdapter<CarritoItem>(context, 0, items) {
    private val imageExecutor = Executors.newFixedThreadPool(3)
    private val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false)
        val item = items[position]

        val imageView = itemView.findViewById<ImageView>(R.id.imgCarritoProducto)
        itemView.findViewById<TextView>(R.id.tvNombreCarrito).text = context.getString(
            R.string.cart_item_format,
            item.nombreProducto,
            item.cantidad
        )
        itemView.findViewById<TextView>(R.id.tvPrecioCarrito).text = context.getString(
            R.string.price_format,
            numberFormat.format(item.subtotal)
        )
        itemView.findViewById<Button>(R.id.btnEliminarCarrito).setOnClickListener {
            onDelete(item)
        }
        cargarImagen(item.imagenUrl, imageView)
        return itemView
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
}
