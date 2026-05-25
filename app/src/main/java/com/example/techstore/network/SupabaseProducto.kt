package com.example.techstore.network

import com.example.techstore.models.Producto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProducto(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    val stock: Int,
    @SerialName("imagen_url")
    val imagenUrl: String = "",
    @SerialName("created_at")
    val createdAt: String? = null
) {
    fun toProducto(): Producto {
        return Producto(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUrl = imagenUrl
        )
    }
}
