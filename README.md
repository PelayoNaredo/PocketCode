# PocketCode

Diseño IDE Android Nativo

> Consulta `ARCHITECTURE.md` para la guía unificada de arquitectura, sistema de
> diseño, roadmap y componentes.

AndroidCode IDE: Informe de Diseño Técnico y Estrategia de Producto

1. Introducción y Filosofía Central 1.1. Resumen Ejecutivo Este informe detalla
   el diseño técnico y estratégico integral para "AndroidCode IDE", una
   aplicación nativa para Android diseñada para proporcionar un entorno de
   desarrollo completo y móvil. La filosofía central de la plataforma es delegar
   las tareas más exigentes, como la compilación de código, a un sistema de
   infraestructura en la nube sin servidores. Este enfoque transforma el
   dispositivo móvil de un simple editor de código a un centro de desarrollo
   integral y portátil. La arquitectura se fundamenta en principios modernos,
   escalables y seguros, utilizando una Arquitectura Limpia modular en el
   frontend, un Backend for Frontend (BFF) sin servidores en la nube y un sólido
   pipeline de integración y entrega continua (CI/CD) integrado directamente en
   la experiencia de usuario. Este diseño aborda las limitaciones intrínsecas
   del hardware móvil al tiempo que proporciona un flujo de trabajo de
   desarrollo ágil y de grado empresarial.

1.2. La Visión de AndroidCode IDE: Empoderando el Desarrollo Nativo-Móvil La
visión de AndroidCode IDE es llenar una brecha crítica en el mercado de
herramientas para desarrolladores. Si bien las soluciones existentes en Android
a menudo se limitan a la edición de texto o sufren de problemas de rendimiento,
AndroidCode IDE busca replicar la potencia total de un IDE de escritorio como
Android Studio, pero optimizado para el factor de forma móvil. El público
objetivo es una nueva generación de desarrolladores que desean la flexibilidad
de programar, gestionar proyectos y desplegar aplicaciones desde cualquier
lugar, directamente desde sus dispositivos Android de alto rendimiento. Este es
un producto diseñado para un futuro en el que la computación ya no se limita al
escritorio.

1.3. Principios Fundamentales de la Arquitectura El diseño se basa en tres
principios fundamentales que guían todas las decisiones técnicas y de producto:

Aumento con la Nube: Si bien la funcionalidad principal del IDE es local, todas
las tareas pesadas, como la compilación, la depuración y las tareas asistidas
por IA, se ejecutarán en una infraestructura en la nube sin servidores,
escalable y flexible. Este enfoque garantiza que la interfaz de usuario de la
aplicación siga siendo rápida y fluida. Escalabilidad y Mantenimiento: El código
se estructurará siguiendo los principios de la Arquitectura Limpia y de una
arquitectura multi-módulo. Este enfoque es crucial para garantizar la
escalabilidad a largo plazo, mejorar los tiempos de compilación del proyecto y
facilitar la colaboración entre equipos de desarrollo, permitiendo una clara
separación de responsabilidades. Diseño que Prioriza la Seguridad: Los datos
sensibles, en particular las claves de API y las credenciales de usuario, nunca
se almacenarán en el cliente. Un proxy en el servidor actuará como intermediario
para todas las interacciones sensibles en la nube, garantizando que el usuario y
sus datos estén protegidos contra posibles vulnerabilidades. 2. Arquitectura del
Frontend: El IDE Nativo 2.1. Interfaz y Experiencia de Usuario (UI/UX) La UI/UX
priorizará un diseño responsivo e intuitivo que se adapte a diversas formas de
dispositivos Android, desde teléfonos hasta tabletas y dispositivos plegables.
Se utilizará Jetpack Compose como el conjunto de herramientas declarativas para
la UI, lo que permitirá la creación de diseños dinámicos y una experiencia de
usuario consistente y de alta calidad. El diseño incorporará las mejores
prácticas de editores móviles existentes como Acode y Spck, incluyendo una
interfaz amigable, múltiples temas y un navegador de archivos responsivo. A
futuro, se planea expandir la personalización permitiendo a los usuarios
reorganizar el espacio de trabajo con paneles acoplables y acceder a un mercado
de temas creados por la comunidad.

2.2. Funcionalidades Centrales del IDE: Un Análisis Profundo El IDE nativo será
un entorno con una rica variedad de características, diseñado para replicar las
funcionalidades de un IDE de escritorio:

