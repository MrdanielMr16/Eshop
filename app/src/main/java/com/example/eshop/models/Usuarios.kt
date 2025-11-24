package com.example.eshop.models

data class Usuarios(
    var id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
)
