# FitHome 🏋‍♂⌚📺

FitHome es una innovadora aplicación de fitness para el hogar, desarrollada nativamente para Android, que crea una experiencia de entrenamiento completamente conectada y sincronizada entre tu teléfono móvil, tu reloj inteligente (Wear OS) y tu televisión (Android TV).

El objetivo de FitHome es eliminar las distracciones y guiar al usuario de forma inmersiva, utilizando cada dispositivo para lo que mejor sabe hacer: el móvil como centro de control, el reloj como monitor personal y la TV como guía visual.

## ✨ Características Principales

*   *Sincronización Multi-dispositivo en Tiempo Real:* Inicia una rutina en el móvil y observa cómo el reloj y la TV responden al instante.
*   *Conteo de Repeticiones por Sensor (Wear OS):* El reloj utiliza su acelerómetro para detectar y contar las repeticiones de ejercicios como sentadillas, liberando al usuario de contarlas manualmente.
*   *Inicio Inteligente por Sensor (Móvil):* La rutina comienza automáticamente cuando el usuario coloca el teléfono boca abajo, gracias al sensor de luz ambiental. ¡Cero distracciones!
*   *Guía Visual en Pantalla Grande (Android TV):* Un video demostrativo del ejercicio se reproduce en la TV, asegurando que el usuario mantenga la forma correcta.
*   *Control Centralizado:* Finaliza la rutina desde el móvil y todos los dispositivos se detendrán y reiniciarán, listos para la siguiente sesión.
*   *Interfaz de Usuario Moderna:* Diseños limpios y profesionales construidos con XML, Material Design y componentes específicos para cada factor de forma.

---

### 👥 Equipo y Roles

*   *Sergio (Líder de Proyecto y Arquitecto de Software):*
    *   *Rol:* Como líder del proyecto, Sergio fue responsable de definir la visión general y la arquitectura del sistema. Diseñó el flujo de comunicación multi-dispositivo, implementó la lógica de backend (servidor Ktor en la TV), la comunicación entre los módulos (API Wearable, HTTP) y la integración de los sensores. También se encargó de las pruebas de integración para asegurar que todos los componentes funcionaran en perfecta armonía.

*   *Alejandra (Desarrolladora de UI/UX - Móvil):*
    *   *Rol:* Alejandra se encargó de toda la experiencia de usuario en la aplicación móvil, el punto de entrada principal para el usuario. Diseñó e implementó la interfaz de usuario utilizando XML y Material Design, creando las tarjetas de ejercicio, la pantalla de selección y la pantalla de "rutina en curso". Su trabajo fue crucial para asegurar que la aplicación fuera intuitiva, atractiva y fácil de usar.

*   *Eduardo (Diseñador de Interfaz - Wear OS):*
    *   *Rol:* Eduardo fue el especialista en la interfaz para dispositivos vestibles. Diseñó y desarrolló el layout de la aplicación para Wear OS, asegurando que la información crítica (repeticiones, tiempo) fuera clara y legible en una pantalla redonda y pequeña. Su enfoque en la usabilidad y el diseño específico para wearables fue clave para crear una experiencia de monitorización efectiva y sin fricciones.

*   *Ángel (Documentador Técnico / Technical Writer):*
    *   *Rol:* Ángel fue el responsable de la documentación completa del proyecto. Su trabajo incluyó la descripción detallada de la arquitectura del sistema, el flujo de comunicación entre los módulos y la guía de configuración. Una documentación clara y precisa es esencial para la mantenibilidad del código y para que otros desarrolladores puedan entender y colaborar en el proyecto.

## 🏛 Arquitectura y Flujo de Comunicación

El proyecto está construido sobre una arquitectura multi-módulo en Gradle, donde cada dispositivo tiene su propio módulo (mobile, wear, tv). La comunicación se orquesta desde el módulo móvil, que actúa como el "cerebro" del sistema.

*El flujo de datos es el siguiente:*

1.  *Móvil (Selección):* El usuario elige un ejercicio.
2.  *Móvil (Distribución de Órdenes):*
    *   Móvil -> TV: Envía una petición *HTTP* a través de la red local (Wi-Fi) al servidor Ktor de la TV para reproducir un video.
    *   Móvil -> Reloj: Envía un mensaje con los datos de la rutina (nombre, tiempo, reps) usando la *API Wearable de Google*.
3.  *Móvil (Inicio Inteligente):*
    *   Detecta la oscuridad con el sensor de luz y envía un segundo mensaje (/start_timer) al reloj.
