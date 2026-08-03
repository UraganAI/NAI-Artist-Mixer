package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MultipleHeartsIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back/Top-left smaller heart
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = tint.copy(alpha = 0.65f),
            modifier = Modifier
                .size(15.dp)
                .offset(x = (-4.5).dp, y = (-3.5).dp)
        )
        // Front/Bottom-right main heart
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(17.dp)
                .offset(x = 3.dp, y = 3.dp)
        )
    }
}
