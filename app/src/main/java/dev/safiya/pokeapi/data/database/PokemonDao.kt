package dev.safiya.pokeapi.data.database

import androidx.room.*


import dev.safiya.pokeapi.model.PokemonResult

@Dao
interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PokemonResult)

    @Delete
    suspend fun delete(item: PokemonResult)

    @Query("SELECT * FROM PokemonResult where page=:pageno")
    suspend fun getAllPokemon(pageno: Int): List<PokemonResult>

    @Query("SELECT COUNT(id) FROM PokemonResult")
    suspend fun getPokemonCount(): Int

    @Query("SELECT page FROM PokemonResult ORDER BY page DESC LIMIT 1")
    suspend fun getPokemonPage(): Int

    @Query("SELECT * FROM PokemonResult ORDER BY name ASC")
    suspend fun getAtoZPokemon(): List<PokemonResult>

    @Query("SELECT * FROM PokemonResult where page=:pageno ORDER BY id ASC")
    suspend fun get1toNPokemon(pageno: Int): List<PokemonResult>

    @Query("SELECT * FROM PokemonResult where name Like:pokemon OR id =:pokemon OR abilities Like '%' || :pokemon || '%'")
    suspend fun getSearchedPokemon(pokemon: String): List<PokemonResult>

}