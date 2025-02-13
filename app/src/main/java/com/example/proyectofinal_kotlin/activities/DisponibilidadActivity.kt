package com.example.proyectofinal_kotlin.activities

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.data.DatabaseHelper

class DisponibilidadActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var layoutHorarios: LinearLayout
    private var selectedDate: String = ""
    private var selectedPista: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad)

        dbHelper = DatabaseHelper(this)
        layoutHorarios = findViewById(R.id.layoutHorarios)
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

        for (hora in horarios) {
            val btnHora = Button(this)
            btnHora.text = hora

            if (reservas.contains(hora)) {
                btnHora.isEnabled = false
                btnHora.text = "$hora (Ocupado)"
            } else {
                btnHora.setOnClickListener {
                    dbHelper.insertReserva(selectedPista, 20.0, selectedDate, hora, "Tarjeta",intent.getStringExtra("email").toString())
                    Toast.makeText(this, "Reserva realizada para $selectedDate a las $hora en $selectedPista", Toast.LENGTH_SHORT).show()
                    mostrarHorariosDisponibles()
                }
            }
            layoutHorarios.addView(btnHora)
        }
    }
}
