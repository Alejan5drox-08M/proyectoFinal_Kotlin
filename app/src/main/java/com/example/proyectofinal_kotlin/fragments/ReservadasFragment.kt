package com.example.proyectofinal_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal_kotlin.adapter.ReservaAdapter
import com.example.proyectofinal_kotlin.data.DatabaseHelper
import com.example.proyectofinal_kotlin.databinding.FragmentReservadasBinding

class ReservadasFragment : Fragment() {
    private var _binding: FragmentReservadasBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ReservaAdapter
    private var email: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservadasBinding.inflate(inflater, container, false)
        dbHelper = DatabaseHelper(requireContext())

        binding.recyclerViewReservas.layoutManager = LinearLayoutManager(requireContext())


        loadReservas()

        return binding.root
    }

    private fun loadReservas() {
        val reservas = dbHelper.getReservasByUser(email.toString()) // Obtener reservas de SQLite
        if (reservas.isEmpty()) {
            binding.recyclerViewReservas.visibility = View.GONE
            binding.textViewNoReservas.visibility = View.VISIBLE
        } else {
            binding.recyclerViewReservas.visibility = View.VISIBLE
            binding.textViewNoReservas.visibility = View.GONE
            adapter = ReservaAdapter(reservas,dbHelper)
            binding.recyclerViewReservas.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
