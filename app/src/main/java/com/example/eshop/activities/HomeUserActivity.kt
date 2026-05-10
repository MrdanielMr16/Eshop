package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eshop.R
import com.example.eshop.database.ProductoDAO
import com.example.eshop.models.Producto
import com.google.android.material.appbar.MaterialToolbar

class HomeUserActivity : AppCompatActivity() {

    private lateinit var listViewProductosUser: ListView
    private lateinit var btnCarritoComprador: Button
    private lateinit var btnPerfilUsuario: Button
    private lateinit var btnVolverComprador: Button
    private lateinit var btnCerrarSesionComprador: Button
    private lateinit var productoDAO: ProductoDAO
    private lateinit var productos: MutableList<Producto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        listViewProductosUser = findViewById(R.id.listViewProductosUser)
        btnCarritoComprador = findViewById(R.id.btnCarritoComprador)
        btnPerfilUsuario = findViewById(R.id.btnPerfilUsuario)
        btnVolverComprador = findViewById(R.id.btnVolverComprador)
        btnCerrarSesionComprador = findViewById(R.id.btnCerrarSesionComprador)
        productoDAO = ProductoDAO(this)

        btnCarritoComprador.setOnClickListener {
            StaticScreenActivity.open(this, StaticScreenActivity.BUYER_CART)
        }

        btnPerfilUsuario.setOnClickListener {
            StaticScreenActivity.open(this, StaticScreenActivity.BUYER_PROFILE)
        }

        btnVolverComprador.setOnClickListener {
            finish()
        }

        btnCerrarSesionComprador.setOnClickListener {
            cerrarSesion()
        }

        cargarProductos()
    }

    private fun cargarProductos() {
        productos = productoDAO.obtenerTodos().toMutableList()
        val adapter = ProductoAdapter(this, productos)
        listViewProductosUser.adapter = adapter
        listViewProductosUser.setOnItemClickListener { _, _, _, _ ->
            StaticScreenActivity.open(this, StaticScreenActivity.BUYER_DETAIL)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        menu.findItem(R.id.menuUsuarios).isVisible = false
        menu.findItem(R.id.menuProductos).isVisible = false
        menu.findItem(R.id.menuReportes).isVisible = false
        menu.findItem(R.id.menuPedidosVendedor).isVisible = false
        menu.findItem(R.id.menuPerfilVendedor).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio, R.id.menuCatalogo -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BUYER_CATALOG)
                return true
            }
            R.id.menuCarrito -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BUYER_CART)
                return true
            }
            R.id.menuPagos -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BUYER_PAYMENT)
                return true
            }
            R.id.menuPerfilComprador -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BUYER_PROFILE)
                return true
            }
            R.id.menuBiometria -> {
                StaticScreenActivity.open(this, StaticScreenActivity.BIOMETRIC_AUTH)
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
