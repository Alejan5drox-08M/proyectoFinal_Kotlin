package com.example.proyectofinal_kotlin.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectofinal_kotlin.R
import com.example.proyectofinal_kotlin.data.DatabaseHelper
import com.example.proyectofinal_kotlin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var mibinding: ActivityLoginBinding
    lateinit var dbhelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mibinding = ActivityLoginBinding.inflate(layoutInflater)
        dbhelper= DatabaseHelper(this)
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
        val editTextEmail = mibinding.editTextEmail
        val editTextPassword = mibinding.editTextPassword
        val buttonLogin = mibinding.buttonLogin
        if(!dbhelper.getUser("admin","admin")||!dbhelper.getUser("aleh","aleh")){
            dbhelper.insertUser("admin","admin")
            dbhelper.insertUser("aleh","aleh")
        }
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val userExists = dbhelper.getUser(email, password)
                if (userExists) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
