package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eshop.R
import com.example.eshop.database.ProductoDAO
import com.example.eshop.models.Producto
import com.google.android.material.appbar.MaterialToolbar

class HomeUserActivity : AppCompatActivity() {

    private lateinit var listViewProductosUser: ListView
    private lateinit var productoDAO: ProductoDAO
    private lateinit var productos: MutableList<Producto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        listViewProductosUser = findViewById(R.id.listViewProductosUser)
        productoDAO = ProductoDAO(this)

        cargarProductos()
    }

    private fun cargarProductos() {
        productos = productoDAO.obtenerTodos().toMutableList()
        val adapter = ProductoAdapter(this, productos)
        listViewProductosUser.adapter = adapter

        // Nada de clicks para editar/eliminar aquí
        // Es solo lectura para el usuario normal
    }

    // ===== MENÚ =====

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)

        // Ocultamos opciones de admin
        val itemUsuarios = menu.findItem(R.id.menuUsuarios)
        val itemProductos = menu.findItem(R.id.menuProductos)

        itemUsuarios.isVisible = false
        itemProductos.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                // Ya está en inicio (HomeUserActivity)
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menuCerrarSesion -> {
                cerrarSesion()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