Editor de Código Inteligente y Avanzado: La base de la aplicación será un editor
robusto que ofrecerá resaltado de sintaxis para una amplia gama de lenguajes.
Incluirá una función de autocompletado inteligente y se mejorará con
herramientas de refactorización avanzadas (como "extraer método") y soporte para
configuraciones de teclas personalizadas (keybindings) como Vim y Emacs, para
atraer a un público más amplio de desarrolladores. Gestión de Proyectos y
Archivos: El IDE admitirá la estructura de proyecto estándar de Android
(manifests, java, res) y los archivos de compilación de Gradle. Una
característica clave será la capacidad de abrir y gestionar proyectos
multi-módulo completos, lo cual es fundamental para el desarrollo de
aplicaciones a gran escala. Una debilidad conocida de los editores móviles
existentes como Spck es su incapacidad para compartir fácilmente un espacio de
trabajo con otras aplicaciones de desarrollo como Termux debido a las
restricciones de Scoped Storage de Android. AndroidCode IDE abordará esto
directamente implementando una capa de gestión de archivos personalizada que
proporcione una ubicación de espacio de trabajo "verdaderamente compartida y
accesible", permitiendo una interoperabilidad fluida con otras herramientas en
el dispositivo. Esto constituye una ventaja competitiva al resolver un problema
conocido del usuario. Herramientas Integradas: El IDE contará con una terminal
integrada , un cliente de Git para el control de versiones y un depurador con
capacidades de depuración en línea. Si bien las herramientas de perfilado de
Android Studio son complejas y se utilizan para la depuración profunda, se
incluirá un monitor de rendimiento más ligero en el dispositivo para el uso de
memoria y CPU. Sistema de Compilación Remota: El IDE utilizará Gradle como base
para el sistema de compilación. Sin embargo, el proceso de compilación real se
delegará a un pipeline de CI/CD basado en la nube. La razón de esta decisión
fundamental es que la ejecución de una compilación completa de Gradle en un
dispositivo móvil es una tarea computacionalmente costosa, consume mucho tiempo
y agota la batería. El resultado es una experiencia de usuario lenta y
frustrante. Se observó que los editores móviles existentes carecen de esta
funcionalidad o son lentos, mientras que los IDE de escritorio como Android
Studio, basados en IntelliJ IDEA, son muy potentes pero requieren muchos
recursos. Un análisis de las capacidades de las acciones de GitHub, una
herramienta de CI/CD, revela su capacidad para ejecutar compilaciones en un
servidor remoto de manera transparente. Los recursos de investigación
proporcionan guías detalladas sobre cómo compilar y firmar paquetes de
aplicaciones de Android (.aab) de forma remota utilizando el comando gradlew
bundleRelease. Esto demuestra que la solución ideal no es compilar localmente,
sino que el botón "Compilar" en AndroidCode IDE debe activar un proceso de
subida de código a un repositorio remoto, lo que a su vez activa una compilación
en la nube. Una vez completada, la aplicación simplemente descarga el artefacto
resultante (APK/AAB). Esto libera al dispositivo de la tarea más exigente,
garantizando una interfaz de usuario rápida y responsiva, y preservando la
duración de la batería. 2.3. Estructura del Código: Un Enfoque Multi-Módulo y
por Capas La arquitectura de la aplicación será un diseño multi-módulo y por
capas basado en los principios de la Arquitectura Limpia. Esto no es solo una
buena práctica, sino una necesidad para un proyecto de esta magnitud, ya que
promueve la escalabilidad y la capacidad de prueba. La arquitectura se
organizará para lograr una alta cohesión y un bajo acoplamiento, lo que
significa que los módulos serán independientes y tendrán una responsabilidad
claramente definida, lo que limita el efecto de los cambios en el código.

La arquitectura se dividirá en tres capas con una dirección de dependencia
unidireccional, siempre apuntando hacia el centro :

Capa de Presentación (:ui, :features): Esta capa gestiona los componentes de la
UI, la gestión del estado y la lógica de la vista. Las pantallas del editor, el
explorador de proyectos y la configuración del IDE se gestionarán como módulos
específicos de cada característica. Esta capa dependerá de la capa de Dominio.
Capa de Dominio (:domain): Es la capa central que contiene la lógica de negocio,
las entidades y los casos de uso. Es completamente independiente de cualquier
marco de trabajo o biblioteca externa, lo que la convierte en la parte más
estable de la arquitectura. Esta capa definirá interfaces para tareas como
CodeGenerationUseCase o ProjectManagementRepository, pero no dependerá de la
capa de Datos ni de la de Presentación. Capa de Datos (:data): Esta capa
implementa las interfaces de los repositorios definidas en la capa de Dominio.
Maneja todas las interacciones con las API, las bases de datos locales y los
sistemas de archivos, actuando como la fuente única de la verdad para los datos.
La siguiente tabla ilustra la estructura modular recomendada para el proyecto,
detallando la responsabilidad de cada módulo y sus dependencias.

