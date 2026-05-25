package com.example.techstore.models

data class Usuarios(
    var id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
)