4.  *Reloj (Ejecución):*
    *   Recibe la orden de inicio, activa el temporizador y empieza a contar repeticiones con el acelerómetro.
5.  *Móvil (Finalización):*
    *   El usuario pulsa "Finalizar". El móvil envía órdenes de parada a la TV (HTTP) y al reloj (API Wearable).

## 🛠 Stack Tecnológico

Este proyecto utiliza tecnologías modernas y recomendadas para el desarrollo nativo de Android.

#### 📱 Módulo mobile (El Cerebro)
*   *Lenguaje:* Kotlin
*   *UI:* XML con ConstraintLayout, RecyclerView y MaterialCardView (Material Design 3).
*   *Sensores:* SensorManager para Sensor.TYPE_LIGHT.
*   *Comunicación (a Reloj):* Google Wearable Data Layer API (MessageClient).
*   *Comunicación (a TV):* Ktor Client para peticiones HTTP.
*   *Concurrencia:* Kotlin Coroutines y lifecycleScope.

#### ⌚ Módulo wear (El Monitor)
*   *Lenguaje:* Kotlin
*   *UI:* XML con BoxInsetLayout y ConstraintLayout para adaptarse a pantallas redondas.
*   *Sensores:* SensorManager para Sensor.TYPE_ACCELEROMETER con HIGH_SAMPLING_RATE_SENSORS.
*   *Comunicación:* WearableListenerService para recibir mensajes en segundo plano y BroadcastReceiver para comunicar el servicio con la Activity.

#### 📺 Módulo tv (La Guía Visual)
*   *Lenguaje:* Kotlin
*   *UI:* XML con FrameLayout y PlayerView.
*   *Video:* ExoPlayer para una reproducción de video robusta y eficiente.
*   *Servidor:* *Ktor Server* con el motor *CIO (Coroutine-based I/O)*, escuchando en el puerto 8080 las rutas /play y /stop.
*   *Concurrencia:* Kotlin Coroutines (Dispatchers.IO) para ejecutar el servidor en un hilo de fondo sin bloquear la UI.

## 🚀 Configuración y Puesta en Marcha

Para ejecutar este proyecto, necesitarás Android Studio y configurar el entorno para la comunicación entre los dispositivos.

### Prerrequisitos
*   Android Studio (versión reciente).
*   Emuladores configurados:
    *   Un emulador de Teléfono (API 30+).
    *   Un emulador de Wear OS (API 30+).
    *   Un emulador de Android TV (API 30+).

### Pasos para la Ejecución

1.  *Clonar el Repositorio:*
    bash
    git clone https://github.com/tu-usuario/FitHome.git
    

2.  *Configuración de Red (¡CRÍTICO!):*
    La comunicación entre el móvil y la TV requiere una configuración de red específica.

    *   *Actualiza la IP en el código:*
        *   En el archivo mobile/src/main/java/com/example/fithome/MainActivity.kt, modifica la variable tvIpAddress a "10.0.2.2".
        *   En el archivo mobile/res/xml/network_security_config.xml, asegúrate de que el dominio permitido sea 10.0.2.2.

    *   *Crear el Túnel de Comunicación:*
        1.  Inicia el emulador de la *TV*.
        2.  Abre una terminal en Android Studio.
        3.  Obtén el ID del emulador de la TV con adb devices.
        4.  Crea un túnel de red desde tu PC al emulador de la TV con el siguiente comando (reemplaza <ID_DE_LA_TV>):
            bash
            adb -s <ID_DE_LA_TV> forward tcp:8080 tcp:8080
            
        5.  *IMPORTANTE:* No cierres esta terminal mientras estés probando la aplicación.

3.  *Orden de Lanzamiento:*
    1.  Ejecuta la app del módulo **tv** en el emulador de TV.
    2.  Ejecuta la app del módulo **wear** en el emulador de Wear OS.
    3.  Ejecuta la app del módulo **mobile** en el emulador de Teléfono.

4.  *¡A Entrenar!*
    *   Selecciona un ejercicio en el móvil.
    *   Pon el emulador del móvil "boca abajo" usando los controles extendidos del emulador (... > Virtual sensors > Light).
    *   ¡Observa cómo todo se sincroniza!

## 📂 Estructura del Proyecto

```
FitHome/
├── mobile/         # Módulo de la aplicación para teléfono
├── wear/           # Módulo de la aplicación para Wear OS
└── tv/             # Módulo de la aplicación para Android TV
```
