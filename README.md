# FakeStore Kotlin Android

**Versión reestructurada:** consulta [ARQUITECTURA_Y_PRUEBAS.md](ARQUITECTURA_Y_PRUEBAS.md) para la arquitectura y el código actual. La guía HTML incluida documenta la base anterior.

Proyecto completo de continuación de Fake Store para US03, US04 y US05. Empieza abriendo **docs/GUIA_COMPLETA.html** en un navegador; contiene requisitos, arquitectura, explicación por archivo y código completo numerado.


## Abrir y ejecutar Kotlin

Extrae el ZIP y abre la carpeta FakeStore_Kotlin_Android en Android Studio. Espera la sincronización de Gradle, selecciona un emulador o teléfono Android y ejecuta el módulo app.

Configuración: JDK 17 o 21, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, compileSdk/targetSdk 35, minSdk 23. Para la comprobación se usó JDK 21 con destino JVM 17. Instala Android SDK Platform 35 y acepta sus licencias. Android Studio creará local.properties con sdk.dir de tu computadora; alternativamente configura ANDROID_HOME.

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

En macOS/Linux, habilita permiso de ejecución si hace falta y usa ./gradlew en lugar de .\gradlew.bat. Se incluye el wrapper estándar y su JAR; no es necesario instalar Gradle global. La primera sincronización necesita Internet.

El APK de prueba se crea en app/build/outputs/apk/debug/app-debug.apk. La aplicación usa com.example.fakestoreroles.kotlin para poder instalarla junto con la versión Flutter. Conserva el namespace de clases anterior; al cambiar el ID, esta versión Kotlin necesita iniciar sesión nuevamente y no comparte preferencias con una instalación anterior.

Las pruebas locales JUnit usan org.json para la JVM y coroutines-test. Glide gestiona las imágenes y RecyclerView recicla las filas. Las preferencias cifradas heredadas generan avisos de deprecación de security-crypto; se conservan y su futura migración no forma parte de US03–US05.

## Cuentas de demostración del proyecto base

| Perfil | Usuario | Contraseña |
|---|---|---|
| Administrador ID 2 | mor_2314 | 83r5^_ |
| Auditor ID 3 | kevinryan | kev02937@ |
| Cliente ID 4 | donero | ewedon |

Son cuentas públicas de ejemplo del código recibido. Su disponibilidad depende del servicio y no se garantiza si cambia la base remota. No se añade un acceso que omita la autenticación cuando la API no está disponible.

## Documentación

- docs/ARQUITECTURA_Y_REQUISITOS.md
- docs/CODIGO_EXPLICADO.md
- docs/VERIFICACION.md
- docs/CAMBIOS_SOBRE_BASE.md
- docs/historias

Los avisos y límites de Fake Store están descritos en la guía y en las pantallas de gestión.
# Reestructuración POO US03–US05

Consulta [ARQUITECTURA_Y_PRUEBAS.md](ARQUITECTURA_Y_PRUEBAS.md) para la separación de vistas, controladores, modelos, validaciones, red y servicios, junto con los botones, permisos, códigos HTTP y pruebas ampliadas.
