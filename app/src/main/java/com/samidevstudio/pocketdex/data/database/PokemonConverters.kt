package com.samidevstudio.pocketdex.data.database

import androidx.room.TypeConverter
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo
import kotlinx.serialization.json.Json

class PokemonConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromStatInfoList(value: List<StatInfo>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStatInfoList(value: String): List<StatInfo> = Json.decodeFromString(value)

    @TypeConverter
    fun fromEvolutionNodeList(value: List<EvolutionNode>): String = Json.encodeToString(value)

    @TypeConverter
    fun toEvolutionNodeList(value: String): List<EvolutionNode> = Json.decodeFromString(value)
}
