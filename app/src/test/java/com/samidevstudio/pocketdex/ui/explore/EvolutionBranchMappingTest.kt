package com.samidevstudio.pocketdex.ui.explore

import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import org.junit.Assert.assertEquals
import org.junit.Test

class EvolutionBranchMappingTest {

    @Test
    fun linearEvolutionChainReturnsSinglePath() {
        val root = EvolutionNode(
            id = "1", name = "bulbasaur", imageUrl = "",
            evolvesTo = listOf(
                EvolutionNode(
                    id = "2", name = "ivysaur", imageUrl = "",
                    evolvesTo = listOf(
                        EvolutionNode(id = "3", name = "venusaur", imageUrl = "")
                    )
                )
            )
        )

        val paths = root.allPaths()
        assertEquals(1, paths.size)
        assertEquals(listOf("1", "2", "3"), paths[0].map { it.id })

        val stages = root.byStages()
        assertEquals(3, stages.size)
        assertEquals(listOf("1"), stages[0].map { it.id })
        assertEquals(listOf("2"), stages[1].map { it.id })
        assertEquals(listOf("3"), stages[2].map { it.id })
    }

    @Test
    fun multiBranchEvolutionReturnsMultiplePaths() {
        val root = EvolutionNode(
            id = "133", name = "eevee", imageUrl = "",
            evolvesTo = listOf(
                EvolutionNode(id = "134", name = "vaporeon", imageUrl = ""),
                EvolutionNode(id = "135", name = "jolteon", imageUrl = ""),
                EvolutionNode(id = "136", name = "flareon", imageUrl = "")
            )
        )

        val paths = root.allPaths()
        assertEquals(3, paths.size)
        assertEquals(listOf("133", "134"), paths[0].map { it.id })
        assertEquals(listOf("133", "135"), paths[1].map { it.id })
        assertEquals(listOf("133", "136"), paths[2].map { it.id })

        val stages = root.byStages()
        assertEquals(2, stages.size)
        assertEquals(listOf("133"), stages[0].map { it.id })
        assertEquals(listOf("134", "135", "136"), stages[1].map { it.id })
    }

    @Test
    fun multiBranchWithSubsequentEvolutionsReturnsAllFullPaths() {
        val root = EvolutionNode(
            id = "265", name = "wurmple", imageUrl = "",
            evolvesTo = listOf(
                EvolutionNode(
                    id = "266", name = "silcoon", imageUrl = "",
                    evolvesTo = listOf(EvolutionNode(id = "267", name = "beautifly", imageUrl = ""))
                ),
                EvolutionNode(
                    id = "268", name = "cascoon", imageUrl = "",
                    evolvesTo = listOf(EvolutionNode(id = "269", name = "dustox", imageUrl = ""))
                )
            )
        )

        val paths = root.allPaths()
        assertEquals(2, paths.size)
        assertEquals(listOf("265", "266", "267"), paths[0].map { it.id })
        assertEquals(listOf("265", "268", "269"), paths[1].map { it.id })

        val stages = root.byStages()
        assertEquals(3, stages.size)
        assertEquals(listOf("265"), stages[0].map { it.id })
        assertEquals(listOf("266", "268"), stages[1].map { it.id })
        assertEquals(listOf("267", "269"), stages[2].map { it.id })
    }

    @Test
    fun singleNodeWithNoEvolutionsReturnsSingleElementPathAndStage() {
        val root = EvolutionNode(id = "150", name = "mewtwo", imageUrl = "")

        val paths = root.allPaths()
        assertEquals(1, paths.size)
        assertEquals(listOf("150"), paths[0].map { it.id })

        val stages = root.byStages()
        assertEquals(1, stages.size)
        assertEquals(listOf("150"), stages[0].map { it.id })
    }
}
