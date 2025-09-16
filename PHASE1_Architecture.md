# Arquitectura Final del Proyecto: PocketCode IDE

## 1. Visión General y Principios Arquitectónicos

Este documento consolida y define la arquitectura final para **PocketCode IDE**, un Entorno de Desarrollo Integrado nativo de Android, diseñado para ser potente, portátil y estar preparado para el futuro.

La filosofía central es superar las limitaciones computacionales del hardware móvil delegando las tareas más pesadas (compilación, análisis de IA) a una infraestructura en la nube sin servidor. Esto transforma el dispositivo móvil de un simple editor de código a un completo centro de desarrollo.

Los principios que guían esta arquitectura son:

*   **Nativo en la Nube (Cloud-Native):** El cliente es una aplicación nativa de Android, pero la plataforma aprovecha la nube para tareas intensivas, garantizando que la interfaz de usuario se mantenga rápida y fluida.
*   **Arquitectura Limpia y Modular:** El código se estructura siguiendo los principios de la Arquitectura Limpia y un enfoque multi-módulo para garantizar la escalabilidad, mantenibilidad y facilitar el desarrollo en paralelo.
*   **Seguridad por Diseño:** La seguridad es un pilar fundamental. Las credenciales sensibles, como las claves de API, nunca se exponen en el cliente y se gestionan a través de un proxy seguro en el servidor.
*   **Infraestructura como Código (IaC):** Toda la infraestructura de la nube se define mediante código (Terraform) para asegurar la reproducibilidad, el control de versiones y la automatización.

---

## 2. Arquitectura del Frontend (Cliente Nativo Android)

El frontend es una aplicación nativa de Android, moderna y rica en funcionalidades, que constituye el núcleo de la experiencia del usuario.

### 2.1. Pila Tecnológica

*   **Lenguaje:** Kotlin
*   **UI Toolkit:** Jetpack Compose (para una UI declarativa y reactiva)
*   **Arquitectura de UI:** Model-View-Intent (MVI) para un flujo de datos unidireccional y predecible.
*   **Asincronía:** Kotlin Coroutines & Flows
*   **Inyección de Dependencias:** Hilt
*   **Navegación:** Jetpack Navigation Compose

### 2.2. Estructura Multi-módulo

La aplicación se organiza en capas y módulos para lograr una alta cohesión y un bajo acoplamiento. La dirección de las dependencias es siempre hacia el interior (hacia el dominio).

| Módulo Gradle           | Tipo                | Responsabilidad Clave                                                             | Dependencias Clave                          |
| ----------------------- | ------------------- | --------------------------------------------------------------------------------- | ------------------------------------------- |
| `:app`                  | App                 | Punto de entrada, orquesta la navegación.                                         | Todos los módulos `:features:*`             |
| `:features:editor`      | Android Library     | UI del editor de código, resaltado de sintaxis.                                   | `:domain:ide`, `:core:ui`                   |
| `:features:project`     | Android Library     | UI de gestión de proyectos, explorador de archivos, Git.                          | `:domain:project`, `:core:ui`               |
| `:features:settings`    | Android Library     | UI de configuración, temas, gestión de cuenta y claves (BYOK).                    | `:domain:ide`, `:core:ui`                   |
| `:features:designer`    | Android Library     | UI del diseñador visual WYSIWYG para Jetpack Compose.                              | `:domain:ide`, `:core:ui`                   |
| `:features:marketplace` | Android Library     | UI del mercado de activos comunitarios.                                           | `:domain:marketplace`, `:core:ui`           |
| `:features:ai`          | Android Library     | UI del panel del Asistente de IA.                                                 | `:domain:ai`, `:core:ui`                    |
| `:features:management`  | Android Library     | UI de herramientas de gestión de proyecto (ej. tablero Kanban).                   | `:domain:project`, `:core:ui`               |
| `:domain:ide`           | Kotlin Library      | Lógica de negocio del IDE (casos de uso de refactorización, etc.).                | `:domain:project`, `:core:api`              |
| `:domain:project`       | Kotlin Library      | Lógica de negocio de gestión de proyectos (casos de uso de archivos, tareas, etc.). | `:core:api`                                 |
| `:domain:marketplace`   | Kotlin Library      | Lógica de negocio del mercado (casos de uso de activos).                          | `:core:api`                                 |
| `:domain:ai`            | Kotlin Library      | Lógica de negocio del Asistente de IA (casos de uso de generación).               | `:core:api`                                 |
| `:data:ide`             | Kotlin Library      | Implementación de repositorios del IDE, llamadas a la API de IA (vía BFF).        | `:domain:ide`, `:core:network`              |
| `:data:project`         | Kotlin Library      | Implementación de repositorios de proyectos, acceso a BBDD y filesystem local.    | `:domain:project`, `:core:storage`          |
| `:data:marketplace`     | Kotlin Library      | Implementación de repositorios del mercado.                                       | `:domain:marketplace`, `:core:network`    |
| `:data:ai`              | Kotlin Library      | Implementación de repositorios de IA, incluyendo modelos locales.                 | `:domain:ai`, `:core:network`               |
| `:core:ui`              | Android Library     | Componentes de Jetpack Compose compartidos, temas y utilidades de UI.             | -                                           |
| `:core:utils`           | Kotlin Library      | Utilidades de propósito general.                                                  | -                                           |
| `:core:network`         | Kotlin Library      | Cliente HTTP compartido (Ktor/Retrofit) y configuración de API.                   | -                                           |
| `:core:storage`         | Kotlin Library      | Abstracciones para acceso al almacenamiento local (filesystem, base de datos).    | -                                           |
| `:core:p2p`             | Kotlin Library      | Lógica de comunicación P2P para la función "Device Mesh".                         | -                                           |
| `:core:api`             | Kotlin Library      | Interfaces y modelos de datos (DTOs) para la comunicación entre capas.            | -                                           |

