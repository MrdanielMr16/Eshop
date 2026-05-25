# TechStore

TechStore es una aplicacion movil Android para una tienda virtual de productos tecnologicos. El proyecto incluye interfaces por rol, autenticacion, CRUDs y conexion con Supabase para persistencia de datos.

## Funcionalidades

- Splash screen y pantallas de autenticacion.
- Login y registro con Supabase.
- Login con biometria del dispositivo.
- Login con Google usando Credential Manager.
- Navegacion por roles: administrador, vendedor y comprador.
- CRUD de usuarios desde el panel administrador.
- CRUD de productos con imagenes desde galeria o camara.
- Gestion de carrito y ordenes.
- Reportes y vistas administrativas.
- Interfaz estilo TechStore con layouts XML, drawables y estilos personalizados.

## Roles

| Rol | Correo sugerido | Acceso |
| --- | --- | --- |
| Administrador | `admin@techstore.com` | Dashboard admin, usuarios, productos y reportes |
| Vendedor | `vendedor@techstore.com` | Panel vendedor, productos, pedidos y perfil |
| Comprador | Cualquier correo registrado | Catalogo, detalle, carrito, pagos y perfil |

La contrasena no esta fija en el codigo. Es la que se registra en la app o directamente en Supabase.

## Configuracion local

Crea o actualiza el archivo `local.properties` con tus credenciales:

```properties
sdk.dir=C:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_PUBLISHABLE_KEY=tu_anon_public_key
GOOGLE_WEB_CLIENT_ID=tu-web-client-id.apps.googleusercontent.com
```

Importante:

- `SUPABASE_URL` debe terminar en `.supabase.co`, sin `/rest/v1`.
- No subas `local.properties` al repositorio.
- Usa la key anon/public de Supabase, no la `service_role`.

## Supabase

El script de base de datos esta en:

```text
supabase/techstore_schema.sql
```

Ejecutalo desde Supabase:

```text
SQL Editor -> New query -> pegar script -> Run
```

Tablas principales:

- `usuarios`
- `productos`
- `carritos`
- `ordenes`

Tambien se configura el bucket de Storage:

- `productos`

## Google Login

El inicio con Google usa Credential Manager. Para configurarlo necesitas:

1. Crear un OAuth Client ID tipo Web application en Google Cloud.
2. Copiar el Client ID en `local.properties` como `GOOGLE_WEB_CLIENT_ID`.
3. Tener una cuenta Google configurada en el dispositivo o emulador con Google Play.

Si aparece `No credentials available`, revisa que el dispositivo tenga una cuenta Google agregada.

## Generar APK

Desde Android Studio:

```text
Build -> Generate App Bundles or APKs -> Generate APKs
```

El APK queda en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Desde terminal PowerShell:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleDebug
```

## Tecnologias

- Kotlin
- Android XML Layouts
- Material Components
- Supabase PostgREST
- Supabase Storage
- AndroidX Biometric
- AndroidX Credential Manager

