# Nokia Multi-Tap Android

Proyecto inicial Android nativo en Kotlin. Mantiene Gboard como teclado de entrada y usa un AccessibilityService opcional para intentar mostrar un panel flotante con el código Multi-Tap.

## Requisitos
- Android Studio reciente
- JDK 17
- Android SDK 35
- Un dispositivo Android para probar el servicio

## Abrir y compilar
1. Descomprimir el proyecto.
2. Abrir la carpeta `NokiaMultiTap` en Android Studio.
3. Esperar la sincronización de Gradle.
4. Ejecutar en un teléfono o emulador.
5. En la app, usar "Abrir ajustes de accesibilidad".
6. Activar manualmente "Nokia Multi-Tap: panel flotante".
7. Abrir una app de chat, tocar un campo de texto y escribir.

## Limitaciones importantes
- Android no garantiza que un servicio de accesibilidad pueda leer todos los campos de texto de todas las aplicaciones.
- Algunos chats, WebViews, campos personalizados y campos protegidos no exponen el texto completo.
- La posición del panel depende del teclado, del fabricante y de la aplicación; puede requerir ajustar el valor `y` en `TextObserverService.kt`.
- Esta es una base de desarrollo, no un APK ya compilado ni una versión validada en todos los dispositivos.
- La accesibilidad es un permiso sensible. Activala solo si entendés que permite observar cambios de texto en campos compatibles. El código no guarda ni transmite el contenido.
- Los campos de contraseña se excluyen. Aun así, el servicio debe probarse cuidadosamente antes de usarlo a diario.

## Convención de codificación
- Letras: distribución Multi-Tap indicada en el proyecto.
- Espacio entre letras.
- `/` entre palabras.
- Acentos normalizados (á -> A).
- Ñ se normaliza a N porque la distribución especificada no asigna una tecla exclusiva a Ñ.
- Puntuación básica en tecla 1: punto=1, coma=11, pregunta=111, admiración=1111.
- Los caracteres no contemplados se omiten en la codificación inicial.
