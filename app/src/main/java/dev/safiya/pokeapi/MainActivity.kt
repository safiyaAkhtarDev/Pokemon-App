package dev.safiya.pokeapi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.safiya.pokeapi.databinding.ActivityMainBinding
import dev.safiya.pokeapi.data.database.AppDatabase
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Singleton instance for database

        AppDatabase.invoke(this)
        // Gets appComponent from MyApplication available in the base Gradle module

        setContentView(binding.root)
    }
}