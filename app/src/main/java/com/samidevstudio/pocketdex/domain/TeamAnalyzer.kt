package com.samidevstudio.pocketdex.domain

/** Minimal, UI-agnostic view of a team member needed for analysis. */
data class TeamMemberTypes(
    val pokemonId: String,
    val types: List<String>
)

/** Derived, whole-team analysis: composition, weaknesses/resistances, and type coverage. */
data class TeamAnalysis(
    val typeComposition: Map<String, Int>,
    val sharedWeaknesses: List<TeamTypeWeakness>,
    val sharedResistances: List<String>,
    val sharedImmunities: List<String>,
    val coverageGaps: List<String>
)

/** How many team members are weak to [type], and their multipliers. */
data class TeamTypeWeakness(
    val type: String,
    val weakMemberCount: Int
)

/**
 * Pure business logic for the Team Builder. No Android/Compose dependency so it can be
 * unit-tested directly.
 */
object TeamAnalyzer {

    fun analyze(
        members: List<TeamMemberTypes>,
        allTypes: Map<String, TypeRelations>
    ): TeamAnalysis {
        if (members.isEmpty() || allTypes.isEmpty()) {
            return TeamAnalysis(
                typeComposition = emptyMap(),
                sharedWeaknesses = emptyList(),
                sharedResistances = emptyList(),
                sharedImmunities = emptyList(),
                coverageGaps = allTypes.keys.sorted()
            )
        }

        // Type composition: how many team members carry each type.
        val composition = members
            .flatMap { it.types }
            .groupingBy { it }
            .eachCount()

        // For every attacking type, count how many team members are weak/resistant/immune to it.
        val attackTypes = allTypes.keys
        val weakCounts = mutableMapOf<String, Int>()
        val resistantTypes = mutableListOf<String>()
        val immuneTypes = mutableListOf<String>()
        val coveredTypes = mutableSetOf<String>()

        for (attackType in attackTypes) {
            val classifications = members.map { member ->
                val multiplier = TypeEffectivenessCalculator.multiplierFor(attackType, member.types, allTypes)
                TypeEffectivenessCalculator.classify(multiplier)
            }

            val weakCount = classifications.count { it == Effectiveness.WEAK }
            val anyImmune = classifications.any { it == Effectiveness.IMMUNE }
            val allResistOrImmune = classifications.all {
                it == Effectiveness.RESISTANT || it == Effectiveness.IMMUNE
            }

            if (weakCount > 0) weakCounts[attackType] = weakCount
            if (anyImmune) immuneTypes.add(attackType)
            if (allResistOrImmune) resistantTypes.add(attackType)

            // Coverage: does any team member's own type learnset threaten this attacking type
            // offensively? We approximate coverage using each member's own types as their move pool.
            for (member in members) {
                for (memberType in member.types) {
                    val relations = allTypes[memberType] ?: continue
                    if (attackType in relations.doubleDamageTo) coveredTypes.add(attackType)
                }
            }
        }

        val sharedWeaknesses = weakCounts.entries
            .sortedByDescending { it.value }
            .map { TeamTypeWeakness(type = it.key, weakMemberCount = it.value) }

        val coverageGaps = attackTypes.filterNot { it in coveredTypes }.sorted()

        return TeamAnalysis(
            typeComposition = composition,
            sharedWeaknesses = sharedWeaknesses,
            sharedResistances = resistantTypes.sorted(),
            sharedImmunities = immuneTypes.sorted(),
            coverageGaps = coverageGaps
        )
    }
}
