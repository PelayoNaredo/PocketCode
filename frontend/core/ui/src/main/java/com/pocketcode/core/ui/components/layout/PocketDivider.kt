package com.pocketcode.core.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * PocketDivider - Divisor horizontal estilizado con tokens del sistema.
 *
 * Componente simple para separar secciones con una línea horizontal.
 * Aplica los tokens de color de Pocket para mantener consistencia visual.
 *
 * @param modifier Modificador para customizar el layout
 * @param thickness Grosor del divisor
 * @param color Color del divisor (por defecto usa ColorTokens.outline con transparencia)
 *
 * @sample PocketDividerSamples
 */
@Composable
fun PocketDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = ColorTokens.outline.copy(alpha = 0.12f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

/**
 * PocketVerticalDivider - Divisor vertical estilizado con tokens del sistema.
 *
 * Variante vertical del divisor para separar elementos horizontalmente.
 *
 * @param modifier Modificador para customizar el layout
 * @param thickness Grosor del divisor
 * @param color Color del divisor
 */
@Composable
fun PocketVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = ColorTokens.outline.copy(alpha = 0.12f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

/**
 * Ejemplos de uso de PocketDivider
 */
private object PocketDividerSamples {
    
    @Composable
    fun BasicUsage() {
        Column {
            Text("Sección 1")
            PocketDivider()
            Text("Sección 2")
        }
    }
    
    @Composable
    fun WithCustomColor() {
        PocketDivider(
            thickness = 2.dp,
            color = ColorTokens.primary.copy(alpha = 0.5f)
        )
    }
    
    @Composable
    fun InList() {
        LazyColumn {
            items(10) { index ->
                Text("Item $index")
                if (index < 9) {
                    PocketDivider(
                        modifier = Modifier.padding(horizontal = SpacingTokens.medium)
                    )
                }
            }
        }
    }
}