Tabla 1: Módulos Gradle y Dependencias Recomendadas Módulo Gradle

Tipo de Módulo

Responsabilidad Clave

Dependencias

app

App

Punto de entrada, orquesta la navegación, contiene el AndroidManifest.xml

:features:editor, :features:project, etc.

:features:editor

Biblioteca de Android

Lógica de UI para el editor de código, editor de texto, resaltado de sintaxis,
etc.

:domain:ide, :core:utils

:features:project

Biblioteca de Android

Lógica de UI para la gestión de proyectos, explorador de archivos,
sincronización de Git, etc.

:domain:project, :core:utils

:domain:ide

Biblioteca de Kotlin

Lógica de negocio central del IDE (casos de uso de generación de código,
refactorización, etc.)

:domain:project, :core:api

:domain:project

Biblioteca de Kotlin

Lógica de negocio central para la gestión de proyectos (casos de uso de
archivos, modelos de datos, etc.)

:core:api

:data:ide

Biblioteca de Kotlin

Implementa los repositorios del IDE, gestiona las llamadas a la API de IA.

:domain:ide, :core:network

:data:project

Biblioteca de Kotlin

Implementa los repositorios de proyectos, gestiona el acceso a la base de datos
y al sistema de archivos local.

:domain:project, :core:storage

:core:utils

Biblioteca de Kotlin

Utilidades de uso general y funciones auxiliares (validadores, etc.).

-

:core:network

Biblioteca de Kotlin

Cliente HTTP compartido y configuración para las llamadas a la API.

-

:core:storage

Biblioteca de Kotlin

Clases de abstracción para el acceso al almacenamiento (filesystem).

-

:core:api

Biblioteca de Kotlin

Interfaces y modelos de datos para la comunicación entre capas.

- 2.4. Adopción de Herramientas de Desarrollo Modernas: Jetpack Compose y MVI
  Jetpack Compose es la opción recomendada para la UI, ya que ofrece un enfoque
  moderno, reactivo y declarativo. El estado de la UI se gestionará utilizando
  el patrón de arquitectura Modelo-Vista-Intención (MVI), que proporciona un
  flujo de datos unidireccional y robusto que es altamente compatible con
  Compose. Esta arquitectura es ideal para gestionar los complejos estados de la
  UI de un IDE, donde las interacciones pueden producir efectos en cascada en
  toda la interfaz. La implementación se basará en los tres componentes
  centrales de MVI: State (un objeto que representa el estado actual de la
  pantalla), Event (objetos que reflejan las acciones del usuario) y Effect
  (acciones puntuales que modifican la UI, como mostrar un Toast o iniciar una
  navegación). Este enfoque garantiza una base de código predecible y altamente
  comprobable.

3. Backend e Infraestructura en la Nube 3.1. El Patrón Backend for Frontend
   (BFF): Razón e Implementación Dado que AndroidCode IDE es una aplicación
   diseñada principalmente para el entorno móvil, el patrón de arquitectura
   Backend for Frontend (BFF) es la elección ideal. Esta capa servirá como
   intermediaria entre el cliente móvil y los servicios de backend principales
   (proveedores de IA, APIs de Git, etc.). El BFF agregará y transformará los
   datos para proporcionar respuestas ligeras y optimizadas, adaptadas
   específicamente a las necesidades del cliente móvil, lo cual es fundamental
   para el rendimiento en redes móviles. Al manejar interacciones complejas con
   múltiples APIs, el BFF también reducirá la lógica necesaria en el cliente,
   simplificando la base de código y permitiendo al equipo de desarrollo
   centrarse en las características únicas del IDE en el frontend.