### 2.3. Sistema de Diseño

La UI se rige por un estricto sistema de diseño para garantizar la consistencia y la calidad.
*   **Filosofía:** Minimalista, intuitiva, eficiente y con una **Experiencia de Usuario Hiper-simplificada** para reducir la carga cognitiva del desarrollador.
*   **Temas:** Soporte completo para temas Claro y Oscuro, definidos a través de tokens de diseño.
*   **Fundamentos:** Tipografía, espaciado, iconografía y layout están estandarizados para todo el proyecto.
*   **Componentes:** Se prioriza la reutilización de componentes, que deben ser agnósticos al tema y documentados (ej: en Storybook).

### 2.4. IA Híbrida: Modelos Locales y en la Nube

Para optimizar costos y ofrecer funcionalidades offline, se adopta un enfoque de IA híbrido:
*   **IA en la Nube:** Para tareas complejas que requieren un razonamiento avanzado (generación de código a partir de prompts, chat de asistencia), se utilizarán las APIs a través del proxy seguro.
*   **IA Local (Offline):** Para funcionalidades en tiempo real y de baja latencia como el **autocompletado de código**, se implementarán modelos de lenguaje pequeños (SLMs) directamente en el dispositivo utilizando frameworks como **TensorFlow Lite**.

### 2.5. Patrones de UX para un IDE Móvil

La interfaz se diseñará específicamente para el uso móvil, evitando replicar patrones de escritorio que no se adaptan bien a pantallas pequeñas.
*   **Navegación Principal:** Se usará una barra de navegación inferior (`BottomNavigationView`) para el acceso rápido a las secciones principales (Editor, Proyecto, Gestión, etc.).
*   **Acceso a Herramientas:** Se evitarán las barras de herramientas sobrecargadas. Las acciones secundarias se agruparán en menús contextuales (al mantener pulsado) o modales.
*   **Configuraciones Complejas:** Se utilizarán asistentes guiados (Wizards) para tareas como la configuración inicial de un proyecto, simplificando el proceso para los usuarios novatos.
*   **Consola y Depuración:** La información de la consola y el depurador se mostrará en un panel inferior deslizable y redimensionable, permitiendo al usuario ver el código y la salida simultáneamente.

---

## 3. Arquitectura del Backend (Infraestructura en la Nube)

El backend es una arquitectura de microservicios sin servidor, diseñada para ser de bajo costo y altamente escalable, utilizando una combinación de servicios gestionados y alternativas de código abierto.

### 3.1. Pila de Servicios en la Nube y Estrategia de Bajo Costo

*   **Computación:** Google Cloud Run será la plataforma principal para desplegar los microservicios por su modelo de pago por uso.
*   **Backend como Servicio (BaaS):**
    *   **Principal:** Se utilizará **Firebase** (Firestore, Authentication, Cloud Storage) por su robusta capa gratuita y su profunda integración con Android.
    *   **Alternativas Viables:** Se consideran **Appwrite** y **Supabase** como alternativas de código abierto que pueden ser auto-alojadas para un control total de los costos a largo plazo.
*   **Acceso a APIs de IA:** Se utilizará **OpenRouter** como una puerta de enlace unificada para acceder a múltiples modelos de IA (incluyendo Gemini, DeepSeek, etc.). Esto permite optimizar costos, aumentar la redundancia y simplificar la integración.

### 3.2. Arquitectura de Microservicios

El backend se compone de varios servicios independientes que se comunican a través de APIs REST/GraphQL.

