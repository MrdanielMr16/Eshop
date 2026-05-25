package com.example.techstore.models

data class CarritoItem(
    val id: String = "",
    val usuarioId: String = "",
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val imagenUrl: String = ""
) {
    val subtotal: Double
        get() = cantidad * precioUnitario
}
