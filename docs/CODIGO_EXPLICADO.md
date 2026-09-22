# Explicación del código por archivo

Lee primero los modelos, después servicios, estado y pantallas. Este documento explica las responsabilidades y los métodos; el anexo HTML permite seguir el código completo por línea.

## app/src/main/java/com/example/fakestoreroles/Product.kt

Product es una data class inmutable con los campos del JSON. fromJson valida enteros positivos, precios finitos y cadenas; required centraliza validación de texto. toJson construye el cuerpo PUT. Rating representa puntuación y conteo opcionales, con límites. ProductRules.text, parsePrice, price e image son funciones puras para el formulario; validate repite reglas de modelo y pertenencia de categoría en la capa de datos.

## app/src/main/java/com/example/fakestoreroles/ProductRepository.kt

ProductRepository declara las operaciones que necesita la UI. StoreTransport permite intercambiar la red por un doble de prueba. HttpStoreTransport usa HttpURLConnection con plazos de 15 segundos, JSON UTF-8 y Accept; verifica HTTP y libera recursos con use y finally. HttpProductRepository recibe un lector de sesión. parse convierte JSON incorrecto en ApiException. products selecciona ruta y codifica categoría; categories valida textos y elimina duplicados; detail exige identidad consistente. requireAdmin lee la sesión antes de PUT/DELETE. update valida campos y respuesta; delete exige confirmación con el mismo ID.

## app/src/main/java/com/example/fakestoreroles/CatalogViewModel.kt

CatalogState es una instantánea inmutable de productos, categorías, filtro, carga y errores. CatalogViewModel expone StateFlow de solo lectura y modifica MutableStateFlow privado. init inicia las dos consultas. load limpia datos, aumenta generation y cancela la tarea de productos anterior; el trabajo de red usa withContext(io). Solo el resultado actual se publica. Las cancelaciones se relanzan para respetar el ciclo de vida. loadCategories evita peticiones repetidas mientras carga y deja consultar productos aunque falle el filtro. El dispatcher es inyectable para pruebas deterministas.

## app/src/main/java/com/example/fakestoreroles/CatalogActivity.kt

onCreate exige sesión, obtiene el ViewModel con una fábrica y monta encabezado, acciones, categorías y contenedor de productos. registerForActivityResult recibe el retorno del detalle y restablece el catálogo general cuando corresponde. repeatOnLifecycle recoge estado solo mientras la pantalla está iniciada. RecyclerView se oculta inmediatamente al cargar para que DiffUtil no deje ver datos antiguos. Las categorías solo se reconstruyen cuando cambia lista o selección. goLogin limpia la pila; Salir elimina también sesión y carrito. Usuarios se construye únicamente para Administrador.

## app/src/main/java/com/example/fakestoreroles/ProductAdapter.kt

ListAdapter calcula diferencias entre listas con DiffUtil. Holder agrupa referencias a las vistas de una fila. onCreateViewHolder construye una tarjeta reciclable y accesible con imagen y textos. onBindViewHolder introduce el producto actual y Glide gestiona carga/caché de imagen. El click entrega Product al catálogo. onViewRecycled cancela la imagen asociada. Diff identifica objetos por ID y compara el resto por igualdad de data class.

## app/src/main/java/com/example/fakestoreroles/StoreUi.kt

Funciones de presentación compartidas: dp convierte medidas, label crea texto, action crea botones y cardBackground un fondo redondeado. storeRoot configura título y contenedor claro. ViewCompat aplica insets de barras del sistema y teclado para impedir que oculten controles en Android reciente. Estos helpers mantienen el estilo uniforme y no realizan peticiones ni validaciones de negocio.

## app/src/main/java/com/example/fakestoreroles/ProductDetailActivity.kt

onCreate valida sesión y solicita el ID con lifecycleScope/Dispatchers.IO. Un error muestra Toast, marca resultado de restablecimiento y termina la Activity. render muestra imagen Glide, texto y precio; lee el perfil local y crea controles de gestión solo si es Administrador. Cliente conserva el contador de carrito. confirmDelete permite cancelar y comprueba confirmación remota antes de regresar. edit carga categorías; showEditor crea un formulario desplazable y usa ProductRules para señalar errores por campo. El botón Guardar se sustituye por un listener que evita cerrar ante validación fallida. Durante PUT se deshabilitan entradas y botones; los errores conservan el formulario. CancellationException se propaga para no actualizar actividades destruidas.

## app/src/main/java/com/example/fakestoreroles/ApiService.kt

Código base de autenticación y usuarios conservado. UserRole y SessionData tipan el perfil y la sesión. ApiException contiene mensajes. roleFromId conserva la regla académica. authenticate valida entradas y la identidad del usuario antes de devolver sesión. loginToken envía POST, distingue credenciales y exige token de tipo texto no vacío. getUsers mapea la lista de usuarios a UserItem. get y readResponse centralizan GET y lectura cerrando los streams. La consulta antigua de productos sin todos los campos fue reemplazada por ProductRepository.

## app/src/main/java/com/example/fakestoreroles/SessionManager.kt

