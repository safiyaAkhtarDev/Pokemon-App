package dev.safiya.pokeapi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dev.safiya.pokeapi.data.Converters
import dev.safiya.pokeapi.model.PokemonResult
import javax.inject.Singleton


@Database(entities = [PokemonResult::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getPokemonDao(): PokemonDao

    companion object {
        @Volatile
        // Database Instance
        private var instance: AppDatabase? = null
        private val LOCK = Any()


        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        public fun getDatabase(): AppDatabase {
            return instance!!
        }
        // create database


        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "my_db.db"
            ).build()

    }
}