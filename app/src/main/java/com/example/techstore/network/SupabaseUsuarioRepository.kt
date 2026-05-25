package com.example.techstore.network

import com.example.techstore.models.Usuarios
import com.example.techstore.utils.PasswordHelper
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class SupabaseUsuarioRecord(
    val rowId: Int,
    val usuario: Usuarios
)

@Serializable
private data class SupabaseUsuarioDto(
    val id: Int? = null,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    fun toUsuario(): Usuarios {
        return Usuarios(
            id = id ?: 0,
            nombre = nombre,
            email = email,
            password = password,
            rol = rol
        )
    }
}

class SupabaseUsuarioRepository {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val table get() = SupabaseClientProvider.client.from("usuarios")

    fun obtenerTodos(onSuccess: (List<SupabaseUsuarioRecord>) -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            val usuarios = table.select().decodeList<SupabaseUsuarioDto>()
                .map { dto -> SupabaseUsuarioRecord(dto.id ?: 0, dto.toUsuario()) }
            onSuccess(usuarios)
        }
    }

    fun obtenerPorEmail(email: String, onSuccess: (Usuarios?) -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            val usuario = table.select {
                filter {
                    filter("email", FilterOperator.EQ, email.trim().lowercase())
                }
            }.decodeList<SupabaseUsuarioDto>().firstOrNull()?.toUsuario()
            onSuccess(usuario)
        }
    }

    fun validarLogin(email: String, password: String, onSuccess: (Usuarios?) -> Unit, onError: (Exception) -> Unit) {
        obtenerPorEmail(email, { usuario ->
            if (usuario != null && PasswordHelper.verifyPassword(password, usuario.password)) {
                onSuccess(usuario)
            } else {
                onSuccess(null)
            }
        }, onError)
    }

    fun crear(usuario: Usuarios, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            table.insert(usuario.toDto())
            onSuccess()
        }
    }

    fun actualizar(rowId: Int, nombre: String, rol: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runSupabase(onError) {
            table.update(
                {
                    set("nombre", nombre)
                    set("rol", rol)
                }
            ) {
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

    private fun Usuarios.toDto(): SupabaseUsuarioDto {
        return SupabaseUsuarioDto(
            nombre = nombre,
            email = email.trim().lowercase(),
            password = PasswordHelper.hashPasswordIfNeeded(password),
            rol = rol
        )
    }
}