Encapsula preferencias cifradas y MasterKey heredados. saveSession conserva los cuatro valores, getSession captura fallos del almacenamiento y readSession exige token, nombre, ID y enum correctos. clearSession elimina la sesión. La biblioteca security-crypto está deprecada en la versión utilizada: se mantiene para compatibilidad con el proyecto de origen y se registra como deuda técnica, sin ocultar la advertencia de compilación.

## app/src/main/java/com/example/fakestoreroles/LoginActivity.kt

Comprueba sesión al iniciar; si existe abre CatalogActivity. En el formulario valida vacíos y longitudes, consulta conectividad, deshabilita el botón y lanza el acceso en un hilo secundario como en la base recibida. Los resultados vuelven al hilo de UI solo si la Activity sigue válida. openSession reemplaza la pila con el catálogo. No se guarda la contraseña. El código heredado usa Thread; el nuevo catálogo y detalle usan corrutinas ligadas al ciclo de vida.

## app/src/main/java/com/example/fakestoreroles/NetworkUtils.kt

Utilidad heredada que consulta ConnectivityManager y capacidades de red para comprobar conectividad antes del acceso. Esa comprobación orienta el mensaje, pero no garantiza que Fake Store esté disponible; las peticiones también manejan sus propios errores.

## app/src/main/java/com/example/fakestoreroles/AdminActivity.kt

Pantalla heredada para consultar usuarios. Exige sesión Administrador, prepara vistas y llama loadUsers. La consulta se ejecuta fuera del hilo principal y solo actualiza una Activity válida. addUserView muestra nombre, correo y rol local. logout elimina sesión y carrito; goToLogin reemplaza la pila. Se conserva la lista administrativa previa; el catálogo de productos nuevo sí utiliza RecyclerView conforme a US03.

## app/src/main/java/com/example/fakestoreroles/ClientActivity.kt

Adaptador de una Activity anterior: onCreate redirige a CatalogActivity y finaliza, evitando mantener dos implementaciones de catálogo.

## app/src/main/java/com/example/fakestoreroles/AuditorActivity.kt

Adaptador de la ruta previa del Auditor hacia el catálogo común, que exige sesión y deriva permisos de la información local.

## app/src/main/java/com/example/fakestoreroles/CartState.kt

Objeto de memoria con colección privada. add acepta IDs positivos; count informa cantidades y clear vacía al salir. Conserva el alcance de contador del proyecto previo.

## app/src/test/java/com/example/fakestoreroles/ProductTest.kt

Pruebas JUnit del modelo, reglas, JSON incorrecto, imagen opcional, rutas codificadas, sesión y métodos de escritura. StoreTransport falso registra peticiones y devuelve respuestas controladas; no modifica datos remotos.

## app/src/test/java/com/example/fakestoreroles/CatalogViewModelTest.kt

Prueba determinista del estado con StandardTestDispatcher: verifica loading inicial, limpieza al filtrar, categoría, error, reintento, restablecimiento y rechazo de selección desconocida. Sustituye Dispatchers.Main y lo restaura al terminar.


## Archivos de configuración y recursos

El README es el punto de entrada. docs/GUIA_COMPLETA.html reúne requisitos, explicación y anexos en un archivo que se abre sin Internet. docs/ARQUITECTURA_Y_REQUISITOS.md explica decisiones y criterios; docs/CODIGO_EXPLICADO.md explica cada archivo de aplicación. docs/VERIFICACION.md separa comprobaciones reales y pendientes. docs/historias contiene transcripciones de los documentos recibidos.

Los manifests Android declaran permiso INTERNET, pantalla de arranque, nombre y actividades internas no exportadas. ACCESS_NETWORK_STATE en Kotlin permite la comprobación heredada de conectividad. allowBackup=false evita restaurar sesiones cifradas en otro dispositivo. Los estilos definen temas y colores; los layouts XML restantes de Kotlin corresponden al acceso y la consulta administrativa heredados. Las nuevas pantallas Android se construyen en Kotlin para mantener toda la lógica de presentación explicada junto a sus componentes.

Los archivos build.gradle.kts describen plugin, namespace, SDK, versión, dependencias y opciones de compilación. settings.gradle.kts registra repositorios y módulos; en Flutter localiza también su SDK. gradle.properties configura AndroidX y memoria. gradle-wrapper.properties fija la distribución de Gradle; gradlew, gradlew.bat y gradle-wrapper.jar la inician. El wrapper es infraestructura generada, no lógica de negocio.

En Flutter pubspec.yaml declara dependencias, versión y recursos; pubspec.lock fija las versiones resueltas; analysis_options.yaml activa reglas del analizador. MainActivity.kt del contenedor Flutter hereda FlutterActivity y deja renderizado y lógica a Dart. GeneratedPluginRegistrant.java es registro generado de plugins; Flutter puede recrearlo. .gitignore evita incorporar cachés, APK, carpetas de IDE y rutas personales.

El anexo HTML presenta el código de aplicación y sus archivos de configuración completos con números de línea. La explicación se organiza por clase, método y bloque funcional: imports reúnen dependencias, constructores fijan colaboradores, campos conservan estado, métodos aplican comportamiento y callbacks conectan acciones del usuario. Los números corresponden a los archivos de esta entrega, no al ZIP previo.
