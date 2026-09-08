package com.samidevstudio.pocketdex.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A custom Retro Indication that provides a scale and slight color shift
 * instead of the standard Material ripple.
 */
object RetroIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return RetroIndicationNode(interactionSource)
    }

    override fun hashCode(): Int = -1
    override fun equals(other: Any?) = other === this
}

private class RetroIndicationNode(
    private val interactionSource: InteractionSource
) : Modifier.Node(), DrawModifierNode {
    private val scale = Animatable(1f)
    private val alpha = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        launch { scale.animateTo(0.96f, tween(80)) }
                        launch { alpha.animateTo(0.08f, tween(80)) }
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        launch { scale.animateTo(1f, tween(100)) }
                        launch { alpha.animateTo(0f, tween(100)) }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        scale(scale.value, pivot = size.center) {
            this@draw.drawContent()
            if (alpha.value > 0f) {
                drawRect(color = Color.Black.copy(alpha = alpha.value))
            }
        }
    }
}

@Composable
fun rememberRetroIndication(): Indication = RetroIndication
