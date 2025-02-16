package com.example.proyectofinal_kotlin.activities

import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.data.DatabaseHelper
import com.example.proyectofinal_kotlin.fragments.SettingsFragment

class DisponibilidadActivity : AppCompatActivity() {
    lateinit var mitoolbar: Toolbar
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var layoutHorarios: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedDate: String = ""
    private var selectedPista: String = ""
    private var precioReserva = 20.0f

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
        inicializar_toolbar()
        setSupportActionBar(findViewById(R.id.toolbarDispo))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun mostrarHorariosDisponibles() {
        layoutHorarios.removeAllViews()

        val horarios = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
        val reservas = dbHelper.getReservasByFechaYPista(selectedDate, selectedPista)
        val saldoActual = dbHelper.getUserSaldo(intent.getStringExtra("email").toString())


        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"


        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        for (hora in horarios) {
            val horaParts = hora.split(":")
            val hour = horaParts[0].toInt()
            val minute = horaParts[1].toInt()

            if (selectedDate == currentDate) {
                if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
                    continue
                }
            }

            val btnHora = Button(this)
            btnHora.text = hora

            if (reservas.contains(hora)) {
                btnHora.isEnabled = false
                btnHora.text = "$hora (Ocupado)"
            } else {
                btnHora.setOnClickListener {
                    if (saldoActual >= precioReserva) {
                        val nuevoSaldo = saldoActual - precioReserva
                        dbHelper.updateUserSaldo(intent.getStringExtra("email").toString(), nuevoSaldo)

                        dbHelper.insertReserva(
                            selectedPista, precioReserva.toDouble(), selectedDate, hora,
                            "Tarjeta", intent.getStringExtra("email").toString()
                        )

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


    private fun inicializar_toolbar() {
        this.mitoolbar=findViewById(R.id.toolbarDispo)
        this.mitoolbar.setTitle("Sportify Reserve")
        this.mitoolbar.setLogo(R.drawable.logo)
        setSupportActionBar(this.mitoolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return true
    }
}
