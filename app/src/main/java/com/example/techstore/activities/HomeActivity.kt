package com.example.techstore.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity() {
    private lateinit var btnCrudUsuarios: Button
    private lateinit var btnCrudProductos: Button
    private lateinit var btnReporteVentas: Button
    private lateinit var btnVolverAdmin: Button
    private lateinit var btnCerrarSesionAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        btnCrudUsuarios = findViewById(R.id.btnCrudUsuarios)
        btnCrudProductos = findViewById(R.id.btnCrudProductos)
        btnReporteVentas = findViewById(R.id.btnReporteVentas)
        btnVolverAdmin = findViewById(R.id.btnVolverAdmin)
        btnCerrarSesionAdmin = findViewById(R.id.btnCerrarSesionAdmin)

        btnCrudUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }

        btnCrudProductos.setOnClickListener {
            startActivity(Intent(this, AdminProductosActivity::class.java))
        }

        btnReporteVentas.setOnClickListener {
            StaticScreenActivity.open(this, StaticScreenActivity.ADMIN_REPORTES)
        }

        btnVolverAdmin.setOnClickListener {
            finish()
        }

        btnCerrarSesionAdmin.setOnClickListener {
            cerrarSesion()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        menu.findItem(R.id.menuCatalogo).isVisible = false
        menu.findItem(R.id.menuCarrito).isVisible = false
        menu.findItem(R.id.menuPagos).isVisible = false
        menu.findItem(R.id.menuPerfilComprador).isVisible = false
        menu.findItem(R.id.menuPedidosVendedor).isVisible = false
        menu.findItem(R.id.menuPerfilVendedor).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                Toast.makeText(this, "Ya estas en Inicio", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menuUsuarios -> {
                startActivity(Intent(this, AdminUsuariosActivity::class.java))
                return true
            }
            R.id.menuProductos -> {
                startActivity(Intent(this, AdminProductosActivity::class.java))
                return true
            }
            R.id.menuReportes -> {
                StaticScreenActivity.open(this, StaticScreenActivity.ADMIN_REPORTES)
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
