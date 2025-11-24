package com.example.eshop.adapters  // o .activities, como prefieras

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.eshop.R
import com.example.eshop.models.Usuarios

class UsuarioAdapter(
    context: Context,
    private val usuarios: List<Usuarios>
) : ArrayAdapter<Usuarios>(context, 0, usuarios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_usuario, parent, false)
        }

        val usuario = usuarios[position]

        val tvNombre = itemView!!.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvEmail = itemView.findViewById<TextView>(R.id.tvEmailUsuario)
        val tvRol = itemView.findViewById<TextView>(R.id.tvRolUsuario)

        tvNombre.text = usuario.nombre
        tvEmail.text = usuario.email
        tvRol.text = "Rol: ${usuario.rol}"

        return itemView
    }
}
