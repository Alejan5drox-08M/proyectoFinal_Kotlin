package com.example.proyectofinal_kotlin.adapter

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.data.DatabaseHelper
import java.util.*

class ReservaAdapter(
    private val reservas: MutableList<DatabaseHelper.Reserva>,
    private val dbHelper: DatabaseHelper,
    private val email: String
) : RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder>() {

    class ReservaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textPrecio: TextView = view.findViewById(R.id.textPrecio)
        val textFecha: TextView = view.findViewById(R.id.textFecha)
        val textHora: TextView = view.findViewById(R.id.textHora)
        val textMetodoPago: TextView = view.findViewById(R.id.textMetodoPago)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.elemento_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val reserva = reservas[position]
        val imageView = holder.itemView.findViewById<ImageView>(R.id.imageTipoReserva)

        when (reserva.pista) {
            "Pádel" -> imageView.setImageResource(R.drawable.ic_pista_padel)
            "Fútbol Sala" -> imageView.setImageResource(R.drawable.ic_pista_futbol)
            "Baloncesto" -> imageView.setImageResource(R.drawable.ic_pista_baloncesto)
            "Tenis" -> imageView.setImageResource(R.drawable.ic_pista_tenis)
        }

        holder.textPrecio.text = "Precio: ${reserva.precio}€"
        holder.textFecha.text = "Fecha: ${reserva.fecha}"
        holder.textHora.text = "Hora: ${reserva.hora}"
        holder.textMetodoPago.text = "Método de pago: ${reserva.metodoPago}"

        holder.btnEliminar.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Eliminar reserva")
                .setMessage("¿Estás seguro de que quieres eliminar esta reserva?")
                .setPositiveButton("Sí") { _, _ ->
                    eliminarReserva(position, reserva.id) // Llamar al nuevo método corregido
                    Toast.makeText(context, "Reserva eliminada y dinero devuelto.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.btnEditar.setOnClickListener {
            mostrarDialogoEdicion(holder.itemView.context, reserva, position)
        }
    }

    override fun getItemCount(): Int = reservas.size


    private fun eliminarReserva(position: Int, idReserva: Int) {
        val precioReserva = reservas[position].precio
        dbHelper.eliminarReserva(idReserva)
        reservas.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
        dbHelper.updateUserSaldo(email, dbHelper.getUserSaldo(email) + precioReserva)
    }


    private fun mostrarDialogoEdicion(context: Context, reserva: DatabaseHelper.Reserva, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_reserva, null)
        val datePickerButton: Button = dialogView.findViewById(R.id.btnSeleccionarFecha)
        val spinnerHora: Spinner = dialogView.findViewById(R.id.spinnerHora)

        val fechaSeleccionada = Calendar.getInstance()
        datePickerButton.text = reserva.fecha

        actualizarHorasDisponibles(context, spinnerHora, reserva.pista, reserva.fecha)

        datePickerButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val nuevaFecha = "$year-${month + 1}-$dayOfMonth"
                    datePickerButton.text = nuevaFecha

                    actualizarHorasDisponibles(context, spinnerHora, reserva.pista, nuevaFecha)
                },
                fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
            )
            val calendar = Calendar.getInstance()
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        AlertDialog.Builder(context)
            .setTitle("Modificar Reserva")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaFecha = datePickerButton.text.toString()
                val nuevaHora = spinnerHora.selectedItem?.toString()

                if (nuevaHora == null) {
                    Toast.makeText(context, "Por favor, selecciona una hora.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                modificarReserva(position, reserva.id, nuevaFecha, nuevaHora)
            }

            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarHorasDisponibles(context: Context, spinnerHora: Spinner, pista: String, fecha: String) {
        val horasDisponibles = obtenerHorasDisponibles(pista, fecha)

        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val horasFiltradas = if (fecha == currentDate) {
            horasDisponibles.filter {
                val horaParts = it.split(":")
                val hour = horaParts[0].toInt()
                val minute = horaParts[1].toInt()
                hour > currentHour || (hour == currentHour && minute > currentMinute)
            }
        } else {
            horasDisponibles
        }

        if (horasFiltradas.isEmpty()) {
            Toast.makeText(context, "No hay horarios disponibles para esta fecha", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, horasFiltradas)
        spinnerHora.adapter = adapter
    }

    private fun obtenerHorasDisponibles(pista: String, fecha: String): List<String> {
        val todasLasHoras = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
        val horasReservadas = dbHelper.obtenerHorasReservadas(pista, fecha)
        return todasLasHoras.filter { it !in horasReservadas }
    }


    private fun modificarReserva(position: Int, idReserva: Int, nuevaFecha: String, nuevaHora: String) {
        dbHelper.modificarReserva(idReserva, nuevaFecha, nuevaHora)

        reservas[position] = reservas[position].copy(fecha = nuevaFecha, hora = nuevaHora)
        notifyItemChanged(position)
    }
}
