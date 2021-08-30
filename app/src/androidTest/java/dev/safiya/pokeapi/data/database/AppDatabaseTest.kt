package dev.safiya.pokeapi.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.safiya.pokeapi.model.PokemonResult
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest : TestCase() {

    private lateinit var pokemonDao: PokemonDao
    private lateinit var db: AppDatabase

    @Before
    public override fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        pokemonDao = db.getPokemonDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadSpend() = runBlocking {
        val pokemonResult = PokemonResult(
            "pikachu", emptyList(), 1, "https://pokeapi.co/api/v2/pokemon/1/"
        )
        pokemonDao.insert(pokemonResult)
        val pokemon = pokemonDao.getAllPokemon(1)
        assertThat(pokemon.contains(pokemonResult)).isTrue()


    }
}