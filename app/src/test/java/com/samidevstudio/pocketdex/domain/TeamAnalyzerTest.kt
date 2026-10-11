package com.samidevstudio.pocketdex.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamAnalyzerTest {

    private val fireRelations = TypeRelations(
        name = "fire",
        doubleDamageFrom = setOf("water", "ground", "rock"),
        doubleDamageTo = setOf("grass", "ice", "bug", "steel"),
        halfDamageFrom = setOf("fire", "grass", "ice", "bug", "steel", "fairy"),
        halfDamageTo = setOf("fire", "water", "rock", "dragon"),
        noDamageFrom = emptySet(),
        noDamageTo = emptySet()
    )

    private val flyingRelations = TypeRelations(
        name = "flying",
        doubleDamageFrom = setOf("electric", "ice", "rock"),
        doubleDamageTo = setOf("grass", "fighting", "bug"),
        halfDamageFrom = setOf("grass", "fighting", "bug"),
        halfDamageTo = setOf("electric", "rock", "steel"),
        noDamageFrom = setOf("ground"),
        noDamageTo = emptySet()
    )

    private val waterRelations = TypeRelations(
        name = "water",
        doubleDamageFrom = setOf("electric", "grass"),
        doubleDamageTo = setOf("fire", "ground", "rock"),
        halfDamageFrom = setOf("fire", "water", "ice", "steel"),
        halfDamageTo = setOf("water", "grass", "dragon"),
        noDamageFrom = emptySet(),
        noDamageTo = emptySet()
    )

    private val electricRelations = TypeRelations(
        name = "electric",
        doubleDamageFrom = setOf("ground"),
        doubleDamageTo = setOf("water", "flying"),
        halfDamageFrom = setOf("electric", "flying", "steel"),
        halfDamageTo = setOf("electric", "grass", "dragon"),
        noDamageFrom = emptySet(),
        noDamageTo = setOf("ground")
    )

    private val rockRelations = TypeRelations(
        name = "rock",
        doubleDamageFrom = setOf("water", "grass", "fighting", "ground", "steel"),
        doubleDamageTo = setOf("fire", "ice", "flying", "bug"),
        halfDamageFrom = setOf("normal", "fire", "poison", "flying"),
        halfDamageTo = setOf("fighting", "ground", "steel"),
        noDamageFrom = emptySet(),
        noDamageTo = emptySet()
    )

    private val allTypes = mapOf(
        "fire" to fireRelations,
        "flying" to flyingRelations,
        "water" to waterRelations,
        "electric" to electricRelations,
        "rock" to rockRelations
    )

    @Test
    fun dualTypeEffectivenessCalculatesMultipliersAndClassifications() {
        val defendingTypes = listOf("fire", "flying") // Charizard

        // Rock vs Fire/Flying: 2.0 * 2.0 = 4.0 (Weak)
        val rockMult = TypeEffectivenessCalculator.multiplierFor("rock", defendingTypes, allTypes)
        assertEquals(4.0, rockMult, 0.001)
        assertEquals(Effectiveness.WEAK, TypeEffectivenessCalculator.classify(rockMult))

        // Ground vs Fire/Flying: 2.0 * 0.0 = 0.0 (Immune)
        val groundMult = TypeEffectivenessCalculator.multiplierFor("ground", defendingTypes, allTypes)
        assertEquals(0.0, groundMult, 0.001)
        assertEquals(Effectiveness.IMMUNE, TypeEffectivenessCalculator.classify(groundMult))

        // Grass vs Fire/Flying: 0.5 * 0.5 = 0.25 (Resistant)
        val grassMult = TypeEffectivenessCalculator.multiplierFor("grass", defendingTypes, allTypes)
        assertEquals(0.25, grassMult, 0.001)
        assertEquals(Effectiveness.RESISTANT, TypeEffectivenessCalculator.classify(grassMult))
    }

    @Test
    fun weaknessCountsAggregatesWeaknessesAcrossTeamMembers() {
        val members = listOf(
            TeamMemberTypes("6", listOf("fire", "flying")),  // Charizard: weak to rock, electric, water
            TeamMemberTypes("130", listOf("water", "flying")) // Gyarados: weak to electric (4x), rock
        )

        val analysis = TeamAnalyzer.analyze(members, allTypes)

        // Shared weaknesses: Electric (2 members), Rock (2 members), Water (1 member)
        val weakMap = analysis.sharedWeaknesses.associate { it.type to it.weakMemberCount }
        assertEquals(2, weakMap["electric"])
        assertEquals(2, weakMap["rock"])
        assertEquals(1, weakMap["water"])
    }

    @Test
    fun emptyOrUnknownTypeDataIsHandledGracefully() {
        // Empty team
        val emptyAnalysis = TeamAnalyzer.analyze(emptyList(), allTypes)
        assertTrue(emptyAnalysis.sharedWeaknesses.isEmpty())
        assertTrue(emptyAnalysis.sharedResistances.isEmpty())
        assertTrue(emptyAnalysis.sharedImmunities.isEmpty())
        assertEquals(allTypes.keys.sorted(), emptyAnalysis.coverageGaps)

        // Member with unknown type
        val unknownMember = listOf(TeamMemberTypes("999", listOf("unknown_type")))
        val unknownAnalysis = TeamAnalyzer.analyze(unknownMember, allTypes)
        assertTrue(unknownAnalysis.sharedWeaknesses.isEmpty())
        assertEquals(allTypes.keys.sorted(), unknownAnalysis.coverageGaps)
    }

    @Test
    fun unsupportedCatalogEntriesAreNotAnalysisCandidatesButPokemonTypesRemain() {
        val catalog = allTypes + mapOf(
            "unknown" to fireRelations.copy(name = "unknown"),
            "stellar" to TypeRelations(
                name = "stellar",
                doubleDamageFrom = emptySet(),
                doubleDamageTo = emptySet(),
                halfDamageFrom = emptySet(),
                halfDamageTo = emptySet(),
                noDamageFrom = emptySet(),
                noDamageTo = emptySet()
            )
        )
        val analysis = TeamAnalyzer.analyze(
            members = listOf(TeamMemberTypes("10200", listOf("fire", "stellar"))),
            allTypes = catalog
        )

        assertEquals(1, analysis.typeComposition["stellar"])
        assertFalse(analysis.coverageGaps.contains("unknown"))
        assertFalse(analysis.coverageGaps.contains("stellar"))
        assertEquals(setOf("fire", "flying", "water", "electric", "rock", "unknown", "stellar"), catalog.keys)

        val emptyTeam = TeamAnalyzer.analyze(emptyList(), catalog)
        assertFalse(emptyTeam.coverageGaps.contains("unknown"))
        assertFalse(emptyTeam.coverageGaps.contains("stellar"))
    }

    @Test
    fun typeBasedCoverageCalculatesStabTypeGaps() {
        // Team with only Fire member (Charizard)
        val fireTeam = listOf(TeamMemberTypes("6", listOf("fire")))
        val analysis = TeamAnalyzer.analyze(fireTeam, allTypes)

        // Fire hits "grass", "ice", "bug", "steel" for double damage.
        // In our small allTypes map ("fire", "flying", "water", "electric", "rock"):
        // Fire does double damage to none of these directly in allTypes, but let's check water/electric/rock:
        // Water is in doubleDamageTo of Fire? No (halfDamageTo).
        // Rock is in doubleDamageTo of Fire? No (halfDamageTo).
        // So coverageGaps should contain all types that Fire does not hit for double damage.
        assertTrue(analysis.coverageGaps.contains("water"))
        assertTrue(analysis.coverageGaps.contains("electric"))
    }
}
