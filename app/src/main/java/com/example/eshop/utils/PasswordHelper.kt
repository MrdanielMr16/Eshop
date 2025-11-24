package com.example.eshop.utils

import java.security.MessageDigest

object PasswordHelper {
    fun hashPassword(password: String): String{
        //Convertir un string a bytes
        val bytes = password.toByteArray()
        val md= MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold(""){str, it -> str + "%02x".format(it)}
    }

    fun verifyPassword(password: String, passwordHashed: String): Boolean{
        return hashPassword(password) == passwordHashed
    }
}