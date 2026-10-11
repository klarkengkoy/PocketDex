package com.samidevstudio.pocketdex.domain

/**
 * Plain data carrier for a single type's damage relations, decoupled from the
 * Room entity so the domain layer has no Android/Room dependency.
 */
data class TypePokemonReference(
    val id: String,
    val name: String
)

data class TypeRelations(
    val name: String,
    val doubleDamageFrom: Set<String>,
    val doubleDamageTo: Set<String>,
    val halfDamageFrom: Set<String>,
    val halfDamageTo: Set<String>,
    val noDamageFrom: Set<String>,
    val noDamageTo: Set<String>,
    val pokemonReferences: List<TypePokemonReference> = emptyList(),
    val hasIncompletePokemonReferences: Boolean = false
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

data class TypeAttackMatchup(
    val attackingType: String,
    /** Null means the cached type data cannot support a reliable calculation. */
    val multiplier: Double?
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
            multiplier *= againstDefendingType(attackType, relations)
        }
        return multiplier
    }

    /** Returns null when the selected types or their cached damage relations are unavailable. */
    fun multiplierOrNullFor(
        attackType: String,
        defendingTypes: List<String>,
        allTypes: Map<String, TypeRelations>
    ): Double? {
        if (defendingTypes.isEmpty()) return null
        val attackingType = allTypes[attackType] ?: return null
        if (!attackingType.hasDamageRelationData()) return null

        var multiplier = 1.0
        for (defendingType in defendingTypes) {
            val relations = allTypes[defendingType] ?: return null
            if (!relations.hasDamageRelationData()) return null
            multiplier *= againstDefendingType(attackType, relations)
        }
        return multiplier
    }

    /** Preserves one row for every attacking type, including types with unavailable data. */
    fun attackMatchups(
        attackingTypes: List<String>,
        defendingTypes: List<String>,
        allTypes: Map<String, TypeRelations>
    ): List<TypeAttackMatchup> = attackingTypes.map { attackType ->
        TypeAttackMatchup(
            attackingType = attackType,
            multiplier = multiplierOrNullFor(attackType, defendingTypes, allTypes)
        )
    }

    /** Formats common Pokémon type multipliers without confusing them with member counts. */
    fun formatMultiplier(multiplier: Double): String = when (multiplier) {
        0.0 -> "0×"
        0.25 -> "¼×"
        0.5 -> "½×"
        1.0 -> "1×"
        else -> when {
            multiplier >= 1.0 && multiplier % 1.0 == 0.0 -> "${multiplier.toLong()}×"
            multiplier > 0.0 && (1.0 / multiplier) % 1.0 == 0.0 -> {
                val denominator = (1.0 / multiplier).toLong()
                when (denominator) {
                    2L -> "½×"
                    4L -> "¼×"
                    8L -> "⅛×"
                    else -> "1/${denominator}×"
                }
            }
            else -> "${multiplier}×"
        }
    }

    private fun againstDefendingType(attackType: String, relations: TypeRelations): Double = when (attackType) {
        in relations.noDamageFrom -> 0.0
        in relations.doubleDamageFrom -> 2.0
        in relations.halfDamageFrom -> 0.5
        else -> 1.0
    }

    private fun TypeRelations.hasDamageRelationData(): Boolean =
        doubleDamageFrom.isNotEmpty() || doubleDamageTo.isNotEmpty() ||
            halfDamageFrom.isNotEmpty() || halfDamageTo.isNotEmpty() ||
            noDamageFrom.isNotEmpty() || noDamageTo.isNotEmpty()

    fun classify(multiplier: Double): Effectiveness = when {
        multiplier == 0.0 -> Effectiveness.IMMUNE
        multiplier < 1.0 -> Effectiveness.RESISTANT
        multiplier > 1.0 -> Effectiveness.WEAK
        else -> Effectiveness.NEUTRAL
    }

    /** Builds the full offensive + defensive matchup summary for a single type's detail screen. */
    fun singleTypeMatchups(type: TypeRelations): TypeMatchupSummary = TypeMatchupSummary(
        superEffectiveAgainst = type.doubleDamageTo.filter(TypeBrowseability::isBrowseable).sorted(),
        notVeryEffectiveAgainst = type.halfDamageTo.filter(TypeBrowseability::isBrowseable).sorted(),
        noEffectAgainst = type.noDamageTo.filter(TypeBrowseability::isBrowseable).sorted(),
        weaknesses = type.doubleDamageFrom.filter(TypeBrowseability::isBrowseable).sorted(),
        resistances = type.halfDamageFrom.filter(TypeBrowseability::isBrowseable).sorted(),
        immunities = type.noDamageFrom.filter(TypeBrowseability::isBrowseable).sorted()
    )
}
