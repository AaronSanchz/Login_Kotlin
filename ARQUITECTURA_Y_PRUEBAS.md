# Fake Store API: arquitectura Kotlin

## Versiones conservadas

Java 21 para ejecutar Gradle, Gradle 8.9, Android Gradle Plugin 8.7.3 y la configuración Kotlin existente. El módulo conserva `compileSdk = 35`, `minSdk = 23` y su identificador de aplicación. No se cambiaron dependencias ni versiones.

## Organización orientada a objetos

El código principal está en `app/src/main/java/com/example/fakestoreroles/`. Se conserva el paquete Kotlin `com.example.fakestoreroles` para que las referencias y el manifiesto sigan funcionando.

| Carpeta | Responsabilidad |
| --- | --- |
| `model/` | `Product`, `Rating` y carrito temporal. Los datos se validan al convertir JSON. |
| `validation/` | `ProductRules` y `LoginRules`, independientes de la interfaz. |
| `network/` | `StoreTransport`, conexión HTTP, conectividad y mensajes por código HTTP. |
| `service/` | Autenticación, almacenamiento de sesión y repositorio de productos. |
| `state/` | `LoginController` y `CatalogViewModel`: coordinación de acceso, filtros, solicitudes y errores. |
| `ui/` | Actividades, adaptador y componentes visuales. |

La vista de acceso llama a `LoginController`; el catálogo observa `CatalogViewModel`. `ProductRepository` es una interfaz inyectable y `StoreTransport` permite probar las rutas y cuerpos sin Internet. Las actividades conservan la navegación y los diálogos propios de Android. Las clases y las acciones principales llevan comentarios junto a su implementación.

## Controles y permisos

| Control | Función y validación |
| --- | --- |
| Iniciar sesión | Requiere usuario y contraseña con límites de longitud; bloquea el formulario mientras responde la API. |
| Actualizar y Reintentar | Repiten la consulta de productos o categorías tras error. |
| Filtros de categoría | Consultan solo la categoría elegida y descartan respuestas antiguas. |
| Ver detalle y Volver | Navegan entre catálogo y producto. |
| Agregar al carrito | Visible para cliente; aumenta el conteo local. |
| Editar y Guardar | Solo administrador; valida título, descripción, precio, categoría e imagen HTTPS antes de PUT. |
| Eliminar y Cancelar | Solo administrador; pide confirmación antes de DELETE. |
| Cerrar sesión | Borra sesión y carrito y regresa al acceso. |

El repositorio comprueba nuevamente el rol antes de PUT y DELETE, aunque un control no se dibuje para cliente o auditor. Fake Store API simula PUT y DELETE: una respuesta correcta no significa persistencia.

`HttpErrorMapper` distingue 400/401, 403, 404, 408/504, 429, 5xx y otros códigos. El transporte informa también de espera agotada, falta de conexión, respuesta vacía y JSON inválido. Los mensajes conservan el código HTTP para diagnóstico.

## Pruebas

Desde la raíz del proyecto: `./gradlew.bat :app:testDebugUnitTest --offline`. Las pruebas cubren autenticación con transporte falso, validaciones antes de red, roles, datos JSON incorrectos, rutas codificadas, errores HTTP, escrituras prohibidas, confirmaciones inválidas y cambios de selección del catálogo. La ejecución final pasó en una copia temporal limpia porque OneDrive bloqueó la carpeta generada de resultados en este directorio.
