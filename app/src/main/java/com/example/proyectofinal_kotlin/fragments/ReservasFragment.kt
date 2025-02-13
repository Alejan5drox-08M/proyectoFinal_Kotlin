package com.example.proyectofinal_kotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.activities.DisponibilidadActivity
import com.example.proyectofinal_kotlin.data.DatabaseHelper

class ReservasFragment : Fragment() {
    private lateinit var spinnerPistas: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var textSelectedDate: TextView
    private lateinit var btnVerDisponibilidad: Button
    private lateinit var dbHelper: DatabaseHelper
    private var selectedDate: String = ""
    private var selectedPista: String = ""
    private var email: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reservas, container, false)

        spinnerPistas = view.findViewById(R.id.spinnerPistas)
        calendarView = view.findViewById(R.id.calendarView)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)
        btnVerDisponibilidad = view.findViewById(R.id.btnVerDisponibilidad)
        dbHelper = DatabaseHelper(requireContext())

        // Opciones del Spinner
        val opcionesPistas = arrayOf("Pádel", "Fútbol Sala", "Baloncesto", "Tenis")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesPistas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPistas.adapter = adapter

        // Capturar selección del Spinner
        spinnerPistas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPista = opcionesPistas[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPista = opcionesPistas[0] // Valor por defecto
            }
        }

        // Manejar selección de fecha en el CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            textSelectedDate.text = "Fecha seleccionada: $selectedDate"
        }

        // Botón para ver disponibilidad
        btnVerDisponibilidad.setOnClickListener {
            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una fecha primero", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireContext(), DisponibilidadActivity::class.java)
                intent.putExtra("fecha", selectedDate)
                intent.putExtra("pista", selectedPista)
                intent.putExtra("email", email)
                startActivity(intent)
            }
        }

        return view
    }
}

