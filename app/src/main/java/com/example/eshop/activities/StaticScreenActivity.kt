package com.example.eshop.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eshop.R
import com.google.android.material.appbar.MaterialToolbar

class StaticScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screen = intent.getStringExtra(EXTRA_SCREEN).orEmpty()
        setContentView(buildScreen(screen))
    }

    private fun buildScreen(screen: String): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.background_gradient)
        }

        val definition = screenDefinition(screen)
        val toolbar = MaterialToolbar(this).apply {
            title = definition.title
            setTitleTextColor(Color.WHITE)
            setBackgroundColor(getColor(R.color.green))
            setNavigationIcon(android.R.drawable.ic_menu_revert)
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(56))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
        }

        content.addView(title(definition.title))
        content.addView(subtitle(definition.subtitle))

        definition.sections.forEach { section ->
            content.addView(card(section.first, section.second))
        }

        if (definition.actions.isNotEmpty()) {
            content.addView(sectionLabel("Acciones"))
            definition.actions.forEach { action ->
                content.addView(actionButton(action.label, action.target))
            }
        }

        content.addView(sectionLabel("Navegacion"))
        content.addView(backButton())
        content.addView(logoutButton())

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        return root
    }

    private fun title(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 27f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(0, 0, 0, dp(8))
    }

    private fun subtitle(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        alpha = 0.9f
        textSize = 16f
        setPadding(0, 0, 0, dp(18))
    }

    private fun sectionLabel(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 18f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setPadding(0, dp(12), 0, dp(8))
    }

    private fun card(heading: String, body: List<String>): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(Color.argb(230, 245, 245, 245))
        }
        card.addView(TextView(this).apply {
            text = heading
            setTextColor(Color.rgb(25, 25, 25))
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        body.forEach { item ->
            card.addView(TextView(this).apply {
                text = item
                setTextColor(Color.rgb(45, 45, 45))
                textSize = 15f
                setPadding(0, dp(8), 0, 0)
            })
        }
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, dp(12)) }
        return card
    }

    private fun actionButton(label: String, target: String?): Button = Button(this).apply {
        text = label
        setTextColor(Color.BLACK)
        setBackgroundColor(getColor(R.color.green))
        setOnClickListener {
            when (target) {
                PRODUCT_CRUD -> startActivity(Intent(this@StaticScreenActivity, AdminProductosActivity::class.java))
                null -> Toast.makeText(this@StaticScreenActivity, "$label listo para implementar", Toast.LENGTH_SHORT).show()
                else -> open(this@StaticScreenActivity, target)
            }
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply { setMargins(0, 0, 0, dp(10)) }
    }

    private fun backButton(): Button = Button(this).apply {
        text = "Volver"
        setTextColor(Color.BLACK)
        setBackgroundColor(Color.LTGRAY)
        setOnClickListener { finish() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply { setMargins(0, 0, 0, dp(10)) }
    }

    private fun logoutButton(): Button = Button(this).apply {
        text = "Cerrar sesion"
        setTextColor(Color.BLACK)
        setBackgroundColor(getColor(R.color.green))
        setOnClickListener { cerrarSesion() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply { setMargins(0, 0, 0, dp(10)) }
    }

    private fun cerrarSesion() {
        getSharedPreferences("sesion", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun screenDefinition(screen: String): ScreenDefinition {
        return when (screen) {
            ADMIN_REPORTES -> ScreenDefinition(
                "Reporte de ventas",
                "Resumen financiero para administrador.",
                listOf(
                    "Indicadores del mes" to listOf("Ventas totales: $ 15.230.000", "Pedidos completados: 128", "Ticket promedio: $ 119.000"),
                    "Productos destacados" to listOf("Audífonos Bluetooth: 38 unidades", "Smartwatch Pro: 24 unidades", "Cargador rápido: 19 unidades"),
                    "Actividad reciente" to listOf("Nuevo pedido #ES-1024", "Producto actualizado: Cámara WiFi", "Usuario comprador registrado")
                )
            )
            SELLER_DASHBOARD -> ScreenDefinition(
                "Panel vendedor",
                "Control rápido de productos y pedidos recibidos.",
                listOf(
                    "Resumen" to listOf("Productos activos: 24", "Pedidos pendientes: 8", "Ventas de hoy: $ 620.000"),
                    "Tareas" to listOf("Actualizar stock bajo", "Revisar pedidos pendientes", "Responder mensajes de compradores")
                ),
                listOf(
                    ScreenAction("Panel de productos", SELLER_PRODUCTS),
                    ScreenAction("Historial de pedidos", SELLER_ORDERS),
                    ScreenAction("Perfil de vendedor", SELLER_PROFILE)
                )
            )
            SELLER_PRODUCTS -> ScreenDefinition(
                "Panel de productos",
                "CRUD completo para productos del vendedor.",
                listOf(
                    "Listado" to listOf("Audífonos Bluetooth - $ 120.000 - Stock 15", "Smartwatch Pro - $ 280.000 - Stock 7", "Cámara WiFi - $ 190.000 - Stock 4"),
                    "Operaciones CRUD" to listOf("Crear producto", "Editar nombre, precio, descripción y stock", "Eliminar productos descontinuados")
                ),
                listOf(ScreenAction("Abrir CRUD de productos", PRODUCT_CRUD))
            )
            SELLER_ORDERS -> ScreenDefinition(
                "Historial de pedidos",
                "Pedidos recibidos por el vendedor.",
                listOf(
                    "Pendientes" to listOf("#ES-1024 - Juan Pérez - $ 280.000", "#ES-1025 - Laura Gómez - $ 120.000"),
                    "Enviados" to listOf("#ES-1019 - Smartwatch Pro", "#ES-1016 - Cámara WiFi"),
                    "Completados" to listOf("#ES-1008 - Audífonos Bluetooth", "#ES-1002 - Cargador rápido")
                )
            )
            SELLER_PROFILE -> ScreenDefinition(
                "Perfil de vendedor",
                "Datos comerciales y configuración de cuenta.",
                listOf(
                    "Vendedor" to listOf("Nombre: Tienda ESHOP", "Correo: vendedor@eshop.com", "Rol: vendedor"),
                    "Tienda" to listOf("Calificación: 4.8", "Productos publicados: 24", "Tiempo de respuesta: 2 horas")
                )
            )
            BUYER_CATALOG -> ScreenDefinition(
                "Catálogo de productos",
                "Explora los productos disponibles.",
                listOf(
                    "Tecnología" to listOf("Audífonos Bluetooth - $ 120.000", "Smartwatch Pro - $ 280.000", "Cámara WiFi - $ 190.000"),
                    "Ofertas" to listOf("Envío gratis desde $ 200.000", "10% de descuento en accesorios")
                ),
                listOf(
                    ScreenAction("Ver detalle de producto", BUYER_DETAIL),
                    ScreenAction("Ir al carrito", BUYER_CART)
                )
            )
            BUYER_DETAIL -> ScreenDefinition(
                "Detalle de producto",
                "Información completa antes de comprar.",
                listOf(
                    "Smartwatch Pro" to listOf("Precio: $ 280.000", "Stock disponible: 7 unidades", "Descripción: monitor de actividad, notificaciones y batería de larga duración"),
                    "Valoraciones" to listOf("4.8 de 5", "Excelente calidad y fácil configuración")
                ),
                listOf(ScreenAction("Agregar al carrito", BUYER_CART))
            )
            BUYER_CART -> ScreenDefinition(
                "Carrito de compras",
                "Productos listos para finalizar compra.",
                listOf(
                    "Productos" to listOf("Smartwatch Pro x1 - $ 280.000", "Audífonos Bluetooth x1 - $ 120.000"),
                    "Resumen" to listOf("Subtotal: $ 400.000", "Envío: $ 12.000", "Total: $ 412.000")
                ),
                listOf(ScreenAction("Continuar a pago", BUYER_PAYMENT))
            )
            BUYER_PAYMENT -> ScreenDefinition(
                "Pasarela de pagos",
                "Selecciona un método de pago seguro.",
                listOf(
                    "Métodos" to listOf("Tarjeta débito/crédito", "PSE", "Nequi", "Pago contra entrega"),
                    "Total a pagar" to listOf("$ 412.000", "Compra protegida por ESHOP")
                ),
                listOf(ScreenAction("Confirmar pago", null))
            )
            BUYER_PROFILE -> ScreenDefinition(
                "Perfil de usuario",
                "Datos personales y preferencias del comprador.",
                listOf(
                    "Cuenta" to listOf("Nombre: Comprador ESHOP", "Correo: comprador@eshop.com", "Rol: comprador"),
                    "Opciones" to listOf("Mis pedidos", "Direcciones", "Métodos de pago", "Favoritos")
                )
            )
            RECOVER_PASSWORD -> ScreenDefinition(
                "Recuperar contraseña",
                "Flujo transversal para restablecer acceso.",
                listOf(
                    "Paso 1" to listOf("Ingresar correo registrado"),
                    "Paso 2" to listOf("Validar identidad con código de seguridad"),
                    "Paso 3" to listOf("Crear nueva contraseña")
                ),
                listOf(ScreenAction("Enviar código", null))
            )
            BIOMETRIC_AUTH -> ScreenDefinition(
                "Autenticación biométrica",
                "Acceso transversal con huella o rostro.",
                listOf(
                    "Estado" to listOf("Biometría disponible para iniciar sesión más rápido"),
                    "Seguridad" to listOf("La app solicita confirmación del dispositivo", "No almacena datos biométricos dentro de ESHOP")
                ),
                listOf(ScreenAction("Activar biometría", null))
            )
            else -> ScreenDefinition(
                "ESHOP",
                "Pantalla principal de navegación.",
                listOf("Módulos" to listOf("Administrador", "Vendedor", "Comprador"))
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    data class ScreenDefinition(
        val title: String,
        val subtitle: String,
        val sections: List<Pair<String, List<String>>>,
        val actions: List<ScreenAction> = emptyList()
    )

    data class ScreenAction(val label: String, val target: String?)

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val ADMIN_REPORTES = "admin_reportes"
        const val SELLER_DASHBOARD = "seller_dashboard"
        const val SELLER_PRODUCTS = "seller_products"
        const val SELLER_ORDERS = "seller_orders"
        const val SELLER_PROFILE = "seller_profile"
        const val BUYER_CATALOG = "buyer_catalog"
        const val BUYER_DETAIL = "buyer_detail"
        const val BUYER_CART = "buyer_cart"
        const val BUYER_PAYMENT = "buyer_payment"
        const val BUYER_PROFILE = "buyer_profile"
        const val RECOVER_PASSWORD = "recover_password"
        const val BIOMETRIC_AUTH = "biometric_auth"
        const val PRODUCT_CRUD = "product_crud"

        fun open(context: Context, screen: String) {
            context.startActivity(Intent(context, StaticScreenActivity::class.java).putExtra(EXTRA_SCREEN, screen))
        }
    }
}
