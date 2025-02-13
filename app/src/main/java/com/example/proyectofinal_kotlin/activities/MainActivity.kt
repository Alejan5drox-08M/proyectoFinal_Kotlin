package com.example.proyectofinal_kotlin.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.fragments.ReservadasFragment
import com.example.proyectofinal_kotlin.fragments.ReservasFragment
import com.example.proyectofinal_kotlin.fragments.SettingsFragment
import com.example.proyectofinal_kotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity: AppCompatActivity() {
    lateinit var mitoolbar: Toolbar
    lateinit var botnav: BottomNavigationView
    lateinit var mibinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mibinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mibinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferences: SharedPreferences = getSharedPreferences("config", 0)
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)

        // Aplicar el tema guardado
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        if (savedInstanceState == null) {
            val email = intent.getStringExtra("email") ?: ""
            val fragment = ReservadasFragment().apply {
                arguments = Bundle().apply {
                    putString("email", email)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
        }
        inicializar_toolbar()
        inicializar_navegacion_inferior()
    }
    private fun inicializar_navegacion_inferior() {
        this.botnav = mibinding.bottomnav
        botnav.setOnItemSelectedListener {
            val email = intent.getStringExtra("email") ?:""
            when(it.itemId) {
                R.id.tusreservas -> {
                    val fragment = ReservadasFragment().apply {
                        arguments = Bundle().apply {
                            putString("email", email)
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .commit()
                    mitoolbar.menu.clear()
                    mitoolbar.menu.close()
                }

                R.id.reservas -> {
                    val fragment = ReservasFragment().apply {
                        arguments = Bundle().apply {
                            putString("email", email)
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .commit()
                    mitoolbar.menu.clear()
                    mitoolbar.menu.close()
                }

                R.id.ajustes -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingsFragment())
                    }
                    mitoolbar.menu.clear()
                    mitoolbar.menu.close()
                }
            }
            true
        }
    }
    private fun inicializar_toolbar() {
        this.mitoolbar=findViewById(R.id.toolbar)
        this.mitoolbar.setTitle("Sportify Reserve")
        this.mitoolbar.setLogo(R.drawable.logo)
        setSupportActionBar(this.mitoolbar)
    }
}