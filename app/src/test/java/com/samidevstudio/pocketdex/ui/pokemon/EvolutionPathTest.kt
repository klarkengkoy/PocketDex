package com.samidevstudio.pocketdex.ui.pokemon

import org.junit.Assert.assertEquals
import org.junit.Test

class EvolutionPathTest {
    private fun node(id: String, vararg children: EvolutionNode) = EvolutionNode(
        id = id,
        name = "pokemon-$id",
        imageUrl = "",
        evolvesTo = children.toList()
    )

    @Test
    fun linearChainKeepsParentToChildOrder() {
        val chain = listOf(node("1", node("2", node("3"))))

        assertEquals(listOf("1", "2", "3"), chain.evolutionPath("3", emptyMap()).map { it.id })
    }

    @Test
    fun splitBranchUsesFirstEvolutionAsHorizontalDefault() {
        val chain = listOf(node("1", node("2"), node("3")))

        assertEquals(listOf("1", "2"), chain.evolutionPath("1", emptyMap()).map { it.id })
        assertEquals(listOf("1", "3"), chain.evolutionPath("3", emptyMap()).map { it.id })
        assertEquals(listOf("1", "2"), chain.evolutionPath("1", mapOf("1" to "2")).map { it.id })
    }

    @Test
    fun verticalSiblingSwitchWrapsAndChangesTheFollowingPreview() {
        val chain = listOf(
            node(
                "265",
                node("266", node("267")),
                node("268", node("269"))
            )
        )

        assertEquals("268", chain.nextEvolutionSibling("266", forward = true)?.id)
        assertEquals("266", chain.nextEvolutionSibling("268", forward = true)?.id)
        assertEquals("268", chain.nextEvolutionSibling("266", forward = false)?.id)

        val selectedPath = chain.evolutionPath("268", mapOf("265" to "268"))
        assertEquals(listOf("265", "268", "269"), selectedPath.map { it.id })
    }

    @Test
    fun terminalVerticalSiblingSwitchKeepsEarlierEvolutionStages() {
        val chain = listOf(node("43", node("44", node("45"), node("182"))))

        assertEquals(listOf("45", "182"), chain.evolutionSiblings("45").map { it.id })
        val selectedPath = chain.evolutionPath("182", mapOf("44" to "182"))

        assertEquals(listOf("43", "44", "182"), selectedPath.map { it.id })
    }

    @Test
    fun branchFollowedByEvolutionKeepsSelectedRoute() {
        val chain = listOf(node("1", node("2", node("4")), node("3", node("5"))))

        assertEquals(
            listOf("1", "3", "5"),
            chain.evolutionPath("5", mapOf("1" to "3")).map { it.id }
        )
        assertEquals(
            listOf("1", "2", "4"),
            chain.evolutionPath("4", mapOf("1" to "2")).map { it.id }
        )
    }
}
