package com.example.proyectofinal_kotlin.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.data.DatabaseHelper
import com.example.proyectofinal_kotlin.fragments.SettingsFragment

class DisponibilidadActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var layoutHorarios: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedDate: String = ""
    private var selectedPista: String = ""
    private var precioReserva = 20.0f // Precio por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad)

        dbHelper = DatabaseHelper(this)
        layoutHorarios = findViewById(R.id.layoutHorarios)
        sharedPreferences = getSharedPreferences("config", 0)

        selectedDate = intent.getStringExtra("fecha").toString()
        selectedPista = intent.getStringExtra("pista").toString()

        val textFecha = findViewById<TextView>(R.id.textFecha)
        textFecha.text = "Disponibilidad para: $selectedDate\nPista: $selectedPista"

        mostrarHorariosDisponibles()
    }

    private fun mostrarHorariosDisponibles() {
        layoutHorarios.removeAllViews()

        val horarios = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
        val reservas = dbHelper.getReservasByFechaYPista(selectedDate, selectedPista)
        val saldoActual = sharedPreferences.getFloat("saldo", 50.0f) // Obtener saldo

        for (hora in horarios) {
            val btnHora = Button(this)
            btnHora.text = hora

            if (reservas.contains(hora)) {
                btnHora.isEnabled = false
                btnHora.text = "$hora (Ocupado)"
            } else {
                btnHora.setOnClickListener {
                    if (saldoActual >= precioReserva) {
                        // Restar el saldo y guardar
                        val nuevoSaldo = saldoActual - precioReserva
                        sharedPreferences.edit().putFloat("saldo", nuevoSaldo).apply()

                        // Guardar reserva
                        dbHelper.insertReserva(
                            selectedPista, precioReserva.toDouble(), selectedDate, hora,
                            "Tarjeta", intent.getStringExtra("email").toString()
                        )

                        // Actualizar saldo en SettingsFragment
                        val settingsFragment = supportFragmentManager
                            .findFragmentByTag("SettingsFragment") as? SettingsFragment
                        settingsFragment?.descontarSaldo(nuevoSaldo)

                        Toast.makeText(
                            this, "Reserva realizada. Nuevo saldo: €${nuevoSaldo}",
                            Toast.LENGTH_SHORT
                        ).show()

                        mostrarHorariosDisponibles()
                    } else {
                        Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutHorarios.addView(btnHora)
        }
    }
}
