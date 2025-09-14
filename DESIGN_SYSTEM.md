# Guía de Diseño del Frontend

## 1. Filosofía y Principios de Diseño

Nuestra filosofía de diseño se centra en la simplicidad, la claridad y la eficiencia. Cada elemento de la interfaz debe tener un propósito claro y ser fácil de entender. Nos guiaremos por los siguientes principios:

-   **Minimalismo:** Eliminamos cualquier elemento superfluo que no aporte valor al usuario. Menos es más. El diseño debe ser limpio y centrado en el contenido.
-   **Experiencia de Usuario (UX) Intuitiva:** La navegación y las acciones deben ser predecibles y naturales. El usuario no debería necesitar un manual para entender cómo funciona la app.
-   **Simplicidad y Claridad:** La información se presentará de forma clara y concisa. Las acciones complejas se dividirán en pasos sencillos.
-   **Eficiencia:** El diseño debe permitir a los usuarios completar sus tareas de la forma más rápida y fluida posible. Aquí es donde los gestos juegan un papel crucial.

---

## 2. Sistema de Temas (Theming)

La aplicación contará con dos temas, Claro y Oscuro, para adaptarse a las preferencias del usuario y a las condiciones de iluminación. El sistema se basará en un conjunto de variables (tokens) de diseño que se aplicarán globalmente.

### Paleta de Colores

#### Tema Claro (Light Mode)

-   `primary`: `#007AFF` (Azul vibrante para acciones principales y elementos activos)
-   `secondary`: `#8E8E93` (Gris neutro para texto secundario y elementos menos importantes)
-   `background`: `#F2F2F7` (Blanco roto muy claro para el fondo general)
-   `surface`: `#FFFFFF` (Blanco puro para superficies elevadas como tarjetas y modales)
-   `text-primary`: `#000000` (Negro para el texto principal)
-   `text-secondary`: `#6C6C70` (Gris oscuro para texto secundario)
-   `success`: `#34C759` (Verde para acciones de éxito)
-   `error`: `#FF3B30` (Rojo para errores y alertas)
-   `border`: `#D1D1D6` (Gris muy claro para bordes sutiles)

#### Tema Oscuro (Dark Mode)

-   `primary`: `#0A84FF` (Una versión ligeramente más brillante del azul para mejor contraste)
-   `secondary`: `#8E8E93` (Mismo gris neutro, funciona bien en ambos temas)
-   `background`: `#000000` (Negro puro para el fondo, ideal para pantallas OLED)
-   `surface`: `#1C1C1E` (Gris muy oscuro para superficies elevadas)
-   `text-primary`: `#FFFFFF` (Blanco para el texto principal)
-   `text-secondary`: `#8D8D93` (Gris claro para texto secundario)
-   `success`: `#30D158` (Una versión más brillante del verde)
-   `error`: `#FF453A` (Una versión más brillante del rojo)
-   `border`: `#38383A` (Gris oscuro para bordes sutiles)

---

## 3. Fundamentos del Diseño

### Tipografía

-   **Familia de Fuente:** `Inter` o una fuente de sistema sans-serif (como San Francisco en iOS o Roboto en Android) para garantizar legibilidad y una apariencia moderna.
-   **Escala de Tamaños:**
    -   `h1`: 34px (Bold)
    -   `h2`: 28px (Bold)
    -   `h3`: 22px (Semibold)
    -   `body`: 17px (Regular)
    -   `subheadline`: 15px (Regular)
    -   `caption`: 13px (Light)
-   **Altura de Línea:** `1.5` para el cuerpo del texto para asegurar una excelente legibilidad.

### Espaciado

Usaremos una escala de espaciado basada en un múltiplo de 8px para mantener la consistencia vertical y horizontal.

-   `xx-small`: 4px
-   `x-small`: 8px
-   `small`: 12px
-   `medium`: 16px
-   `large`: 24px
-   `x-large`: 32px
-   `xx-large`: 48px

### Layout

-   **Grid:** Un sistema de grid de 12 columnas es flexible para la mayoría de los layouts. Los márgenes laterales serán de `16px` en móvil y `24px` o más en tablets y escritorio.
-   **Puntos de Ruptura (Breakpoints):**
    -   `móvil`: < 768px
    -   `tablet`: >= 768px
    -   `escritorio`: >= 1024px

### Iconografía

-   **Set de Iconos:** `Feather Icons` o `Heroicons`. Son sets minimalistas, limpios y con buen rendimiento.
-   **Tamaños:** Los iconos deben tener un tamaño consistente, generalmente `24x24px`.
-   **Estilo:** Usar siempre el estilo de línea (outline) para una apariencia más ligera y minimalista.

