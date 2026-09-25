# Guía para exponer Fake Store Kotlin

## 1. Qué hace la aplicación
Es una app Android escrita en Kotlin. Permite iniciar sesión con Fake Store API, consultar un catálogo, filtrar por categoría y abrir el detalle de un producto. Según el usuario, muestra acciones de administrador, auditor o cliente.

Frase de presentación: «Mi aplicación separa la pantalla, la consulta de datos y las reglas de validación. El servidor valida el login y la aplicación asigna un perfil local para esta práctica».

## 2. Cómo abrir y ejecutar
Abre la carpeta **FakeStore_Kotlin_Android**, la que contiene `settings.gradle.kts`, `gradlew.bat` y `app`. No abras solamente `app`, la carpeta exterior ni el ZIP.

**Android Studio:** File > Open, elige esa carpeta y espera la sincronización. Selecciona `app`, un dispositivo en Device Manager y pulsa Run. La configuración del proyecto usa `#GRADLE_LOCAL_JAVA_HOME`; su ruta concreta está en `.gradle/config.properties`. El SDK se encuentra en `local.properties`.

**VS Code:** File > Open Folder, elige la misma carpeta. Terminal > Run Task > **Fake Store: ejecutar app**. También hay tareas para compilar y probar. Estas tareas usan Gradle, que es quien realmente construye Android. No uses «Run Kotlin File» para una Activity: no es un programa de consola.

`ejecutar.ps1` prepara Java y el SDK, ejecuta Gradle, busca un dispositivo y, si hace falta, inicia un emulador existente. Si hay más de uno conectado, pide dejar uno para evitar instalar en el equipo incorrecto. No borra datos ni desinstala la app.

La primera sincronización necesita Internet. No borres la carpeta del SDK señalada por `local.properties`. En otro equipo, Android Studio debe generar sus propias rutas locales.

## 3. Mapa del proyecto
Las clases están en `app/src/main/java/com/example/fakestoreroles/`.

| Archivo | Responsabilidad |
|---|---|
| `LoginActivity.kt` | Formulario, validaciones y proceso de acceso. Incluye `LoginRules`. |
| `ApiService.kt` | Login, lectura de usuarios, modelos de sesión y asignación de roles. |
| `NetworkUtils.kt` | Comprobación inicial de conectividad. |
| `SessionManager.kt` | Guarda, recupera y elimina la sesión cifrada. |
| `CatalogActivity.kt` | Pantalla del catálogo, categorías, mensajes, navegación y salida. |
| `CatalogViewModel.kt` | Conserva el estado y coordina cargas de catálogo y categorías. |
| `ProductRepository.kt` | Contrato del repositorio, conexión HTTP y operaciones de productos. |
| `Product.kt` | Modelos `Product` y `Rating`, conversión JSON y `ProductRules`. |
| `ProductAdapter.kt` | Convierte cada producto en una fila del RecyclerView. |
| `ProductDetailActivity.kt` | Detalle, reintento, edición y eliminación simuladas. |
| `AdminActivity.kt` | Consulta de usuarios para el administrador. |
| `CartState.kt` | Conteo del carrito temporal del cliente. |
| `StoreUi.kt` | Componentes visuales compartidos y espacio para barras del sistema. |
| `ClientActivity.kt`, `AuditorActivity.kt` | Pantallas heredadas de la base; el login actual lleva a todos al catálogo. |

`AndroidManifest.xml` declara las pantallas y permisos. Solo el login es una entrada pública. Los XML de `res/layout` definen los formularios de login y usuarios; el catálogo y detalle construyen sus vistas en Kotlin.

`build.gradle.kts` de la raíz fija los plugins. El de `app` configura Android y sus dependencias. El wrapper en `gradle/wrapper` fija Gradle 8.9. No hay que instalar otra versión de Gradle por separado.

