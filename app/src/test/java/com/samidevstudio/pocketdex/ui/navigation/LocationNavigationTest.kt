package com.samidevstudio.pocketdex.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Explore
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.LocationDetail
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.RegionDetail
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Regions
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationNavigationTest {
    @Test
    fun regionLocationSelectionPassesSelectedLocationToDetailRoute() {
        val locationId = "unova-mystery-zone"
        val backStack = mutableListOf<NavKey>(
            Explore,
            Regions,
            RegionDetail("unova", "unova")
        )

        backStack.selectLocationDetail(locationId, locationId)

        assertEquals(
            listOf(Explore, Regions, RegionDetail("unova", "unova"), LocationDetail(locationId, locationId)),
            backStack
        )
        backStack.removeAt(backStack.lastIndex)
        assertEquals(RegionDetail("unova", "unova"), backStack.last())
    }
}
