package com.example.techstore.models

data class Producto(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String = ""
)
