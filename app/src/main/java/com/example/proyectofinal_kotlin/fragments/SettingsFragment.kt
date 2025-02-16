package com.example.proyectofinal_kotlin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.activities.LoginActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject

class SettingsFragment : Fragment() {
    private lateinit var switchTema: Switch
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnGooglePay: Button
    private lateinit var textSaldo: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var paymentsClient: PaymentsClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        initGooglePay()

        switchTema = view.findViewById(R.id.switchTema)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
        btnGooglePay = view.findViewById(R.id.btnGooglePay)
        textSaldo = view.findViewById(R.id.textSaldo)
        sharedPreferences = requireActivity().getSharedPreferences("config", 0)

        val saldoActual = sharedPreferences.getFloat("saldo", 50.0f)
        actualizarSaldoTexto(saldoActual)

        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)
        switchTema.isChecked = isDarkMode
        setAppTheme(isDarkMode)

        switchTema.setOnCheckedChangeListener { _, isChecked ->
            setAppTheme(isChecked)
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        btnGooglePay.setOnClickListener {
            mostrarOpcionesRecarga()
        }
        return view
    }


    private fun initGooglePay() {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        paymentsClient = Wallet.getPaymentsClient(requireActivity(), walletOptions)
    }

    private fun setAppTheme(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun cerrarSesion() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun actualizarSaldoTexto(saldo: Float) {
        textSaldo.text = "Saldo: €${"%.2f".format(saldo)}"
    }
    fun loadJsonFromRawResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readText() }
    }
    private fun pagarConGooglePay(monto: Float) {
        val paymentJson = JSONObject(loadJsonFromRawResource(R.raw.payments))

        val transactionInfo = JSONObject().apply {
            put("totalPrice", "%.2f".format(monto))
            put("totalPriceStatus", "FINAL")
            put("currencyCode", "EUR")
        }

        paymentJson.put("transactionInfo", transactionInfo)

        val paymentDataRequest = PaymentDataRequest.fromJson(paymentJson.toString())

        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            requireActivity(),
            999 // Request code
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    Toast.makeText(requireContext(), "Pago exitoso", Toast.LENGTH_SHORT).show()
                    recargarSaldo(paymentData!!.toJson().toFloat())
                }
                AppCompatActivity.RESULT_CANCELED -> Toast.makeText(requireContext(), "Pago cancelado", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(requireContext(), "Pago fallido", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun pedirMontoPersonalizado() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Introduce el monto (€)"

        AlertDialog.Builder(requireContext())
            .setTitle("Recargar con Google Pay")
            .setView(input)
            .setPositiveButton("Pagar") { _, _ ->
                val monto = input.text.toString().toFloatOrNull()
                if (monto != null && monto > 0) {
                    pagarConGooglePay(monto)
                } else {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarOpcionesRecarga() {
        val opciones = arrayOf("5€", "10€", "20€", "50€", "Google Pay (Otro monto)")
        val cantidades = floatArrayOf(5.0f, 10.0f, 20.0f, 50.0f, -1.0f)

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una cantidad para recargar")
            .setItems(opciones) { _, which ->
                if (cantidades[which] == -1.0f) {
                    pedirMontoPersonalizado()
                } else {
                    recargarSaldo(cantidades[which])
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun recargarSaldo(cantidad: Float) {
        val saldoActual = sharedPreferences.getFloat("saldo", 50.0f)
        val nuevoSaldo = saldoActual + cantidad
        sharedPreferences.edit().putFloat("saldo", nuevoSaldo).apply()
        actualizarSaldoTexto(nuevoSaldo)
        Toast.makeText(requireContext(), "Saldo recargado: €$cantidad", Toast.LENGTH_SHORT).show()
    }

    fun descontarSaldo(cantidad: Float): Boolean {
        val saldoActual = sharedPreferences.getFloat("saldo", 50.0f)
        return if (saldoActual >= cantidad) {
            val nuevoSaldo = saldoActual - cantidad
            sharedPreferences.edit().putFloat("saldo", nuevoSaldo).apply()
            actualizarSaldoTexto(nuevoSaldo)
            true
        } else {
            Toast.makeText(requireContext(), "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
