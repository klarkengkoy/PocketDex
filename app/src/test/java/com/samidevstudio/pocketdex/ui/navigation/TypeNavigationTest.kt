package com.samidevstudio.pocketdex.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Explore
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.TypeDetail
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Types
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeNavigationTest {
    @Test
    fun selectingRelatedTypeReplacesTheCurrentDetailAndBackReturnsToTypes() {
        val backStack = mutableListOf<NavKey>(Explore, Types)

        backStack.selectTypeDetail("fire")
        backStack.selectTypeDetail("water")

        assertEquals(listOf(Explore, Types, TypeDetail("water")), backStack)

        backStack.removeAt(backStack.lastIndex)
        assertEquals(Types, backStack.last())
    }
}
