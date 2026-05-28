package com.example.techstore.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.techstore.R
import com.google.android.material.appbar.MaterialToolbar

class StaticScreenActivity : AppCompatActivity() {

    private val navy = Color.rgb(38, 56, 83)
    private val text = Color.rgb(32, 36, 40)
    private val muted = Color.rgb(140, 140, 140)
    private val light = Color.rgb(244, 244, 244)
    private val accent = Color.rgb(20, 184, 212)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screen = intent.getStringExtra(EXTRA_SCREEN).orEmpty()
        setContentView(buildScreen(screen))
    }

    private fun buildScreen(screen: String): View {
        val definition = screenDefinition(screen)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val toolbar = MaterialToolbar(this).apply {
            title = definition.title
            setTitleTextColor(Color.WHITE)
            setTitleCentered(true)
            background = rounded(navy, 0f, 0f, 16f, 16f)
            setNavigationIcon(android.R.drawable.ic_media_previous)
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar, LinearLayout.LayoutParams.MATCH_PARENT, dp(88))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(26))
        }

        when (screen) {
            BUYER_CATALOG -> addCatalogContent(content)
            BUYER_PAYMENT -> addPaymentContent(content)
            BUYER_PROFILE -> addProfileContent(content)
            SELLER_DASHBOARD -> addSellerDashboard(content)
            SELLER_PRODUCTS -> addCreateProductContent(content)
            SELLER_ORDERS -> addOrderDetailContent(content)
            ADMIN_REPORTES -> addAdminReportContent(content)
            else -> addGenericContent(content, definition)
        }

        if (definition.actions.isNotEmpty() && screen !in setOf(BUYER_CATALOG, BUYER_PAYMENT, SELLER_DASHBOARD, SELLER_PRODUCTS)) {
            definition.actions.forEach { action ->
                content.addView(primaryButton(action.label, action.target))
            }
        }

        content.addView(secondaryButton("Volver") { finish() })
        content.addView(primaryButton("Cerrar Sesión", LOGOUT))

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        if (screen in setOf(BUYER_CATALOG, BUYER_DETAIL, BUYER_CART, BUYER_PAYMENT, BUYER_PROFILE)) {
            root.addView(bottomNav(), LinearLayout.LayoutParams.MATCH_PARENT, dp(78))
        }

        return root
    }

    private fun addCatalogContent(content: LinearLayout) {
        content.addView(searchRow())
        content.addView(productRow("Tarjeta gráfica\nNVIDIA RTX 4060", "$1.200.000"))
        content.addView(productRow("Procesador Intel\nI7-T2T00K", "$1.480.000"))
        content.addView(productRow("Memoria RAM\nCorsair 16GB", "$299.000"))
        content.addView(primaryButton("Ir al carrito", BUYER_CART))
    }

    private fun addPaymentContent(content: LinearLayout) {
        content.addView(bigHeading("Métodos de pago"))
        listOf("Tarjeta de Crédito / Debito", "PSE", "Nequi", "Daviplata").forEach {
            content.addView(line("○  $it", 18f, text, false))
        }
        content.addView(card("Resumen de compra", listOf("Subtotal                 $2.979.000", "Envío                    $50.000", "Total                    $3.029.000"), gray = true))
        content.addView(primaryButton("Pagar ahora", null))
    }

    private fun addProfileContent(content: LinearLayout) {
        content.gravity = Gravity.CENTER_HORIZONTAL
        content.addView(TextView(this).apply {
            text = "●"
            textSize = 76f
            setTextColor(navy)
            gravity = Gravity.CENTER
        })
        content.addView(line("Pepito Perez", 24f, text, true, Gravity.CENTER))
        content.addView(line("pepitoperez23@gmail.com", 13f, muted, false, Gravity.CENTER))
        listOf("Mis pedidos", "Direcciones", "Metodos de pago", "Configuración", "Cerrar sesión").forEach { item ->
            content.addView(profileOption(item))
        }
    }

    private fun addSellerDashboard(content: LinearLayout) {
        content.setBackgroundColor(light)
        content.addView(line("Resumen de tu tienda", 23f, text, true))
        content.addView(line("Controla ventas, pedidos e inventario desde un solo lugar.", 14f, muted, false))
        content.addView(sellerMetricStrip())
        content.addView(sellerChartCard())
        content.addView(sellerOrdersCard())
        content.addView(primaryButton("Gestionar productos", SELLER_PRODUCTS))
        content.addView(primaryButton("Pedidos recibidos", SELLER_ORDERS))
    }

    private fun sellerMetricStrip(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(sellerMiniMetric("Vendido", "$1.220.000", accent), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, dp(14), dp(6), dp(14))
            })
            addView(sellerMiniMetric("Pedidos", "12", navy), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(6), dp(14), 0, dp(14))
            })
        }
    }

    private fun sellerMiniMetric(title: String, value: String, markerColor: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 12f)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            addView(TextView(this@StaticScreenActivity).apply {
                text = " "
                background = rounded(markerColor, 20f)
            }, LinearLayout.LayoutParams(dp(30), dp(30)))
            addView(line(title, 14f, muted, false))
            addView(line(value, 22f, text, true))
        }
    }

    private fun sellerChartCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 12f)
            setPadding(dp(18), dp(16), dp(18), dp(16))
            addView(line("Ventas de la semana", 21f, text, true))
            addView(line("Tendencia positiva frente al inicio de semana", 13f, muted, false))
            addView(LinearLayout(this@StaticScreenActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM
                listOf(42, 62, 54, 82, 116).forEachIndexed { index, height ->
                    addView(TextView(this@StaticScreenActivity).apply {
                        text = listOf("L", "M", "M", "J", "V")[index]
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        setTextColor(Color.WHITE)
                        textSize = 12f
                        background = rounded(if (index == 4) accent else navy, 8f)
                    }, LinearLayout.LayoutParams(0, dp(height), 1f).apply {
                        setMargins(dp(4), dp(18), dp(4), 0)
                    })
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(138)))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, dp(16))
            }
        }
    }

    private fun sellerOrdersCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 12f)
            setPadding(dp(18), dp(16), dp(18), dp(16))
            addView(line("Pedidos recientes", 21f, text, true))
            addView(sellerOrderRow("Pedido a125", "Pendiente", Color.rgb(197, 145, 30)))
            addView(sellerOrderRow("Pedido a126", "Enviado", Color.rgb(46, 157, 105)))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, dp(16))
            }
        }
    }

    private fun sellerOrderRow(order: String, status: String, statusColor: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(4))
            addView(line(order, 15f, text, false), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(TextView(this@StaticScreenActivity).apply {
                text = status
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(statusColor)
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(244, 244, 244), 18f)
                setPadding(dp(12), dp(6), dp(12), dp(6))
            })
        }
    }

    private fun addCreateProductContent(content: LinearLayout) {
        content.setBackgroundColor(light)
        content.addView(inputCard("Nombre del producto", false))
        content.addView(inputCard("Descripción", true))
        content.addView(card("Selecciona categoría                         ⌄", emptyList()))
        content.addView(card("Precio", emptyList()))
        content.addView(primaryButton("Ver todos los productos", PRODUCT_CRUD))
    }

    private fun addOrderDetailContent(content: LinearLayout) {
        content.setBackgroundColor(light)
        content.addView(card("Pedido #126                         PENDIENTE", listOf("24/06/2026          10:20 AM")))
        content.addView(card("Cliente", listOf("Pepito perez", "pepitoperez23@gmail.com", "300 567 3492")))
        content.addView(card("Productos", listOf("RTX 4060", "$1.930.000")))
        content.addView(card("TOTAL                         $1.930.000", emptyList(), gray = true))
        content.addView(primaryButton("Marcar como enviado", null))
    }

    private fun addAdminReportContent(content: LinearLayout) {
        content.addView(card("Usuarios        123", listOf("Ventas Anuales        24.673.232")))
        content.addView(chartCard("Ventas Mensuales", listOf("Enero", "Febrero", "Marzo")))
        content.addView(card("Actividad Reciente", listOf("Nuevo usuario registrado      5 min", "Nuevo usuario registrado      1 hr"), gray = true))
    }

    private fun addGenericContent(content: LinearLayout, definition: ScreenDefinition) {
        content.addView(bigHeading(definition.title))
        content.addView(line(definition.subtitle, 16f, muted, false))
        definition.sections.forEach { section ->
            content.addView(card(section.first, section.second))
        }
    }

    private fun searchRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(18))
            addView(TextView(this@StaticScreenActivity).apply {
                text = "⌕  Buscar productos"
                setTextColor(muted)
                textSize = 16f
                background = rounded(Color.rgb(240, 240, 240), 8f)
                setPadding(dp(16), 0, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
            }, LinearLayout.LayoutParams(0, dp(46), 1f))
            addView(TextView(this@StaticScreenActivity).apply {
                text = "▽"
                setTextColor(Color.BLACK)
                textSize = 32f
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(dp(64), dp(46)))
        }
    }

    private fun productRow(name: String, price: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(14), 0, dp(14))
            addView(TextView(this@StaticScreenActivity).apply {
                text = "RTX"
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(navy)
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(238, 238, 238), 8f)
            }, LinearLayout.LayoutParams(dp(108), dp(108)))
            addView(LinearLayout(this@StaticScreenActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), 0, 0, 0)
                addView(line(name, 18f, Color.BLACK, true))
                addView(line(price, 17f, Color.rgb(80, 80, 80), false))
                addView(primaryButton("Ver más", BUYER_DETAIL, compact = true))
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun inputCard(label: String, large: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 12f)
            setPadding(dp(18), dp(14), dp(18), dp(18))
            addView(line(label, 18f, text, true))
            addView(TextView(this@StaticScreenActivity).apply {
                background = rounded(Color.rgb(230, 230, 230), 16f)
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, if (large) dp(118) else dp(42)).apply {
                setMargins(0, dp(14), 0, 0)
            })
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, dp(20))
            }
        }
    }

    private fun chartCard(title: String, labels: List<String>): LinearLayout {
        return card(title, listOf("▁▃▂▄▆", labels.joinToString("     ")), gray = true)
    }

    private fun profileOption(label: String): TextView = TextView(this).apply {
        text = "$label                                      ❯"
        textSize = 17f
        setTextColor(Color.rgb(75, 75, 75))
        setPadding(0, dp(18), 0, dp(16))
        background = borderBottom()
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun card(heading: String, body: List<String>, gray: Boolean = false): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(if (gray) Color.rgb(232, 232, 232) else Color.WHITE, 12f)
            setPadding(dp(18), dp(16), dp(18), dp(16))
            addView(line(heading, 22f, text, true))
            body.forEach { addView(line(it, 16f, if (it.contains("$")) Color.rgb(65, 65, 65) else Color.rgb(80, 80, 80), false)) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, dp(16))
            }
        }
    }

    private fun bigHeading(value: String): TextView = line(value, 24f, Color.BLACK, true)

    private fun line(value: String, size: Float, color: Int, bold: Boolean, gravityValue: Int = Gravity.START): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = gravityValue
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(6), 0, dp(6))
        }
    }

    private fun primaryButton(label: String, target: String?, compact: Boolean = false): Button = Button(this).apply {
        text = label
        textSize = if (compact) 11f else 15f
        setTextColor(Color.WHITE)
        background = rounded(navy, 6f)
        isAllCaps = false
        setOnClickListener {
            when (target) {
                PRODUCT_CRUD -> startActivity(Intent(this@StaticScreenActivity, AdminProductosActivity::class.java))
                SELLER_PRODUCTS -> startActivity(Intent(this@StaticScreenActivity, AdminProductosActivity::class.java))
                BUYER_CART -> CartActivity.open(this@StaticScreenActivity)
                BUYER_PAYMENT -> PaymentActivity.open(this@StaticScreenActivity)
                LOGOUT -> cerrarSesion()
                null -> Toast.makeText(this@StaticScreenActivity, "$label listo", Toast.LENGTH_SHORT).show()
                else -> open(this@StaticScreenActivity, target)
            }
        }
        layoutParams = LinearLayout.LayoutParams(
            if (compact) dp(95) else LinearLayout.LayoutParams.MATCH_PARENT,
            if (compact) dp(32) else dp(52)
        ).apply { setMargins(0, dp(8), 0, dp(10)) }
    }

    private fun secondaryButton(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        setTextColor(navy)
        background = rounded(Color.rgb(240, 240, 240), 8f)
        isAllCaps = false
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).apply {
            setMargins(0, dp(8), 0, dp(10))
        }
    }

    private fun bottomNav(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(153, 153, 153), 14f, 14f, 0f, 0f)
        listOf("⌂\nInicio", "◫\nCategorias", "▣\nCarrito", "●\nPerfil").forEach { label ->
            addView(TextView(this@StaticScreenActivity).apply {
                text = label
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
        }
    }

    private fun rounded(color: Int, tl: Float, tr: Float, br: Float, bl: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(dp(tl.toInt()).toFloat(), dp(tl.toInt()).toFloat(), dp(tr.toInt()).toFloat(), dp(tr.toInt()).toFloat(), dp(br.toInt()).toFloat(), dp(br.toInt()).toFloat(), dp(bl.toInt()).toFloat(), dp(bl.toInt()).toFloat())
        }
    }

    private fun borderBottom(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1, Color.rgb(225, 225, 225))
        }
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
            ADMIN_REPORTES -> ScreenDefinition("Panel de admin", "Resumen general de ventas.", emptyList())
            SELLER_DASHBOARD -> ScreenDefinition("Panel del\nvendedor", "Resumen del vendedor.", emptyList(), listOf(ScreenAction("Ver todos los productos", SELLER_PRODUCTS)))
            SELLER_PRODUCTS -> ScreenDefinition("Crear producto", "Formulario de producto.", emptyList(), listOf(ScreenAction("Ver todos los productos", PRODUCT_CRUD)))
            SELLER_ORDERS -> ScreenDefinition("Detalle del\npedido", "Pedido recibido.", emptyList())
            SELLER_PROFILE -> ScreenDefinition("Perfil vendedor", "Datos del vendedor.", listOf("Vendedor" to listOf("Nombre: Tienda TechStore", "Correo: vendedor@techstore.com", "Rol: vendedor")))
            BUYER_CATALOG -> ScreenDefinition("Catálogo", "Productos disponibles.", emptyList())
            BUYER_DETAIL -> ScreenDefinition("Detalle", "Detalle del producto.", listOf("RTX 4060" to listOf("Precio: $1.930.000", "Stock disponible: 12", "Tarjeta grafica de alto rendimiento.")), listOf(ScreenAction("Agregar al carrito", BUYER_CART)))
            BUYER_CART -> ScreenDefinition("Carrito", "Resumen de compra.", listOf("Productos" to listOf("RTX 4060 x1 - $1.930.000", "Memoria RAM x1 - $299.000"), "Resumen" to listOf("Subtotal: $2.229.000", "Envio: $50.000", "Total: $2.279.000")), listOf(ScreenAction("Continuar a pago", BUYER_PAYMENT)))
            BUYER_PAYMENT -> ScreenDefinition("Pagos", "Metodos de pago.", emptyList())
            BUYER_PROFILE -> ScreenDefinition("Mi Perfil", "Perfil de usuario.", emptyList())
            RECOVER_PASSWORD -> ScreenDefinition("Recuperar contraseña", "Restablece el acceso a tu cuenta.", listOf("Pasos" to listOf("Ingresa tu correo", "Valida el codigo enviado", "Crea una nueva contraseña")), listOf(ScreenAction("Enviar codigo", null)))
            BIOMETRIC_AUTH -> ScreenDefinition("Biometría", "Acceso con huella o rostro.", listOf("Seguridad" to listOf("Usa la seguridad del dispositivo", "No se guardan datos biometricos")), listOf(ScreenAction("Activar biometria", null)))
            else -> ScreenDefinition("TechStore", "Navegación principal.", listOf("Módulos" to listOf("Administrador", "Vendedor", "Comprador")))
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
        const val LOGOUT = "logout"

        fun open(context: Context, screen: String) {
            context.startActivity(Intent(context, StaticScreenActivity::class.java).putExtra(EXTRA_SCREEN, screen))
        }
    }
}
