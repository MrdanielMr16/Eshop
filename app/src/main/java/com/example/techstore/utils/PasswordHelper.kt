package com.example.techstore.utils

import java.security.MessageDigest

object PasswordHelper {
    fun hashPassword(password: String): String{
        //Convertir un string a bytes
        val bytes = password.toByteArray()
        val md= MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold(""){str, it -> str + "%02x".format(it)}
    }

    fun hashPasswordIfNeeded(password: String): String {
        return if (isHashedPassword(password)) password else hashPassword(password)
    }

    fun verifyPassword(password: String, passwordHashed: String): Boolean{
        return hashPassword(password) == passwordHashed || password == passwordHashed
    }

    private fun isHashedPassword(password: String): Boolean {
        return password.length == 64 && password.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }
}
