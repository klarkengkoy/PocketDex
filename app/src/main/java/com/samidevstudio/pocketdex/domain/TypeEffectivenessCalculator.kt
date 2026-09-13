package com.samidevstudio.pocketdex.domain

/**
 * Plain data carrier for a single type's damage relations, decoupled from the
 * Room entity so the domain layer has no Android/Room dependency.
 */
data class TypeRelations(
    val name: String,
    val doubleDamageFrom: Set<String>,
    val doubleDamageTo: Set<String>,
    val halfDamageFrom: Set<String>,
    val halfDamageTo: Set<String>,
    val noDamageFrom: Set<String>,
    val noDamageTo: Set<String>,
    val pokemonNames: List<String> = emptyList()
)

enum class Effectiveness { IMMUNE, RESISTANT, NEUTRAL, WEAK }

/**
 * A single type's own matchup summary (used by the Type Detail screen).
 * "Offensive" = this type attacking others. "Defensive" = this type being attacked.
 */
data class TypeMatchupSummary(
    val superEffectiveAgainst: List<String>,
    val notVeryEffectiveAgainst: List<String>,
    val noEffectAgainst: List<String>,
    val weaknesses: List<String>,
    val resistances: List<String>,
    val immunities: List<String>
)

/**
 * Computes type-effectiveness matchups. Kept free of Android/Compose so it can be
 * unit-tested and reused by both the Types feature and the Team analyzer.
 */
object TypeEffectivenessCalculator {

    /**
     * Combined multiplier of [attackType] against a Pokémon with [defendingTypes] (1 or 2 types),
     * using PokeAPI's damage_relations semantics (each defending type's "*_from" lists describe
     * how it is affected by that attacking type).
     */
    fun multiplierFor(
        attackType: String,
        defendingTypes: List<String>,
        allTypes: Map<String, TypeRelations>
    ): Double {
        var multiplier = 1.0
        for (defType in defendingTypes) {
            val relations = allTypes[defType] ?: continue
            multiplier *= when (attackType) {
                in relations.noDamageFrom -> 0.0
                in relations.doubleDamageFrom -> 2.0
                in relations.halfDamageFrom -> 0.5
                else -> 1.0
            }
        }
        return multiplier
    }

    fun classify(multiplier: Double): Effectiveness = when {
        multiplier == 0.0 -> Effectiveness.IMMUNE
        multiplier < 1.0 -> Effectiveness.RESISTANT
        multiplier > 1.0 -> Effectiveness.WEAK
        else -> Effectiveness.NEUTRAL
    }

    /** Builds the full offensive + defensive matchup summary for a single type's detail screen. */
    fun singleTypeMatchups(type: TypeRelations): TypeMatchupSummary = TypeMatchupSummary(
        superEffectiveAgainst = type.doubleDamageTo.sorted(),
        notVeryEffectiveAgainst = type.halfDamageTo.sorted(),
        noEffectAgainst = type.noDamageTo.sorted(),
        weaknesses = type.doubleDamageFrom.sorted(),
        resistances = type.halfDamageFrom.sorted(),
        immunities = type.noDamageFrom.sorted()
    )
}
