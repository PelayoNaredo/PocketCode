package com.pocketcode.domain.project.model

/**
 * Representa la fuente de datos utilizada para importar un proyecto existente.
 */
sealed interface ProjectImportSource {
    /**
     * Importación basada en un archivo comprimido (por ejemplo, `.zip`).
     *
     * @param bytes Contenido bruto del archivo comprimido.
     * @param suggestedName Nombre sugerido para el proyecto resultante. Si es nulo se derivará del archivo.
     */
    data class Archive(
        val bytes: ByteArray,
        val suggestedName: String? = null
    ) : ProjectImportSource
}

/**
 * Solicitud de importación de proyecto. Actualmente solo admite archivos comprimidos, pero se puede
 * extender en el futuro para manejar carpetas locales o repositorios remotos.
 */
data class ProjectImportRequest(
    val source: ProjectImportSource
)