3.2. Computación sin Servidor: Cloud Run vs. Firebase Functions Un modelo sin
servidores es la elección lógica para un backend escalable y de pago por uso. Si
bien tanto Firebase Functions como Google Cloud Run son soluciones sin
servidores, la investigación proporciona un camino claro a seguir. Firebase
Functions (1.ª generación) es excelente para tareas sencillas y basadas en
eventos, pero Cloud Run ofrece una flexibilidad superior, concurrencia mejorada
y tiempos de procesamiento de solicitudes más largos. Un hallazgo crucial es que
la segunda generación de Firebase Functions, recomendada por Google, está ahora
construida sobre la infraestructura de Cloud Run. Esta evolución es una clara
indicación de que para cargas de trabajo modernas, que requieren alta
concurrencia y tiempos de ejecución prolongados, Cloud Run es la solución
preferida y a prueba de futuro. Los tiempos de procesamiento más largos y la
concurrencia mejorada de Cloud Run abordan directamente las necesidades de un
backend de IDE, que manejará tareas complejas y potencialmente de larga
duración, como las compilaciones remotas de Gradle o la generación de código a
gran escala con IA. La recomendación es implementar el backend utilizando
Firebase Functions (2.ª generación), ya que esta opción combina el ecosistema
amigable y fácil de usar de Firebase con la potencia y escalabilidad de la
infraestructura basada en contenedores de Cloud Run.

3.3. Almacenamiento Persistente y de Datos en Tiempo Real: Firestore y Cloud
Storage Cloud Firestore: Firestore se utilizará para el almacenamiento de datos
estructurados y persistentes, como perfiles de usuario, metadatos de proyectos y
registros de compilación. Su modelo de pago por uso, con cargos por cada
lectura, escritura y eliminación de documentos, lo hace rentable para una base
de usuarios inicial. La base de datos ofrece una cuota gratuita de 1 GiB de
datos almacenados, así como 50.000 lecturas, 20.000 escrituras y 20.000
eliminaciones por día, lo que es ideal para la fase de inicio del proyecto.
Cloud Storage: Los archivos de proyectos de usuario (código fuente, activos) y
los artefactos de compilación (APKs, AABs) se almacenarán en Google Cloud
Storage. Se utilizarán diferentes clases de almacenamiento (Standard, Nearline)
para optimizar los costos en función de la frecuencia de acceso, por ejemplo,
los proyectos activos se almacenarían en la clase Standard, que es más costosa
pero optimizada para un acceso frecuente. Sincronización en Tiempo Real: Una
característica clave será la sincronización de datos en tiempo real entre el
dispositivo móvil y la nube. Esto se implementará utilizando los oyentes de
tiempo real de Firestore para pequeños cambios en los datos (por ejemplo, la
configuración del proyecto) y un robusto mecanismo de sincronización para los
archivos de código fuente. 3.4. Acceso Seguro a la API con una Capa de Proxy
Todas las interacciones con APIs sensibles, especialmente con modelos de IA, se
canalizarán a través de un proxy seguro en el servidor. Esta es una medida de
seguridad fundamental para evitar que las credenciales queden expuestas en el
lado del cliente. La inclusión de claves de API en el código de una aplicación
cliente representa un riesgo de seguridad crítico, ya que un actor
malintencionado podría aplicar ingeniería inversa a la aplicación, robar la
clave y utilizarla para solicitudes no autorizadas, lo que podría generar cargos
inesperados.

La solución es implementar una capa de proxy en Cloud Run. El cliente móvil se
autenticará con el proxy, que luego añadirá de forma segura la clave de API
necesaria a la solicitud antes de reenviarla a los servicios de IA u otros
servicios externos. El IDE en sí no tendrá conocimiento de la clave de API. El
proxy puede ser más que una capa de seguridad. Los servicios de proxy
gestionados como AIProxy y Braintrust ofrecen características adicionales como
el almacenamiento en caché automático para reducir costos, el monitoreo de
solicitudes para la observabilidad y la posibilidad de modificar los modelos
para realizar pruebas A/B sencillas. El informe recomienda construir un proxy
autogestionado en Cloud Run para mantener el control total, aunque un servicio
gestionado podría ser una excelente alternativa para equipos que priorizan la
velocidad de implementación sobre la personalización.

4. Seguridad y Gestión de Credenciales 4.1. Los Peligros de las Claves de API en
   el Lado del Cliente Esta sección se basa en los principios de diseño de
   seguridad de la capa de proxy, citando prácticas recomendadas de Google y
   OpenAI. Se establece explícitamente que las claves de API son "credenciales
   de portador" y nunca deben codificarse ni incluirse en repositorios de
   código. La razón principal es el riesgo de que la clave sea interceptada y
   utilizada para acceder a datos no autorizados, lo que puede llevar a cargos
   inesperados y a la pérdida del rastro de auditoría. Los métodos alternativos,
   como el uso de variables de entorno o un gestor de secretos, solo son seguros
   en un entorno de desarrollo o en un servidor, pero no en una aplicación
   móvil, donde el código es fácilmente accesible para los usuarios.

