# LAB6 — Mis sesiones de estudio

Aplicación Android desarrollada en Java y XML para registrar y consultar sesiones de estudio. Utiliza Firebase Authentication, Cloud Firestore, RecyclerView y MPAndroidChart.

## Funciones

- Inicio de sesión con correo y contraseña o Google mediante FirebaseUI.
- Registro de cuentas y recuperación de contraseña.
- Registro, edición y eliminación de sesiones de estudio.
- Lista actualizada en tiempo real y ordenada por fecha, de más reciente a más antigua.
- Gráficos de horas por mes y distribución por tipo de actividad, con filtro por curso.
- Acceso a los registros de cada usuario mediante su UID.
- Interfaz en español y cierre de sesión.

Cada sesión contiene curso, fecha (`AAAA-MM-DD`), horas y tipo de actividad (`Teoría`, `Práctica` o `Laboratorio`). Los documentos se guardan en `usuarios/{uid}/sesiones/{idSesion}`.

## Abrir y ejecutar

1. Clona el repositorio y abre su carpeta raíz en Android Studio.
2. Instala la plataforma Android SDK 37 y configura el JDK que solicita Gradle. La configuración actual del daemon usa Java 25.
3. Registra una aplicación Android en Firebase con el identificador `com.example.lab6`.
4. Descarga `google-services.json` desde Firebase y colócalo dentro de `app/`. Este archivo se mantiene local y no se incluye en Git.
5. Habilita los proveedores Correo electrónico/Contraseña y Google en Firebase Authentication. Registra la huella SHA-1 de tu certificado de depuración y descarga de nuevo el JSON si cambia la configuración.
6. Crea Cloud Firestore. Copia el contenido de `firestore.rules` en la pestaña Reglas de la consola y pulsa Publicar.
7. Sincroniza Gradle y ejecuta la aplicación en un dispositivo Android 7.0 (API 24) o superior. Para probar Google, usa un dispositivo o emulador con servicios de Google Play.

No es necesario crear manualmente las colecciones: la aplicación las genera al guardar la primera sesión de estudio.

## Verificación desde la terminal

Con el SDK, el JDK y el archivo de Firebase configurados, en Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

En Linux o macOS:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Las pruebas unitarias comprueban los cálculos de los resúmenes y la validación de los registros. El inicio de sesión y la conexión al proyecto real de Firebase se comprueban ejecutando la aplicación.

## Estructura

- `app/src/main/java/com/example/lab6/`: actividades, modelo, adaptador y cálculos.
- `app/src/main/res/layout/`: pantallas y diseño de cada registro.
- `app/src/main/res/values/strings.xml`: textos de la aplicación.
- `app/src/test/`: pruebas unitarias.
- `firestore.rules`: reglas de acceso y validación de datos.

Las carpetas de compilación, las cachés, la configuración local del IDE y las claves de firma se excluyen mediante `.gitignore`.
