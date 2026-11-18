# Nokia MultiTap para Android (v2.0, Jetpack Compose)

Prototipo Android en Kotlin que convierte texto escrito en código Multi-Tap, localmente y sin red.

## Qué cambió en la v2.0
- Interfaz 100 % Jetpack Compose con Material 3: color dinámico (Material You) en Android 12+, modo oscuro, edge-to-edge, barra superior que se colapsa y splash screen nativo.
- Animaciones con resortes (`spring`): entrada escalonada de tarjetas, rebote al pulsar teclas, transiciones de estado del servicio y del resultado, y vibración háptica.
- Teclado Nokia interactivo: tocá varias veces una tecla para cambiar de letra, como en un Nokia real. En modo "Código a texto" escribe los dígitos del código.
- Panel flotante renderizado con Compose (con `OverlayLifecycleOwner`), con animación de entrada y salida, botón de copiar, botón de cerrar y ocultado automático a los 8 s.
- Servicio más liviano: solo escucha cambios de texto y evita recorrer el árbol de vistas en cada pulsación.
- Compilación de release con R8 y reducción de recursos, `profileinstaller` y catálogo de versiones (`gradle/libs.versions.toml`).
- Pruebas unitarias del codec (`MultiTapCodecTest`).

## Compilar con GitHub Actions
1. Subí el contenido de esta carpeta al repositorio, conservando las rutas.
2. Ejecutá **Build Nokia MultiTap APK** en Actions (también corre al hacer push a `main`).
3. Descargá el artefacto `NokiaMultiTap-APK` y extraé `app-release.apk`.

El APK de release está optimizado, pero firmado con la clave de depuración: sirve para probar en tu teléfono, no para publicar en Play Store. Para máxima fluidez, usá siempre el release; el debug de Compose es notablemente más lento.

## Activación
1. Instalá el APK y abrí Nokia Multi-Tap.
2. Tocá **Activar servicio** y habilitalo en Accesibilidad.
3. En Android 13+, si el interruptor aparece gris: Info de la app, menú ⋮, **Permitir ajustes restringidos**.
4. Abrí una app de mensajería y escribí con Gboard.

## Versiones usadas
AGP 8.13.0, Kotlin 2.3.21, Compose BOM 2026.09.00 (Material 3 1.4), Gradle 8.14.3, compileSdk 36.
Si Gradle pide un compileSdk mayor para alguna librería, subí `compileSdk` y `targetSdk` en `app/build.gradle.kts`.

## Limitaciones importantes
- Android y cada aplicación deciden qué campos y botones exponen al servicio de accesibilidad. La detección y el envío no están garantizados en todas las aplicaciones.
- El botón de enviar usa `ACTION_SET_TEXT` y luego busca un control accesible cuyo texto o descripción contenga `Enviar`, `Send`, `Mandar` o `Submit`. Si la app no lo expone, el código queda insertado y hay que enviarlo manualmente.
- El panel aparece al detectar un cambio de texto en un campo editable, que normalmente ocurre mientras se escribe con el teclado.
- El código se abrevia solo en pantalla; la secuencia Multi-Tap enviada no se altera.
- No se procesan campos marcados como contraseña.
- Este proyecto se armó sin poder compilarlo en el entorno de trabajo: probalo en un dispositivo y en cada app objetivo antes de darlo por terminado.