1.  **Backend for Frontend (BFF):**
    *   **Propósito:** Es el único punto de entrada para el cliente móvil.
    *   **Responsabilidades:** Autenticar todas las solicitudes, agregar datos de otros servicios y adaptar las respuestas para optimizar el rendimiento del cliente.

2.  **Proxy de API Seguro:**
    *   **Propósito:** Gestionar y utilizar de forma segura todas las claves de API de terceros.
    *   **Flujo:** El cliente realiza una solicitud al proxy. Este, a su vez, utiliza **OpenRouter** para encaminar la solicitud al modelo de IA más costo-efectivo o al especificado por el usuario (en el caso de BYOK). La clave de API de OpenRouter y las claves de usuario se gestionan de forma segura en el servidor, nunca en el cliente.

3.  **Servicio del Marketplace:**
    *   **Propósito:** Gestionar toda la lógica del mercado comunitario de activos.
    *   **Responsabilidades:** CRUD de activos, perfiles de creador, sistema de valoraciones y comentarios.

### 3.3. Modelo de Datos

*   **Cloud Firestore:**
    *   `users`: Perfiles, configuraciones, credenciales de terceros cifradas.
    *   `projects`: Metadatos de los proyectos de usuario.
    *   `builds`: Logs y estado de los trabajos de compilación remota.
    *   `marketplace_assets`: Metadatos de los activos del mercado.
    *   `asset_reviews`: Valoraciones y comentarios de los activos.
*   **Google Cloud Storage:**
    *   `project-source-files/`: Código fuente de los proyectos.
    *   `build-artifacts/`: APKs y AABs compilados.
    *   `marketplace-assets/`: Archivos descargables de los activos del mercado.

---

## 4. Pipeline de CI/CD: "Code-to-Cloud-to-Store"

Esta es la característica central que permite un flujo de trabajo de desarrollo profesional.

*   **Motor:** GitHub Actions.
*   **Flujo de Trabajo:**
    1.  **Disparo (Trigger):** Una acción del usuario en el IDE (ej: botón "Compilar") inicia un `git push` a un repositorio privado de GitHub.
    2.  **Compilación:** Un workflow de GitHub Actions se activa, configura un entorno de compilación de Android y ejecuta el comando de Gradle (`./gradlew bundleRelease`).
    3.  **Firma Segura:** El artefacto (`.aab`) se firma utilizando una clave de lanzamiento almacenada de forma segura en los Secretos de GitHub.
    4.  **Almacenamiento:** El artefacto firmado se sube a Google Cloud Storage.
    5.  **Publicación (Futuro):** El pipeline se extenderá para conectarse a la API de Google Play Developer para automatizar la publicación en la tienda.

---

## 5. Estrategia de Seguridad

*   **Gestión de Credenciales:** El **Proxy de API Seguro** es el único componente con acceso a las claves de API de servicios externos. Esto elimina el riesgo de exponer credenciales en el cliente. El modelo "Bring Your Own Key" (BYOK) permite a los usuarios utilizar sus propias claves, que se almacenan cifradas en Firestore y son utilizadas por el proxy en su nombre.
*   **Autenticación y Autorización:** Todas las solicitudes al backend son autenticadas mediante tokens de **Firebase Authentication**. El BFF y otros servicios autorizan las acciones basándose en el rol y los permisos del usuario.
*   **Protección del Cliente:** El APK del IDE implementará ofuscación de código y comprobaciones de integridad para dificultar la ingeniería inversa.

---

## 6. Hoja de Ruta Estratégica (Enfoque de Bajo Costo)

Esta arquitectura final se construirá de forma incremental, priorizando el valor para el desarrollador individual y minimizando los costos iniciales.

*   **Fase 1: MVP "Core" (Producto Mínimo Viable):** El objetivo es lanzar rápidamente un producto funcional que resuelva los problemas más urgentes.
    *   **Funcionalidades:** Editor de código robusto, compilación y depuración básicas, y las herramientas de IA de mayor impacto y menor costo: autocompletado (offline, con modelos locales) y un asistente de chat (usando las capas gratuitas de las APIs de IA).
    *   **Backend:** Desplegar la infraestructura mínima en Firebase para autenticación y almacenamiento de proyectos.

*   **Fase 2: Expansión y Gestión:** Una vez validado el MVP, se añadirán herramientas que aborden el ciclo de vida completo del proyecto.
    *   **Funcionalidades:** Integración de herramientas de gestión de proyectos (ej. tablero Kanban) dentro del IDE. Se añadirán funciones de IA más avanzadas como la depuración inteligente y la optimización de código, utilizando modelos de pago por uso a través de OpenRouter.