---

## 4. Interacción y Gestos

Los gestos agilizan la interacción y hacen que la aplicación se sienta más nativa y fluida.

-   **Deslizar para Acciones (Swipe Actions):**
    -   En listas, deslizar un elemento hacia la izquierda revelará acciones comunes (ej: "Eliminar", "Archivar").
    -   Deslizar hacia la derecha podría revelar una acción principal (ej: "Marcar como completado").
-   **Tirar para Refrescar (Pull to Refresh):**
    -   En vistas que contienen listas de datos dinámicos, tirar hacia abajo desde la parte superior refrescará el contenido.
-   **Mantener Pulsado (Long Press):**
    -   Mantener pulsado un elemento (como una foto, un enlace o un item de una lista) mostrará un menú contextual con acciones secundarias relevantes, evitando saturar la UI principal.
-   **Pellizcar para Zoom (Pinch to Zoom):**
    -   Aplicable en imágenes, mapas o cualquier contenido donde el detalle sea importante.
-   **Doble Toque (Double Tap):**
    -   Para acciones de "me gusta" o "favorito" en elementos como tarjetas o imágenes.

---

## 5. Directrices para Desarrolladores

Esta sección es clave para mantener la integridad del diseño y reducir la deuda técnica a largo plazo.

### Creación de un Nuevo Componente

Antes de crear un componente nuevo, pregúntate: "¿Existe ya un componente que haga algo similar?". Reutilizar es siempre la primera opción. Si la respuesta es no, sigue estos pasos:

1.  **Definición de la API (Props):**
    -   Define las propiedades que recibirá el componente. Sé explícito con los tipos.
    -   Piensa en todos los estados posibles: `disabled`, `loading`, `error`, `active`, etc.
    -   Evita props que controlen directamente el estilo (ej: `style`, `className`). En su lugar, usa props que describan el estado o la variante (`variant="primary"`, `size="large"`).

2.  **Implementación con Temas:**
    -   **NUNCA** uses colores o valores hardcodeados. Utiliza siempre las variables del tema (`theme.colors.primary`, `theme.spacing.medium`).
    -   El componente debe ser agnóstico al tema. Al usar las variables del sistema de diseño, se adaptará automáticamente al modo claro y oscuro.

3.  **Estructura de Archivos:**
    -   Crea una carpeta para el componente (ej: `/components/Button`).
    -   Dentro, incluye el archivo del componente (`index.tsx`), un archivo de estilos si es necesario (`styles.ts`), y un archivo de "historias" para Storybook o similar (`Button.stories.tsx`).

4.  **Documentación y Pruebas:**
    -   Documenta cada prop en el código.
    -   Añade ejemplos en Storybook (o una herramienta similar) para cada variante y estado del componente. Esto sirve como documentación viva y facilita las pruebas visuales.

### Modificación de un Componente Existente

1.  **Evalúa el Impacto:** ¿Este cambio afectará a otras partes de la aplicación? ¿Es un cambio que rompe la compatibilidad (breaking change)?
2.  **Favorece la Extensión sobre la Modificación:**
    -   Si necesitas una nueva funcionalidad, es preferible añadir una nueva `prop` o `variant` en lugar de cambiar el comportamiento de las existentes.
    -   **Ejemplo:** En lugar de cambiar el `padding` del botón por defecto, crea una nueva prop `size="small"` que aplique ese `padding`.
3.  **Actualiza la Documentación:** Si añades o cambias props, actualiza las historias en Storybook y la documentación del componente. Un componente sin documentación actualizada es una fuente de deuda técnica.

### Ejemplo Práctico: Añadir una prop `fullWidth` a un Botón

1.  **Definir la Prop:** En `Button/index.tsx`, añadir `fullWidth?: boolean;` a la interfaz de `props`.
2.  **Aplicar el Estilo:** En `Button/styles.ts`, usar la prop para aplicar el estilo condicionalmente:
    ```javascript
    // Ejemplo con styled-components
    const ButtonWrapper = styled.button`
      width: ${props => props.fullWidth ? '100%' : 'auto'};
      // ... otros estilos que usan variables del tema
      background-color: ${props => props.theme.colors.primary};
      padding: ${props => props.theme.spacing.medium};
    `;
    ```
3.  **Documentar:** En `Button/Button.stories.tsx`, añadir una nueva historia que muestre el botón con la prop `fullWidth`.

Siguiendo estas reglas, aseguramos que la base de código del frontend sea consistente, predecible y fácil de mantener para cualquier desarrollador que se una al proyecto.
