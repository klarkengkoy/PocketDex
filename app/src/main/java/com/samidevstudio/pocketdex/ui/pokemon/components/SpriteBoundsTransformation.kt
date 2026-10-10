package com.samidevstudio.pocketdex.ui.pokemon.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.ceil

private const val MAX_SPRITE_UPSCALE = 1.5f

internal data class SpriteBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

internal fun findVisibleSpriteBounds(
    width: Int,
    height: Int,
    pixels: IntArray
): SpriteBounds? {
    require(width > 0 && height > 0 && pixels.size == width * height)

    var left = width
    var top = height
    var right = 0
    var bottom = 0

    pixels.forEachIndexed { index, pixel ->
        if (pixel ushr 24 != 0) {
            val x = index % width
            val y = index / width
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x + 1)
            bottom = maxOf(bottom, y + 1)
        }
    }

    return if (right == 0 || bottom == 0) null else SpriteBounds(left, top, right, bottom)
}

internal fun spriteCropBounds(
    imageWidth: Int,
    imageHeight: Int,
    visible: SpriteBounds
): SpriteBounds {
    val cropWidth = maxOf(
        visible.width,
        ceil(imageWidth / MAX_SPRITE_UPSCALE).toInt()
    )
    val cropHeight = maxOf(
        visible.height,
        ceil(imageHeight / MAX_SPRITE_UPSCALE).toInt()
    )
    val cropLeft = ((visible.left + visible.right - cropWidth) / 2)
        .coerceIn(maxOf(0, visible.right - cropWidth), minOf(visible.left, imageWidth - cropWidth))
    val cropTop = ((visible.top + visible.bottom - cropHeight) / 2)
        .coerceIn(maxOf(0, visible.bottom - cropHeight), minOf(visible.top, imageHeight - cropHeight))

    return SpriteBounds(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight)
}

internal object SpriteBoundsTransformation : Transformation {
    override val cacheKey = "pocketdex_sprite_alpha_bounds_v1_max_upscale_1_5"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val pixels = IntArray(input.width * input.height)
        input.getPixels(pixels, 0, input.width, 0, 0, input.width, input.height)
        val visible = findVisibleSpriteBounds(input.width, input.height, pixels) ?: return input
        val crop = spriteCropBounds(input.width, input.height, visible)

        return if (
            crop.left == 0 && crop.top == 0 &&
            crop.right == input.width && crop.bottom == input.height
        ) {
            input
        } else {
            Bitmap.createBitmap(input, crop.left, crop.top, crop.width, crop.height)
        }
    }
}
