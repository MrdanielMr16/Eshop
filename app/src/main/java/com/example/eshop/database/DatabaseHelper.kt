package com.example.eshop.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION ) {
    companion object{
        private const val DATABASE_NAME = "UsuariosDB"
        private const val DATABASE_VERSION = 1

        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_Nombre = "nombre"
        const val COLUMN_Email = "email"
        const val COLUMN_Password = "password"
        const val COLUMN_Rol = "rol"
        const val TABLE_PRODUCTOS = "productos"
        const val COLUMN_Prod_ID = "id"
        const val COLUMN_Prod_Nombre = "nombre"
        const val COLUMN_Prod_Descripcion = "descripcion"
        const val COLUMN_Prod_Precio = "precio"
        const val COLUMN_Prod_Stock = "stock"


        private const val CREATE_TALBE = """
            CREATE TABLE $TABLE_USUARIOS(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_Nombre TEXT NOT NULL,
                $COLUMN_Email TEXT NOT NULL UNIQUE,
                $COLUMN_Password TEXT NOT NULL,
                $COLUMN_Rol TEXT Not NULL
            )"""

        private val SQL_CREATE_PRODUCTOS = """
            CREATE TABLE $TABLE_PRODUCTOS (
                $COLUMN_Prod_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_Prod_Nombre TEXT NOT NULL,
                $COLUMN_Prod_Descripcion TEXT,
                $COLUMN_Prod_Precio REAL NOT NULL,
                $COLUMN_Prod_Stock INTEGER NOT NULL
            )""".trimIndent()


    }

    //Se ejecute una sola vez que se crea la DB
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TALBE)
        db.execSQL(SQL_CREATE_PRODUCTOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }
}