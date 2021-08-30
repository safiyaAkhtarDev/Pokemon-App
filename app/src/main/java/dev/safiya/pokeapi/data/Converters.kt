package dev.safiya.pokeapi.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import dev.safiya.pokeapi.model.Abilities

class Converters {

    @TypeConverter
    fun listToJson(value: List<Abilities>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Abilities>::class.java).toList()
}