4.2. Estrategias para la Gestión de Claves de API de IA y la Nube Se han
analizado múltiples estrategias para gestionar las claves de API, y la siguiente
tabla compara los enfoques más comunes, lo que lleva a una recomendación clara.

Tabla 2: Soluciones para la Gestión de Claves de API de IA y la Nube Método de
Gestión

Ventajas

Desventajas

Escenario de Uso Recomendado

Código del Cliente

Fácil de implementar, sin dependencias de backend.

CRÍTICO: Mayor riesgo de exposición y robo. NO RECOMENDADO para apps de
producción.

Solo para prototipos muy básicos o de desarrollo local.

Variables de Entorno/Archivos de Secretos Locales

Mantiene la clave fuera del repositorio, sigue las prácticas de seguridad
comunes.

Las claves siguen estando en el dispositivo del usuario; pueden ser extraídas si
el dispositivo es vulnerable.

Entornos de desarrollo local.

Gestor de Secretos en la Nube (p. ej., Google Secret Manager)

Centraliza las credenciales, permite la rotación de claves, auditable.

Requiere que el cliente se autentique en el gestor para acceder a la clave. El
token de autenticación del cliente puede ser vulnerable.

Microservicios de backend.

Proxy en el Servidor

Mejor Seguridad: Las claves nunca abandonan el entorno del servidor. Control
Total: Permite la gestión de la autenticación, la monitorización, el
almacenamiento en caché y la modificación de los modelos sin actualizar la
aplicación del cliente.

Mayor complejidad, costos de infraestructura, punto único de fallo potencial si
no se gestiona bien.

MÉTODO RECOMENDADO para aplicaciones móviles de producción.

La recomendación es el proxy en el servidor. Un análisis comparativo muestra
que, si bien otros métodos ofrecen seguridad incremental, solo un proxy en el
lado del servidor garantiza que la clave de API nunca resida en un entorno no
confiable como el dispositivo del usuario, previniendo el robo y el uso no
autorizado.

4.3. Almacenamiento y Gestión Segura de los Datos del Usuario La gestión de los
datos del usuario seguirá el principio del menor privilegio. Las credenciales y
configuraciones específicas del usuario se almacenarán en Firestore, accesible
solo después de que el usuario se haya autenticado, y nunca se almacenarán en el
almacenamiento no seguro del dispositivo local. Las credenciales de terceros,
como las claves de API de Gemini, se cifrarán y almacenarán de forma segura en
Firestore, lo que permitirá que el proxy en el servidor las utilice para el
usuario. Los datos sensibles deben cifrarse tanto en tránsito como en reposo
para garantizar la máxima seguridad.

4.4. Protección del Núcleo del IDE: Endurecimiento Binario y Detección de
Manipulaciones Más allá de la seguridad de los datos, el IDE nativo implementará
medidas adicionales para evitar la manipulación. Esto incluye la ofuscación
binaria y la adición de comprobaciones de integridad del código para detectar si
un usuario malicioso ha modificado el comportamiento del IDE. Este enfoque de
seguridad en múltiples capas es crucial para una herramienta de desarrollo, ya
que el código del IDE podría ser un objetivo atractivo para la ingeniería
inversa.

5. El Pipeline de CI/CD "Código a la Nube" Esta es la característica distintiva
   que diferencia a AndroidCode IDE de un simple editor. El diseño utiliza las
   Acciones de GitHub para crear un pipeline robusto y automatizado.