## 4. Login paso a paso
1. El usuario escribe su nombre y contraseña y pulsa Iniciar sesión.
2. `LoginRules` detecta campos vacíos y límites de longitud. El usuario admite 100 caracteres y la contraseña 256. Se quitan espacios externos del usuario; la contraseña se conserva tal como fue escrita.
3. `NetworkUtils` comprueba la conectividad. Esto no garantiza que el servidor esté disponible: la petición también necesita manejar errores.
4. Se desactiva el formulario y aparece un indicador de carga para evitar dobles envíos.
5. `lifecycleScope.launch` inicia una tarea asociada a la pantalla. `withContext(Dispatchers.IO)` ejecuta la conexión fuera del hilo que dibuja la interfaz.
6. `ApiService.authenticate` envía un JSON con usuario y contraseña mediante `POST /auth/login`.
7. Se verifica que el servidor devuelva un token no vacío. Después, `GET /users` permite localizar el ID del usuario.
8. Se asigna el rol local, se guarda la sesión y se abre el catálogo. Se limpia la pila de pantallas para que Atrás no regrese al formulario autenticado.

Una coroutine es una tarea que puede suspenderse sin congelar la pantalla. El ciclo de vida la cancela cuando se destruye la Activity. La conexión HTTP tiene tiempos límite; puede terminar en segundo plano tras una cancelación, pero su resultado cancelado no debe guardar una sesión ni navegar.

## 5. Modelos, servicio y repositorio
Un modelo es una estructura que representa datos: `Product` contiene ID, título, precio, descripción, categoría, imagen y valoración; `SessionData` contiene token, ID, usuario y rol; `UserItem` describe un usuario.

JSON es el formato de intercambio del servidor. `Product.fromJson` lo convierte a Kotlin y valida campos antes de mostrarlos. Un ID inválido o precio negativo se rechaza; una imagen incorrecta usa una alternativa visual y una valoración inválida se omite.

`StoreTransport` es una interfaz: define cómo pedir datos sin obligar a utilizar Internet. `HttpStoreTransport` es su implementación real con `HttpURLConnection`. En las pruebas se sustituye por respuestas controladas. No se cambian las credenciales reales por un acceso falso.

`ProductRepository` expresa las operaciones que necesita la app. `HttpProductRepository` decide las rutas y valida las respuestas. Así, la Activity se concentra en mostrar información y el repositorio en obtenerla.

## 6. Roles y sesión
| ID del usuario | Rol local | Acciones principales |
|---|---|---|
| 1 o 2 | Administrador | Catálogo, detalle, usuarios, editar y eliminar de demostración. |
| 3 | Auditor | Consulta del catálogo y detalle sin controles de gestión. |
| Otros | Cliente | Consulta y agregar al carrito temporal. |

**Los roles son una regla escolar dentro de la app. Fake Store no entrega estos roles ni impone nuestros permisos locales.** En producción, el servidor tendría que verificar cada operación. El token se guarda, pero los endpoints públicos de productos de esta práctica no requieren que lo enviemos como autorización.

`SessionManager` usa `EncryptedSharedPreferences` y una clave protegida por Android. No guarda la contraseña. Al abrir la app, una sesión local válida evita repetir el login. No hay un sistema completo de renovación o revocación remota de tokens en este proyecto.

Cuentas públicas de demostración incluidas en la pantalla:

| Perfil | Usuario | Contraseña |
|---|---|---|
| Administrador | `mor_2314` | `83r5^_` |
| Auditor | `kevinryan` | `kev02937@` |
| Cliente | `donero` | `ewedon` |

## 7. Catálogo y filtros
`CatalogViewModel` mantiene un `CatalogState`: lista de productos, categorías, selección, indicadores de carga y mensajes de error. `StateFlow` comunica sus cambios a la pantalla. La Activity observa mientras está visible.

`GET /products` carga todo. `GET /products/categories` obtiene las categorías. Al seleccionar una, se consulta `GET /products/category/{categoría}`. La categoría se codifica para que espacios y apóstrofos sean válidos en una URL. «Ver todos» elimina el filtro; tocar otra vez la categoría seleccionada también lo elimina.

