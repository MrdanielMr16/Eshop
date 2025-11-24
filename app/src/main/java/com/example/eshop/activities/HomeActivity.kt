package com.example.eshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eshop.R

class HomeActivity : AppCompatActivity() {
    private lateinit var btnCrudUsuarios: Button
    private lateinit var btnCrudProductos: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        btnCrudUsuarios = findViewById(R.id.btnCrudUsuarios)
        btnCrudProductos = findViewById(R.id.btnCrudProductos)



        btnCrudProductos.setOnClickListener {
            // Ir a la pantalla del CRUD de productos
            val intent = Intent(this, AdminProductosActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)

        // Leer rol por si lo necesitas (en admin igual será "admin")
        val prefs = getSharedPreferences("sesion", MODE_PRIVATE)
        val rol = prefs.getString("rol", "usuario")

        // Si quisieras ocultar algo dependiendo del rol, lo puedes hacer aquí
        // val itemUsuarios = menu.findItem(R.id.menuUsuarios)
        // itemUsuarios.isVisible = rol == "admin"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuInicio -> {
                // Ya estás en HomeActivity, no hace falta hacer nada especial
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menuUsuarios -> {
                // TODO: crea esta activity para CRUD de usuarios
                //val intent = Intent(this, AdminUsuariosActivity::class.java)
                //startActivity(intent)
                //return true
            }
            R.id.menuProductos -> {
                val intent = Intent(this, AdminProductosActivity::class.java)
                startActivity(intent)
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