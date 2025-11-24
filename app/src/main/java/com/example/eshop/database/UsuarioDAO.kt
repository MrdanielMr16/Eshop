package com.example.eshop.database

import android.content.ContentValues
import android.content.Context
import com.example.eshop.models.Usuarios
import com.example.eshop.utils.PasswordHelper

class UsuarioDAO(context: Context) {
    //instancia del databaseHelper para acceder a la BD

    private val dbHelper = DatabaseHelper(context)

    fun registrarUsuario(usuarios: Usuarios): Boolean{
        val db = dbHelper.writableDatabase
        val passwordHashed = PasswordHelper.hashPassword(usuarios.password)
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_Nombre, usuarios.nombre)
            put(DatabaseHelper.COLUMN_Email, usuarios.email)
            put(DatabaseHelper.COLUMN_Password, passwordHashed)
            put(DatabaseHelper.COLUMN_Rol, usuarios.rol)

        }
        val resultado = db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
        db.close()
        return resultado != -1L
    }

    fun validarLogin(email: String, password: String): Boolean {
        val usuario = obtenerUsuario(email) ?: return false
        return PasswordHelper.verifyPassword(password, usuario.password)
    }

    fun obtenerUsuario(email: String): Usuarios? {
        val db = dbHelper.readableDatabase
        var usuario: Usuarios? = null

        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COLUMN_Email} = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Nombre))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Email))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Password))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Rol))
            usuario = Usuarios(id, nombre, email, password, rol)
        }

        cursor.close()
        db.close()
        return usuario
    }

    fun validarEmail(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COLUMN_Email} = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }

    fun eliminarUsuario(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val filas = db.delete(
            DatabaseHelper.TABLE_USUARIOS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return filas > 0
    }

    fun actualizarPassword(id: Int, nuevaPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val passwordHashed = PasswordHelper.hashPassword(nuevaPassword)
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_Password, passwordHashed)
        }

        val filas = db.update(
            DatabaseHelper.TABLE_USUARIOS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )

        db.close()
        return filas > 0
    }

    /**
     * Actualizar solo el nombre del usuario
     */
    fun actualizarNombre(id: Int, nuevoNombre: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_Nombre, nuevoNombre)
        }

        val filas = db.update(
            DatabaseHelper.TABLE_USUARIOS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )

        db.close()
        return filas > 0
    }

}