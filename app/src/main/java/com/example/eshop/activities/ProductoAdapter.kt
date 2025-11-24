package com.example.eshop.activities  // o .adapters, según tu estructura

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.eshop.R
import com.example.eshop.models.Producto

class ProductoAdapter(
    context: Context,
    private val productos: List<Producto>
) : ArrayAdapter<Producto>(context, 0, productos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_producto, parent, false)
        }

        val producto = productos[position]

        val tvNombre = itemView!!.findViewById<TextView>(R.id.tvNombreProducto)
        val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecioProducto)
        val tvStock = itemView.findViewById<TextView>(R.id.tvStockProducto)
        val tvDescripcion = itemView.findViewById<TextView>(R.id.tvDescripcionProducto)

        tvNombre.text = producto.nombre
        tvPrecio.text = "Precio: $${producto.precio}"
        tvStock.text = "Stock: ${producto.stock}"
        tvDescripcion.text = producto.descripcion

        return itemView
    }
}
