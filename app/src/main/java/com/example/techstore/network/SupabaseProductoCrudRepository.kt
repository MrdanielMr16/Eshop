package com.example.techstore.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.techstore.models.Producto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.util.UUID

data class SupabaseProductoRecord(
    val rowId: Int,
    val producto: Producto
)

@Serializable
private data class SupabaseProductoWriteDto(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    @SerialName("imagen_url")
    val imagenUrl: String = ""
)

class SupabaseProductoCrudRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val table get() = SupabaseClientProvider.client.from("productos")
    private val bucketName = "productos"

    fun obtenerTodos(onSuccess: (List<SupabaseProductoRecord>) -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            val productos = table.select().decodeList<SupabaseProducto>()
                .map { dto -> SupabaseProductoRecord(dto.id ?: 0, dto.toProducto()) }
            onSuccess(productos)
        }
    }

    fun crear(
        producto: Producto,
        imagenUri: Uri?,
        imagenBitmap: Bitmap?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        runSupabase(onError) {
            val imagenUrl = subirImagenSiExiste(imagenUri, imagenBitmap)
            table.insert(producto.copy(imagenUrl = imagenUrl).toWriteDto())
            onSuccess()
        }
    }

    fun actualizar(
        rowId: Int,
        producto: Producto,
        imagenUri: Uri?,
        imagenBitmap: Bitmap?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        runSupabase(onError) {
            val imagenUrl = subirImagenSiExiste(imagenUri, imagenBitmap)
            val actualizado = if (imagenUrl.isBlank()) producto else producto.copy(imagenUrl = imagenUrl)
            table.update(actualizado.toWriteDto()) {
                filter {
                    filter("id", FilterOperator.EQ, rowId)
                }
            }
            onSuccess()
        }
    }

    fun eliminar(rowId: Int, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            table.delete {
                filter {
                    filter("id", FilterOperator.EQ, rowId)
                }
            }
            onSuccess()
        }
    }

    private suspend fun subirImagenSiExiste(imagenUri: Uri?, imagenBitmap: Bitmap?): String {
        val bytes = when {
            imagenUri != null -> context.contentResolver.openInputStream(imagenUri)?.use { input -> input.readBytes() }
            imagenBitmap != null -> ByteArrayOutputStream().use { output ->
                imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                output.toByteArray()
            }
            else -> null
        } ?: return ""

        val path = "productos/${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClientProvider.client.storage.from(bucketName)
        bucket.upload(path, bytes) {
            upsert = false
        }
        return bucket.publicUrl(path)
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

    private fun Producto.toWriteDto(): SupabaseProductoWriteDto {
        return SupabaseProductoWriteDto(
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUrl = imagenUrl
        )
    }
}