*   **Fase 3: Crecimiento y Monetización:** Introducción del modelo de negocio freemium.
    *   **Funcionalidades Premium:** Acceso ilimitado a la IA avanzada, herramientas de colaboración para equipos pequeños, análisis de rendimiento de la app y una funcionalidad de despliegue asistido que simplifique la publicación en Google Play.

*   **Fase 4: Ecosistema y Liderazgo de Mercado:** Construir las características que consoliden el producto como líder en su nicho.
    *   **Funcionalidades:** El mercado comunitario de activos, el diseñador visual de UI para Jetpack Compose y la funcionalidad "Device Mesh".

---

## 7. Estructura Detallada del Proyecto Final

Esta sección describe la organización de carpetas y archivos para los monorepos de frontend y backend, proporcionando una guía clara para la navegación y el desarrollo.

### 7.1. Estructura del Backend

El backend se organiza como un monorepo para facilitar la gestión de los diferentes microservicios y la infraestructura.

```
backend/
├── .gitignore
├── README.md
├── services/
│   ├── api-proxy/
│   │   ├── Dockerfile      # Conteneriza el servicio de proxy para su despliegue en Cloud Run.
│   │   ├── package.json    # Gestiona las dependencias (ej. express, axios).
│   │   └── src/            # Código fuente del proxy (Node.js/Express o similar).
│   ├── bff/
│   │   ├── Dockerfile      # Conteneriza el BFF para su despliegue.
│   │   ├── package.json    # Dependencias del Backend-for-Frontend.
│   │   └── src/            # Lógica de negocio del BFF.
│   └── marketplace-service/
│       ├── Dockerfile      # Conteneriza el servicio del mercado.
│       ├── package.json    # Dependencias del servicio del mercado.
│       └── src/            # Lógica de negocio del mercado.
├── terraform/
│   ├── .gitkeep
│   ├── cloud_run.tf        # Define los servicios de Cloud Run para el BFF, proxy, etc.
│   ├── firestore.tf        # Define las reglas y colecciones de Firestore.
│   ├── storage.tf          # Define los buckets de Cloud Storage y sus políticas.
│   └── variables.tf        # Variables de configuración para los entornos (dev, prod).
└── user-project-template/
    └── .github/
        └── workflows/
            └── android-publish.yml # Plantilla del pipeline de CI/CD que se copiará
                                    # a cada nuevo proyecto de usuario en GitHub.
```

### 7.2. Estructura del Frontend

El frontend sigue una arquitectura multi-módulo de Android, donde cada capa y cada funcionalidad están encapsuladas en su propio módulo Gradle.

```
frontend/
├── build.gradle.kts        # Script de compilación a nivel de raíz.
├── settings.gradle.kts     # Define todos los módulos incluidos en el proyecto.
├── app/
│   ├── build.gradle.kts
│   └── src/main/           # Punto de entrada de la aplicación, orquesta la navegación.
│       └── AndroidManifest.xml
├── core/                   # Módulos con código compartido y sin lógica de negocio.
│   ├── api/                # Define las interfaces (ej. `ProjectRepository`) y DTOs.
│   ├── network/            # Cliente de red centralizado (ej. Ktor/Retrofit).
│   ├── storage/            # Abstracciones para el acceso al almacenamiento local.
│   ├── ui/                 # Componentes de Jetpack Compose reutilizables, temas, fuentes.
│   └── utils/              # Clases de utilidad (ej. formateadores, validadores).
├── data/                   # Implementación de las interfaces definidas en :core:api.
│   ├── build.gradle.kts
│   └── src/main/java/
│       └── .../data/
│           ├── project/    # Implementación de `ProjectRepository`, interactúa con
│           │               # el sistema de archivos y la base de datos local.
│           └── ai/         # Implementación de repositorios de IA, se comunica con
│                           # el BFF y gestiona los modelos locales de TFLite.
├── domain/                 # Lógica de negocio pura (casos de uso), independiente de Android.
│   ├── build.gradle.kts
│   └── src/main/java/
│       └── .../domain/
│           ├── project/
│           │   ├── model/      # Modelos de dominio (ej. `Project`, `Task`).
│           │   └── usecase/    # Casos de uso (ej. `CreateNewProjectUseCase`).
│           └── ai/
│               └── usecase/    # Casos de uso de IA (ej. `GenerateCodeUseCase`).
└── features/               # Módulos correspondientes a cada pantalla o funcionalidad.
    └── editor/             # Ejemplo de un módulo de funcionalidad.
        ├── build.gradle.kts
        └── src/main/java/
            └── .../editor/
                ├── state/      # Clases que representan el estado de la UI (ej. `EditorState`).
                ├── view/       # Composables de la pantalla del editor.
                └── viewmodel/  # ViewModel que gestiona la lógica de la UI y el estado.
```
