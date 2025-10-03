package com.pocketcode.features.settings.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas ligeras que garantizan la estabilidad del contrato público
 * de telemetría sin depender de componentes de Android en tiempo de compilación.
 */
class SettingsTelemetryContractTest {

    @Test
    fun `los surfaces de telemetría exponen todas las áreas esperadas`() {
        val expected = setOf(
            "GENERAL",
            "EDITOR",
            "AI",
            "PROJECT"
        )

        val actual = SettingsTelemetrySurface.values().map { it.name }.toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun `los tipos de cambio están alineados con los eventos soportados`() {
        val supported = setOf(
            SettingsChangeType.TOGGLE,
            SettingsChangeType.OPTION,
            SettingsChangeType.ACTION
        )

        assertEquals(supported, SettingsChangeType.values().toSet())
    }

    @Test
    fun `todas las superficies generan claves de analytics coherentes`() {
        SettingsTelemetrySurface.values().forEach { surface ->
            val analyticsKey = surface.name.lowercase()
            assertTrue(analyticsKey.isNotBlank())
            assertTrue(surface.name.all { it.isUpperCase() || it == '_' })
        }
    }
}