Cuando cambia la categoría, se cancela la carga anterior y se identifica la petición nueva. Esto evita que una respuesta lenta reemplace resultados de una selección más reciente. `RecyclerView` reutiliza filas en lugar de crear una vista por cada producto. `Glide` carga las imágenes y muestra un icono alternativo si fallan.

## 8. Detalle y validaciones de edición
Al tocar un producto, un `Intent` envía su ID. El detalle consulta `GET /products/{id}` y confirma que el ID de la respuesta coincida. Si falla, muestra el mensaje y permite reintentar o volver.

Para editar se valida título obligatorio (hasta 200 caracteres), descripción obligatoria (hasta 5000), precio mayor que cero y hasta 1 000 000, hasta dos decimales en el formulario, URL HTTPS y categoría válida. El formulario marca el campo incorrecto; el repositorio vuelve a validar antes de enviar.

Solo el administrador ve Editar y Eliminar. Además de ocultar botones, el repositorio comprueba el rol antes de enviar `PUT` o `DELETE`. El borrado pide confirmación. Los botones se desactivan mientras se procesa una operación.

**Fake Store simula las modificaciones:** una respuesta exitosa confirma el ejemplo de petición, no un cambio permanente. Tras refrescar pueden reaparecer los valores originales. El carrito también es temporal: solo cuenta productos en memoria; no implementa pago ni persistencia.

## 9. Navegación y cierre de sesión
Flujo principal: Login → Catálogo → Detalle → Catálogo. El administrador también puede abrir Usuarios. `Intent` indica la pantalla destino y puede transportar datos como el ID.

Salir elimina la sesión y el carrito y abre Login con `NEW_TASK` y `CLEAR_TASK`. Esto elimina las pantallas anteriores para que Atrás no permita regresar a una pantalla de la sesión cerrada. Al volver del detalle se conserva la categoría elegida.

## 10. Manejo de errores
Se distinguen formulario inválido, credenciales incorrectas (400/401 en login), rechazo de conexión (403), producto inexistente (404), espera excesiva, problemas de conexión y respuesta JSON inválida. Las conexiones se cierran en `finally`, tanto en éxito como en error.

El catálogo ofrece reintentar productos y categorías por separado. El detalle ofrece reintento sin expulsarte del flujo. La pantalla de usuarios bloquea recargas simultáneas. Los errores de almacenamiento o fallos inesperados no deben confundirse con credenciales incorrectas.

## 11. Guion de exposición de 5–7 minutos
1. «La app consulta una API de práctica y usa tres perfiles locales». Enseña el login.
2. Pulsa iniciar sin datos para mostrar las validaciones.
3. Inicia como administrador y explica: formulario → API → token → usuario → rol → sesión.
4. Enseña el catálogo, una categoría y Ver todos. Explica `ViewModel`, `StateFlow` y `RecyclerView` con palabras sencillas.
5. Abre un producto, muestra sus campos y el editor. Prueba un precio inválido. Aclara que guardar/eliminar son simulaciones.
6. Vuelve, abre Usuarios y cierra sesión. Explica por qué Atrás no devuelve la sesión anterior.
7. Entra como cliente o auditor y muestra las diferencias de botones.
8. Termina mostrando `ApiService`, `ProductRepository`, `Product` y el informe de verificación.

Si preguntan «¿por qué no usar todo en una Activity?»: «Porque separar pantallas, datos y reglas facilita corregir errores y probar la lógica sin depender siempre del servidor».

## 12. Fuentes y límites
- API y operaciones simuladas: https://github.com/keikaavousi/fake-store-api
- Herramientas Android: https://developer.android.com/tools/sdkmanager
- Java y Gradle en Android Studio: https://developer.android.com/build/jdks

Consulta `CAMBIOS_Y_VERIFICACION.md` para los archivos cambiados y los resultados reales de esta revisión. Compilar correctamente no garantiza disponibilidad futura de Internet o del servidor.