Acciones de GitHub como Motor: Las Acciones de GitHub son una opción ideal para
este pipeline debido a su perfecta integración con los repositorios de Git y su
modelo de precios gratuito, lo que lo hace rentable para equipos de todos los
tamaños. El Flujo de Trabajo de Compilación: El pipeline de CI se activará en
cada evento git push a la rama principal. Un archivo .yml dentro del directorio
.github/workflows del proyecto definirá los pasos, que se ejecutarán en una
máquina virtual de Ubuntu. El flujo de trabajo típico incluirá la descarga del
código, la configuración del JDK correcto, la concesión de permisos de ejecución
a Gradle (chmod +x gradlew) y la ejecución del comando de Gradle para compilar
el archivo .aab (./gradlew bundleRelease). Firma Segura y Automatización del
Lanzamiento: La firma de la aplicación para su lanzamiento requiere una clave
privada que debe mantenerse en secreto. Almacenar esta clave en un repositorio
público es un enorme riesgo de seguridad. Para resolver esto, la clave de firma
se almacenará como un Secreto de GitHub, codificada en Base64. El flujo de
trabajo de CI/CD utilizará una acción dedicada de GitHub
(r0adkll/sign-android-release@v1) para recuperar de forma segura la clave y
firmar la aplicación, eliminando la necesidad de que la clave exista en el
dispositivo del usuario o en el repositorio público. Gestión de Despliegues y
Publicación Automatizada: El pipeline no solo se encargará de la compilación y
firma. Se optimizará con cachés de compilación remota para acelerar los tiempos
de construcción y se extenderá para gestionar el proceso de despliegue completo
en la Google Play Store. Esto incluye la subida del artefacto y la
automatización de la gestión de lanzamientos y notas de versión, convirtiéndolo
en un sistema de publicación totalmente automatizado. 6. Monetización y
Estrategia de Negocio 6.1. El Modelo Freemium para Herramientas de Desarrollo El
modelo freemium es el estándar para las herramientas de desarrollo y es
perfectamente adecuado para AndroidCode IDE. Reduce la barrera de entrada, atrae
a una gran base de usuarios y sienta las bases para futuros ingresos. La clave
para un modelo freemium exitoso es definir una distinción clara entre las
características gratuitas y las premium, garantizando que la versión gratuita
sea lo suficientemente valiosa como para que el usuario la utilice, pero lo
suficientemente limitada como para fomentar las actualizaciones.

6.2. Definición del Conjunto de Características "Gratuito" vs. "Premium" La
distinción se basará en las capacidades nativas de la nube, no en las
características centrales del IDE, ya que estas últimas son la propuesta de
valor principal.

Nivel Gratuito: El IDE central (edición local, Git básico, terminal y gestión de
proyectos) es gratuito para siempre. Incluirá una pequeña asignación mensual de
recursos basados en la nube, como solicitudes de generación de código con IA y
minutos de compilación de CI/CD. Esto funciona como una "prueba sin límite de
tiempo," similar a los modelos utilizados por GitHub y Visual Studio Code. Nivel
Premium: El nivel "Pro" desbloqueará la verdadera potencia de la plataforma. IA
Avanzada: Ofrecerá autocompletado de código asistido por IA ilimitado, acceso a
modelos más potentes y funciones avanzadas como la revisión de código y la
asistencia de depuración. Minutos de Compilación en la Nube: Proporcionará una
cuota mensual mayor o ilimitada de minutos de compilación en las Acciones de
GitHub. Colaboración: Incluirá funciones para el trabajo en equipo, como ramas
protegidas, múltiples revisores y entornos compartidos, replicando los planes de
pago de GitHub. Modelo de "Trae Tu Propia Clave" (BYOK): Un punto clave en la
estrategia de monetización es la implementación de un modelo BYOK. Los usuarios
que ya tienen sus propias claves de API para Gemini o OpenAI podrán usarlas en
el proxy del IDE para evitar los límites de créditos de IA. Este enfoque
flexible y centrado en el usuario atiende a los desarrolladores profesionales
que ya cuentan con una suscripción de pago con un proveedor de IA. Gestión de
Suscripciones: Se recomienda utilizar un servicio especializado como RevenueCat
para gestionar las suscripciones y los pagos dentro de la aplicación, ya que
simplifica el proceso, automatiza las validaciones de recibos y proporciona
análisis valiosos para la estrategia de crecimiento. 7. Análisis Financiero y
Proyecciones de Costos 7.1. Modelado de Costos de la Infraestructura en la Nube
Se proyectarán los costos de la infraestructura en la nube basados en el
principio de que el "costo en la nube = uso x precio unitario". Los costos
dependerán en gran medida del comportamiento del usuario, especialmente del
número de compilaciones de CI/CD y solicitudes de IA. El análisis utilizará el
modelo de pago por uso de Google, destacando los beneficios de las capas
gratuitas para el escalado inicial, ya que muchos servicios de Google Cloud
ofrecen una cuota sin costo.

7.2. Desglose de Costos de los Servicios Clave Se proporciona un desglose
detallado de los costos para los servicios principales, basados en la
información disponible.

