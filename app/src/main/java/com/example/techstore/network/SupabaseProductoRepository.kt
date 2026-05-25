package com.example.techstore.network

import com.example.techstore.models.Producto
import io.github.jan.supabase.postgrest.from

class SupabaseProductoRepository {
    suspend fun obtenerProductos(): List<Producto> {
        return SupabaseClientProvider.client
            .from("productos")
            .select()
            .decodeList<SupabaseProducto>()
            .map { producto -> producto.toProducto() }
    }
}
