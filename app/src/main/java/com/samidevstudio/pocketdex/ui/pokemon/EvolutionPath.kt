package com.samidevstudio.pocketdex.ui.pokemon

fun List<EvolutionNode>.evolutionPath(
    currentId: String,
    selectedChildren: Map<String, String>
): List<EvolutionNode> {
    val root = singleOrNull() ?: return emptyList()
    val path = mutableListOf<EvolutionNode>()
    var node = root

    while (true) {
        path += node
        val next = node.evolvesTo.firstOrNull { it.id == selectedChildren[node.id] }
            ?: node.evolvesTo.firstOrNull { it.containsEvolution(currentId) }
            ?: node.evolvesTo.firstOrNull()
            ?: break
        node = next
    }

    return path
}

fun List<EvolutionNode>.evolutionSiblings(currentId: String): List<EvolutionNode> {
    val parent = singleOrNull()?.findParentOf(currentId) ?: return emptyList()
    return parent.evolvesTo.takeIf { it.size > 1 }.orEmpty()
}

fun List<EvolutionNode>.nextEvolutionSibling(currentId: String, forward: Boolean): EvolutionNode? {
    val siblings = evolutionSiblings(currentId)
    if (siblings.isEmpty()) return null
    val index = siblings.indexOfFirst { it.id == currentId }
    if (index == -1) return null
    val offset = if (forward) 1 else -1
    val nextIndex = (index + offset + siblings.size) % siblings.size
    return siblings[nextIndex]
}

fun List<EvolutionNode>.evolutionParent(currentId: String): EvolutionNode? =
    singleOrNull()?.findParentOf(currentId)

private fun EvolutionNode.findParentOf(id: String): EvolutionNode? =
    if (evolvesTo.any { it.id == id }) this
    else evolvesTo.firstNotNullOfOrNull { it.findParentOf(id) }

private fun EvolutionNode.containsEvolution(id: String): Boolean =
    this.id == id || evolvesTo.any { it.containsEvolution(id) }