Cloud Run: Los costos se determinarán principalmente por el uso de CPU y memoria
durante los procesos de compilación, así como por las solicitudes al proxy BFF.
Los 2 millones de solicitudes gratuitas y las generosas cuotas de CPU/RAM sin
costo se identifican como una ventaja significativa para el crecimiento inicial,
lo que permite que el producto crezca sin incurrir en grandes gastos. Firestore:
Los costos se estimarán en función del número de lecturas, escrituras y
eliminaciones por cada usuario activo. La investigación proporciona modelos de
costos para 5.000 y 100.000 usuarios activos diarios (DAU). Se utilizará esta
información para extrapolar un modelo de costos para un objetivo de 1.000 DAU,
lo que permite una proyección financiera realista. Cloud Storage: Los costos
estarán vinculados a la cantidad de datos almacenados (proyectos de usuario,
artefactos de compilación) y a la transferencia de datos. Se destaca la cuota de
5 GB de almacenamiento estándar y 100 GB de transferencia de datos gratuita como
un beneficio para la etapa de adopción temprana. Cuota Gratuita de Google Cloud:
Se enfatiza que los nuevos clientes reciben 300 dólares en créditos gratuitos,
lo que proporciona un sólido capital inicial para la ejecución del proyecto.
7.3. Estimación de Costos Mensuales de Infraestructura (por 1.000 DAU) La
siguiente tabla resume la proyección de costos, aplicando la lógica del modelo
de pago por uso a un escenario realista de 1.000 usuarios activos. Este análisis
transforma los datos brutos en una inteligencia empresarial estratégica y
práctica, lo que permite la planificación financiera.

Tabla 3: Costos Estimados Mensuales de Infraestructura (por 1.000 DAU) Servicio
de Google Cloud

Modelo de Costo por Uso (Ejemplo)

Proyección para 1.000 DAU

Costo Estimado Mensual

Cloud Run

$0.000024/vCPU-segundo; $0.40/millón de solicitudes

Se asume que 1.000 usuarios generan 5.000 solicitudes/día para el proxy y 100
compilaciones/día (promedio).

Costos cubiertos por la capa gratuita inicial (2M solicitudes gratis). Costo
adicional marginal.

Firestore

$0.06/100K lecturas; $0.18/100K escrituras

Extrapolado del modelo para 50K DAU. Lecturas y escrituras proporcionales al
uso.

Aproximadamente $0.25/mes, en su mayoría cubiertos por la cuota gratuita diaria.

Cloud Storage

$0.020/GB/mes (estándar)

Se asume 100 MB de datos por usuario (proyectos y artefactos). 100 GB en total.

Cubierto por la cuota gratuita de 5 GB, con un costo adicional mínimo si se
supera.

CI/CD (GitHub Actions)

2.000 minutos de compilación/mes gratis (repos públicos). Para privados, se
aplican los costos.

Se asume 100 compilaciones/día de 2 minutos cada una (6.000 minutos/mes).

El modelo freemium cubriría el exceso con minutos de pago.

Total Proyectado (bajo)

Aproximadamente $5-$15/mes (dependiendo del uso de recursos pagados y más allá
del nivel gratuito)

8. Conclusión y Hoja de Ruta Estratégica 8.1. Síntesis de Decisiones
   Arquitectónicas La arquitectura de AndroidCode IDE se ha diseñado
   meticulosamente para superar las limitaciones de la computación móvil. Cada
   decisión ha sido evaluada para crear una plataforma que sea robusta, segura y
   escalable. Las decisiones clave incluyen:

Un frontend construido con Jetpack Compose y el patrón MVI para una UI moderna y
predecible. Una Arquitectura Limpia multi-módulo que garantiza la mantenibilidad
y la colaboración en un proyecto a gran escala. Un enfoque nativo de la nube que
descarga las tareas pesadas al backend sin servidores. Un backend basado en
Cloud Run para una escalabilidad, concurrencia y rentabilidad superiores. Una
capa de proxy para la seguridad de las API, que también agrega funciones
valiosas como el almacenamiento en caché. Un pipeline de CI/CD automatizado con
las Acciones de GitHub para un flujo de trabajo "código a la nube" fluido. Un
modelo de negocio freemium que monetiza las funciones de la nube y ofrece una
opción BYOK, lo que reduce la barrera de entrada para los usuarios. 8.2. Un Plan
de Implementación por Fases Se propone una hoja de ruta por fases para guiar el
desarrollo desde un producto mínimo viable (MVP) hasta la visión completa.

