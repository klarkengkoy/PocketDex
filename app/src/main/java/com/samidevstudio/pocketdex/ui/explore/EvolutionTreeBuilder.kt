package com.samidevstudio.pocketdex.ui.explore

import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode

/**
/ Compute all distinct evolution paths from root to leaf in an evolution tree.
/ E.g., for Eevee, returns [[Eevee, Vaporeon], [Eevee, Jolteon], [Eevee, Flareon], ...]
/ E.g., for Wurmple, returns [[Wurmple, Silcoon, Beautifly], [Wurmple, Cascoon, Dustox]]
 */
fun EvolutionNode.allPaths(): List<List<EvolutionNode>> {
    if (evolvesTo.isEmpty()) {
        return listOf(listOf(this))
    }
    return evolvesTo.flatMap { child ->
        child.allPaths().map { childPath ->
            listOf(this) + childPath
        }
    }
}

/**
/ Group evolution nodes by stage/depth (Stage 0, Stage 1, Stage 2).
 */
fun EvolutionNode.byStages(): List<List<EvolutionNode>> {
    val result = mutableListOf<MutableList<EvolutionNode>>()
    fun traverse(node: EvolutionNode, depth: Int) {
        while (result.size <= depth) {
            result.add(mutableListOf())
        }
        if (result[depth].none { it.id == node.id }) {
            result[depth].add(node)
        }
        for (child in node.evolvesTo) {
            traverse(child, depth + 1)
        }
    }
    traverse(this, 0)
    return result
}
