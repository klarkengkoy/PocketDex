package com.samidevstudio.pocketdex.ui.pokemon.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteBoundsTransformationTest {

    @Test
    fun findVisibleSpriteBounds_ignoresTransparentPadding() {
        val pixels = IntArray(5 * 4)
        pixels[1 * 5 + 2] = 0xFF112233.toInt()
        pixels[2 * 5 + 3] = 0x80112233.toInt()

        assertEquals(SpriteBounds(2, 1, 4, 3), findVisibleSpriteBounds(5, 4, pixels))
    }

    @Test
    fun findVisibleSpriteBounds_returnsNullForFullyTransparentImage() {
        assertNull(findVisibleSpriteBounds(3, 2, IntArray(6)))
    }

    @Test
    fun spriteCropBounds_keepsSpriteVisibleAndLimitsUpscale() {
        val visible = SpriteBounds(40, 30, 50, 60)

        val crop = spriteCropBounds(100, 100, visible)

        assertEquals(67, crop.width)
        assertEquals(67, crop.height)
        assertTrue(crop.left <= visible.left)
        assertTrue(crop.top <= visible.top)
        assertTrue(crop.right >= visible.right)
        assertTrue(crop.bottom >= visible.bottom)
    }

    @Test
    fun spriteCropBounds_doesNotCropFullImageSprite() {
        val visible = SpriteBounds(0, 0, 20, 30)

        assertEquals(SpriteBounds(0, 0, 20, 30), spriteCropBounds(20, 30, visible))
    }
}
