package com.samidevstudio.pocketdex.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TypeEffectivenessComparisonTest {
    private val allTypes = listOf(
        TypeRelations(
            name = "fire",
            doubleDamageFrom = setOf("ground", "rock"),
            doubleDamageTo = setOf("grass"),
            halfDamageFrom = setOf("grass"),
            halfDamageTo = setOf("fire", "water", "rock"),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet()
        ),
        TypeRelations(
            name = "flying",
            doubleDamageFrom = setOf("rock"),
            doubleDamageTo = setOf("grass"),
            halfDamageFrom = setOf("grass"),
            halfDamageTo = setOf("electric", "rock", "steel"),
            noDamageFrom = setOf("ground"),
            noDamageTo = emptySet()
        ),
        TypeRelations(
            name = "grass",
            doubleDamageFrom = setOf("fire", "flying"),
            doubleDamageTo = setOf("water", "ground", "rock"),
            halfDamageFrom = setOf("fire", "flying"),
            halfDamageTo = setOf("fire", "grass", "poison", "flying", "dragon", "steel"),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet()
        ),
        TypeRelations(
            name = "water",
            doubleDamageFrom = setOf("electric", "grass"),
            doubleDamageTo = setOf("fire", "ground", "rock"),
            halfDamageFrom = setOf("fire", "water", "ice", "steel"),
            halfDamageTo = setOf("water", "grass", "dragon"),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet()
        ),
        TypeRelations(
            name = "rock",
            doubleDamageFrom = setOf("water", "grass", "fighting", "ground", "steel"),
            doubleDamageTo = setOf("fire", "ice", "flying", "bug"),
            halfDamageFrom = setOf("normal", "fire", "poison", "flying"),
            halfDamageTo = setOf("fighting", "ground", "steel"),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet()
        ),
        TypeRelations(
            name = "ground",
            doubleDamageFrom = setOf("water", "grass", "ice"),
            doubleDamageTo = setOf("fire", "electric", "poison", "rock", "steel"),
            halfDamageFrom = setOf("poison", "rock"),
            halfDamageTo = setOf("grass", "bug"),
            noDamageFrom = emptySet(),
            noDamageTo = setOf("flying")
        ),
        TypeRelations(
            name = "stellar",
            doubleDamageFrom = emptySet(),
            doubleDamageTo = emptySet(),
            halfDamageFrom = emptySet(),
            halfDamageTo = emptySet(),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet()
        )
    ).associateBy { it.name }

    @Test
    fun calculatesSingleAndDualTypeEffectivenessIncludingCombinedWeaknessesResistancesAndImmunity() {
        assertEquals(
            2.0,
            TypeEffectivenessCalculator.multiplierOrNullFor("fire", listOf("grass"), allTypes)!!,
            0.001
        )
        assertEquals(
            0.5,
            TypeEffectivenessCalculator.multiplierOrNullFor("fire", listOf("water"), allTypes)!!,
            0.001
        )
        assertEquals(
            4.0,
            TypeEffectivenessCalculator.multiplierOrNullFor("rock", listOf("fire", "flying"), allTypes)!!,
            0.001
        )
        assertEquals(
            0.25,
            TypeEffectivenessCalculator.multiplierOrNullFor("grass", listOf("fire", "flying"), allTypes)!!,
            0.001
        )
        assertEquals(
            0.0,
            TypeEffectivenessCalculator.multiplierOrNullFor("ground", listOf("fire", "flying"), allTypes)!!,
            0.001
        )
    }

    @Test
    fun reciprocalAttackDirectionsAreCalculatedIndependently() {
        val firstAttackingSecond = TypeEffectivenessCalculator.attackMatchups(
            attackingTypes = listOf("fire", "flying"),
            defendingTypes = listOf("grass"),
            allTypes = allTypes
        )
        val secondAttackingFirst = TypeEffectivenessCalculator.attackMatchups(
            attackingTypes = listOf("grass"),
            defendingTypes = listOf("fire", "flying"),
            allTypes = allTypes
        )

        assertEquals(listOf("fire", "flying"), firstAttackingSecond.map { it.attackingType })
        assertEquals(listOf(2.0, 2.0), firstAttackingSecond.map { it.multiplier })
        assertEquals(listOf("grass"), secondAttackingFirst.map { it.attackingType })
        assertEquals(listOf(0.25), secondAttackingFirst.map { it.multiplier })
    }

    @Test
    fun unavailableRelationDataDoesNotDefaultToNeutral() {
        assertNull(TypeEffectivenessCalculator.multiplierOrNullFor("fire", emptyList(), allTypes))
        assertNull(TypeEffectivenessCalculator.multiplierOrNullFor("unknown", listOf("grass"), allTypes))
        assertNull(TypeEffectivenessCalculator.multiplierOrNullFor("fire", listOf("unknown"), allTypes))
        assertNull(TypeEffectivenessCalculator.multiplierOrNullFor("stellar", listOf("grass"), allTypes))
        assertEquals(
            null,
            TypeEffectivenessCalculator.attackMatchups(
                attackingTypes = listOf("stellar"),
                defendingTypes = listOf("grass"),
                allTypes = allTypes
            ).single().multiplier
        )
    }

    @Test
    fun typeDetailMatchupsHideOnlyUnsupportedNamesWithoutChangingCachedRelations() {
        val type = allTypes.getValue("fire").copy(
            doubleDamageTo = setOf("grass", "unknown", "stellar", "shadow"),
            halfDamageTo = setOf("water", "unknown", "stellar"),
            noDamageTo = setOf("flying", "unknown", "stellar"),
            doubleDamageFrom = setOf("ground", "unknown", "stellar", "shadow"),
            halfDamageFrom = setOf("grass", "unknown", "stellar"),
            noDamageFrom = setOf("ground", "unknown", "stellar")
        )

        val summary = TypeEffectivenessCalculator.singleTypeMatchups(type)

        assertEquals(listOf("grass", "shadow"), summary.superEffectiveAgainst)
        assertEquals(listOf("water"), summary.notVeryEffectiveAgainst)
        assertEquals(listOf("flying"), summary.noEffectAgainst)
        assertEquals(listOf("ground", "shadow"), summary.weaknesses)
        assertEquals(listOf("grass"), summary.resistances)
        assertEquals(listOf("ground"), summary.immunities)
        assertEquals(setOf("grass", "unknown", "stellar", "shadow"), type.doubleDamageTo)
        assertEquals(setOf("ground", "unknown", "stellar"), type.noDamageFrom)
    }

    @Test
    fun formatsMultipliersWithoutParentheses() {
        assertEquals("0×", TypeEffectivenessCalculator.formatMultiplier(0.0))
        assertEquals("¼×", TypeEffectivenessCalculator.formatMultiplier(0.25))
        assertEquals("½×", TypeEffectivenessCalculator.formatMultiplier(0.5))
        assertEquals("1×", TypeEffectivenessCalculator.formatMultiplier(1.0))
        assertEquals("2×", TypeEffectivenessCalculator.formatMultiplier(2.0))
        assertEquals("4×", TypeEffectivenessCalculator.formatMultiplier(4.0))
    }
}
