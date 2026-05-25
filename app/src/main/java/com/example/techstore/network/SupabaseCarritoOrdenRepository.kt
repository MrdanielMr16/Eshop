package com.example.techstore.network

import com.example.techstore.models.CarritoItem
import com.example.techstore.models.Orden
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SupabaseCarritoItemDto(
    val id: Int? = null,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("producto_id")
    val productoId: String,
    @SerialName("nombre_producto")
    val nombreProducto: String,
    val cantidad: Int,
    @SerialName("precio_unitario")
    val precioUnitario: Double,
    @SerialName("imagen_url")
    val imagenUrl: String = ""
) {
    fun toCarritoItem(): CarritoItem {
        return CarritoItem(
            id = id?.toString().orEmpty(),
            usuarioId = usuarioId,
            productoId = productoId,
            nombreProducto = nombreProducto,
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            imagenUrl = imagenUrl
        )
    }
}

@Serializable
private data class SupabaseOrdenDto(
    val id: Int? = null,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("cliente_nombre")
    val clienteNombre: String,
    val estado: String,
    val total: Double,
    val fecha: Long
) {
    fun toOrden(): Orden {
        return Orden(
            id = id?.toString().orEmpty(),
            usuarioId = usuarioId,
            clienteNombre = clienteNombre,
            estado = estado,
            total = total,
            fecha = fecha
        )
    }
}

class SupabaseCarritoOrdenRepository {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val carritoTable get() = SupabaseClientProvider.client.from("carritos")
    private val ordenesTable get() = SupabaseClientProvider.client.from("ordenes")

    fun agregarAlCarrito(item: CarritoItem, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            carritoTable.insert(item.toDto())
            onSuccess()
        }
    }

    fun obtenerCarrito(usuarioId: String, onSuccess: (List<CarritoItem>) -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            val items = carritoTable.select {
                filter {
                    filter("usuario_id", FilterOperator.EQ, usuarioId)
                }
            }.decodeList<SupabaseCarritoItemDto>().map { it.toCarritoItem() }
            onSuccess(items)
        }
    }

    fun actualizarCantidad(itemId: String, cantidad: Int, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            carritoTable.update({ set("cantidad", cantidad) }) {
                filter {
                    filter("id", FilterOperator.EQ, itemId.toInt())
                }
            }
            onSuccess()
        }
    }

    fun eliminarDelCarrito(itemId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            carritoTable.delete {
                filter {
                    filter("id", FilterOperator.EQ, itemId.toInt())
                }
            }
            onSuccess()
        }
    }

    fun crearOrden(orden: Orden, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            ordenesTable.insert(orden.toDto())
            onSuccess()
        }
    }

    fun obtenerOrdenes(onSuccess: (List<Orden>) -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            onSuccess(ordenesTable.select().decodeList<SupabaseOrdenDto>().map { it.toOrden() })
        }
    }

    fun actualizarEstadoOrden(ordenId: String, estado: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            ordenesTable.update({ set("estado", estado) }) {
                filter {
                    filter("id", FilterOperator.EQ, ordenId.toInt())
                }
            }
            onSuccess()
        }
    }

    fun eliminarOrden(ordenId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            ordenesTable.delete {
                filter {
                    filter("id", FilterOperator.EQ, ordenId.toInt())
                }
            }
            onSuccess()
        }
    }

    private fun runSupabase(onError: (Exception) -> Unit, block: suspend () -> Unit) {
        scope.launch {
            try {
                if (!SupabaseClientProvider.isConfigured) {
                    throw IllegalStateException("Configura SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en local.properties")
                }
                block()
            } catch (error: Exception) {
                onError(error)
            }
        }
    }

    private fun CarritoItem.toDto(): SupabaseCarritoItemDto {
        return SupabaseCarritoItemDto(
            usuarioId = usuarioId,
            productoId = productoId,
            nombreProducto = nombreProducto,
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            imagenUrl = imagenUrl
        )
    }

    private fun Orden.toDto(): SupabaseOrdenDto {
        return SupabaseOrdenDto(
            usuarioId = usuarioId,
            clienteNombre = clienteNombre,
            estado = estado,
            total = total,
            fecha = fecha
        )
    }
}
