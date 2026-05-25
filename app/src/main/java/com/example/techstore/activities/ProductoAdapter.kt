package com.example.techstore.activities  // o .adapters, según tu estructura

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.techstore.R
import com.example.techstore.models.Producto
import java.net.URL
import java.util.concurrent.Executors

class ProductoAdapter(
    context: Context,
    private val productos: List<Producto>
) : ArrayAdapter<Producto>(context, 0, productos) {
    private val imageExecutor = Executors.newFixedThreadPool(3)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_producto, parent, false)
        }

        val producto = productos[position]

        val imgProducto = itemView!!.findViewById<ImageView>(R.id.imgProducto)
        val tvNombre = itemView!!.findViewById<TextView>(R.id.tvNombreProducto)
        val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecioProducto)
        val tvStock = itemView.findViewById<TextView>(R.id.tvStockProducto)
        val tvDescripcion = itemView.findViewById<TextView>(R.id.tvDescripcionProducto)

        cargarImagen(producto.imagenUrl, imgProducto)
        tvNombre.text = producto.nombre
        tvPrecio.text = "Precio: $${producto.precio}"
        tvStock.text = "Stock: ${producto.stock}"
        tvDescripcion.text = producto.descripcion

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
