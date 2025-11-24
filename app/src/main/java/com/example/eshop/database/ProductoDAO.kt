package com.example.eshop.database

import android.content.ContentValues
import android.content.Context
import com.example.eshop.models.Producto

class ProductoDAO(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun insertarProducto(producto: Producto): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_Prod_Nombre, producto.nombre)
            put(DatabaseHelper.COLUMN_Prod_Descripcion, producto.descripcion)
            put(DatabaseHelper.COLUMN_Prod_Precio, producto.precio)
            put(DatabaseHelper.COLUMN_Prod_Stock, producto.stock)
        }

        val res = db.insert(DatabaseHelper.TABLE_PRODUCTOS, null, values)
        db.close()
        return res != -1L
    }

    fun actualizarProducto(producto: Producto): Boolean {
        if (producto.id == null) return false

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_Prod_Nombre, producto.nombre)
            put(DatabaseHelper.COLUMN_Prod_Descripcion, producto.descripcion)
            put(DatabaseHelper.COLUMN_Prod_Precio, producto.precio)
            put(DatabaseHelper.COLUMN_Prod_Stock, producto.stock)
        }

        val filas = db.update(
            DatabaseHelper.TABLE_PRODUCTOS,
            values,
            "${DatabaseHelper.COLUMN_Prod_ID} = ?",
            arrayOf(producto.id.toString())
        )

        db.close()
        return filas > 0
    }

    fun eliminarProducto(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val filas = db.delete(
            DatabaseHelper.TABLE_PRODUCTOS,
            "${DatabaseHelper.COLUMN_Prod_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return filas > 0
    }

    fun obtenerTodos(): List<Producto> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Producto>()

        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTOS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Prod_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Prod_Nombre))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Prod_Descripcion))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Prod_Precio))
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_Prod_Stock))

                lista.add(
                    Producto(
                        id,
                        nombre,
                        descripcion,
                        precio,
                        stock
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }
}