Fase 1 (MVP): Concentrarse en el núcleo del IDE nativo y local. Esto incluye el
editor de código, la gestión de proyectos y archivos, un terminal funcional y la
integración con un cliente de Git básico. Fase 2 (Integración en la Nube):
Implementar el pipeline de compilación en la nube (la funcionalidad de Git a las
Acciones de GitHub) y la capa de proxy de IA básica. En esta fase, los usuarios
podrán compilar y utilizar las funciones de IA por primera vez. Fase 3
(Monetización y Escalamiento): Integrar la capa de gestión de suscripciones (p.
ej., RevenueCat) y desplegar las funciones premium y los precios por niveles.
8.3. Un IDE a Prueba de Futuro La arquitectura seleccionada está diseñada para
el futuro, lo que permite una fácil integración de nuevas tecnologías (como
nuevos modelos de IA, nuevas herramientas de compilación o APIs de terceros) sin
necesidad de una gran reestructuración. Al desacoplar el cliente móvil del
complejo entorno de backend y al adoptar un enfoque modular, la plataforma puede
evolucionar y adaptarse a un panorama tecnológico en constante cambio, lo que
garantiza la viabilidad y el éxito a largo plazo de AndroidCode IDE.

9. Visión Futura y Hoja de Ruta Avanzada Para consolidar a PocketCode como el
   líder indiscutible en el desarrollo móvil, la hoja de ruta incluye varias
   características innovadoras que transformarán el flujo de trabajo del
   desarrollador:

- **Asistente de Desarrollo Impulsado por IA:** Más allá de la simple
  finalización de código, se creará un asistente de IA capaz de generar módulos
  de código completos a partir de descripciones en lenguaje natural, sugerir
  mejoras arquitectónicas y realizar revisiones de código automatizadas para
  detectar errores, problemas de rendimiento y vulnerabilidades de seguridad.
- **Diseñador Visual de UI para Jetpack Compose:** Se integrará una herramienta
  WYSIWYG (Lo que ves es lo que obtienes) que permitirá a los desarrolladores
  diseñar interfaces de usuario mediante un sistema de arrastrar y soltar,
  generando automáticamente código Jetpack Compose limpio e idiomático. Esto
  reducirá drásticamente la barrera de entrada para el diseño de UI.
- **Entorno de Desarrollo "Device Mesh":** Se implementará una funcionalidad que
  permitirá a los desarrolladores conectar múltiples dispositivos Android en un
  único espacio de trabajo cohesivo. Por ejemplo, utilizar una tableta como
  editor principal mientras un teléfono muestra una vista previa en vivo de la
  UI y otro dispositivo transmite los registros (Logcat).
- **Mercado Comunitario de Activos y Módulos:** Se construirá una plataforma
  integrada donde los desarrolladores podrán publicar, compartir, comprar y
  vender plantillas de proyectos, componentes de UI reutilizables y módulos
  funcionales completos. Esto fomentará un ecosistema vibrante alrededor de la
  aplicación y acelerará el desarrollo para todos los usuarios.

10. Sistema de Diseño y Componentes Reutilizables (Resumen) El núcleo visual de
    PocketCode ya está consolidado en los módulos `:core:ui` y
    `:core:ui:tokens`. Para mantener la coherencia sin depender de documentación
    adicional:

- **Principios clave:** Minimalismo, claridad, eficiencia y gestos naturales en
  todas las interacciones.
- **Paleta base:**
  - Tema claro — `primary` `#007AFF`, `secondary` `#8E8E93`, `background`
    `#F2F2F7`, `surface` `#FFFFFF`, `success` `#34C759`, `error` `#FF3B30`.
  - Tema oscuro — `primary` `#0A84FF`, `secondary` `#8E8E93`, `background`
    `#000000`, `surface` `#1C1C1E`, `success` `#30D158`, `error` `#FF453A`.
- **Tipografía y espaciados:** Escala basada en `Inter`/`Roboto` con jerarquía
  `h1 34px`, `h2 28px`, `h3 22px`, `body 17px`, y sistema de espaciado múltiplo
  de 8px (`4-48px`).
- **Buenas prácticas:** Reutilizar componentes existentes (PocketButton,
  PocketCard, PocketTextField, etc.), evitar valores hardcodeados y documentar
  variantes en los módulos correspondientes.

Para detalles operativos, el código fuente de los tokens se encuentra en
`frontend/core/ui/tokens/` y los componentes reutilizables en
`frontend/core/ui/components/`.
