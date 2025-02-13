package com.example.proyectofinal_kotlin.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.activities.LoginActivity

class SettingsFragment : Fragment() {
    private lateinit var switchTema: Switch
    private lateinit var btnCerrarSesion: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        switchTema = view.findViewById(R.id.switchTema)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
        sharedPreferences = requireActivity().getSharedPreferences("config", 0)

        // Cargar estado del switch
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)
        switchTema.isChecked = isDarkMode
        setAppTheme(isDarkMode)

        // Evento para cambiar el tema
        switchTema.setOnCheckedChangeListener { _, isChecked ->
            setAppTheme(isChecked)
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
        }

        // Evento para cerrar sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        return view
    }

    private fun setAppTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun cerrarSesion() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
