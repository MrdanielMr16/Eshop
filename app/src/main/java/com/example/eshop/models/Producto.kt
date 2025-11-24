package com.example.eshop.models

data class Producto(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int
)