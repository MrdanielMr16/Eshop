package com.example.techstore.models

data class Orden(
    val id: String = "",
    val usuarioId: String = "",
    val clienteNombre: String = "",
    val estado: String = "pendiente",
    val total: Double = 0.0,
    val productos: List<CarritoItem> = emptyList(),
    val fecha: Long = System.currentTimeMillis()
)
