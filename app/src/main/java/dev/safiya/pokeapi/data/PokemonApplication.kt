package dev.safiya.pokeapi.data

import android.app.Application
import dev.safiya.pokeapi.data.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonApplication @Inject constructor() : Application() {

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase() }
//    val repository by lazy { PokemonRepository(database.getPokemonDao()) }
}