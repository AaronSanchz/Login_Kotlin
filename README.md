# Fake Store Roles - Kotlin / Android

Proyecto escolar basado en las historias de usuario US01 y US02.

## Lo que cumple
- Verifica Internet antes de llamar a `/auth/login`.
- Autentica con `POST https://fakestoreapi.com/auth/login`.
- Después obtiene los usuarios con `GET /users`, busca el username autenticado y toma su ID.
- Mapea roles por ID: 1 y 2 Administrador, 3 Auditor, restantes Cliente.
- Guarda token, ID, username y rol en `EncryptedSharedPreferences`.
- Restaura una sesión guardada al volver a abrir la app.
- Cierra sesión borrando el almacenamiento de sesión y el carrito local.
- Usa `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` para impedir regresar a pantallas protegidas después del logout.

## Usuarios de prueba
- Administrador (ID 2): `mor_2314` / `83r5^_`
- Auditor (ID 3): `kevinryan` / `kev02937@`
- Cliente (ID 4): `donero` / `ewedon`

> `johnd` tiene ID 1, así que según US01 también corresponde a Administrador.

## Cómo ejecutar
1. Abre la carpeta en Android Studio.
2. Selecciona JDK 17 para Gradle.
3. Sincroniza el proyecto.
4. Ejecuta en un emulador o teléfono Android.

## Compatibilidad
- minSdk 23
- targetSdk 35
- Java/Kotlin JVM 17
- Gradle 8.9
