package com.example.proyectofinal_kotlin.data
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reservas.db"
        private const val DATABASE_VERSION = 5

        private const val TABLE_USERS = "usuarios"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"


        private const val TABLE_RESERVAS = "reservas"
        private const val COLUMN_RESERVA_ID = "id"
        private const val COLUMN_RESERVA_PISTA = "pista"
        private const val COLUMN_RESERVA_PRECIO = "precio"
        private const val COLUMN_RESERVA_FECHA = "fecha"
        private const val COLUMN_RESERVA_HORA = "hora"
        private const val COLUMN_RESERVA_METODO_PAGO = "metodo_pago"
        private const val COLUMN_RESERVA_USER_EMAIL = "user_email"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        val createReservasTable = """
            CREATE TABLE $TABLE_RESERVAS (
                $COLUMN_RESERVA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RESERVA_PISTA TEXT NOT NULL,
                $COLUMN_RESERVA_PRECIO REAL NOT NULL,
                $COLUMN_RESERVA_FECHA TEXT NOT NULL,
                $COLUMN_RESERVA_HORA TEXT NOT NULL,
                $COLUMN_RESERVA_METODO_PAGO TEXT NOT NULL,
                $COLUMN_RESERVA_USER_EMAIL TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_RESERVA_USER_EMAIL) REFERENCES $TABLE_USERS($COLUMN_USER_EMAIL) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createReservasTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }


    fun insertUser(email: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }


    fun getReservasByFechaYPista(fecha: String, pista: String): List<String> {
        val reservas = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT hora FROM reservas WHERE fecha = ? AND pista = ?", arrayOf(fecha, pista))

        while (cursor.moveToNext()) {
            val hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"))
            reservas.add(hora)
        }
        cursor.close()
        return reservas
    }

    fun insertReserva(pista: String, precio: Double, fecha: String, hora: String, metodoPago: String, userEmail: String) {
        val db = writableDatabase
        val query = "INSERT INTO reservas (pista, precio, fecha, hora, metodo_pago,user_email) VALUES (?, ?, ?, ?, ?, ?)"
        db.execSQL(query, arrayOf(pista, precio, fecha, hora, metodoPago,userEmail))
    }

    fun getReservasByUser(email: String): MutableList<Reserva> {
        val reservas = mutableListOf<Reserva>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id, pista, precio, fecha, hora, metodo_pago FROM reservas WHERE user_email = ?",
            arrayOf(email)
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val pista = cursor.getString(1)
            val precio = cursor.getDouble(2)
            val fecha = cursor.getString(3)
            val hora = cursor.getString(4)
            val metodoPago = cursor.getString(5)

            reservas.add(Reserva(id, pista, precio, fecha, hora, metodoPago))
        }
        cursor.close()
        return reservas
    }
    fun modificarReserva(idReserva: Int, nuevaFecha: String, nuevaHora: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("fecha", nuevaFecha)
            put("hora", nuevaHora)
        }
        db.update("reservas", values, "id = ?", arrayOf(idReserva.toString()))
        db.close()
    }
    fun obtenerHorasReservadas(pista: String, fecha: String): List<String> {
        val horasReservadas = mutableListOf<String>()
        val db = readableDatabase
        val query = "SELECT hora FROM reservas WHERE pista = ? AND fecha = ?"
        val cursor = db.rawQuery(query, arrayOf(pista, fecha))

        while (cursor.moveToNext()) {
            horasReservadas.add(cursor.getString(0))
        }

        cursor.close()
        db.close()
        return horasReservadas
    }
    fun getAllReservas(): List<Reserva> {
        val reservas = mutableListOf<Reserva>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM reservas", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val pista = cursor.getString(cursor.getColumnIndexOrThrow("pista"))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"))
            val metodoPago = cursor.getString(cursor.getColumnIndexOrThrow("metodo_pago"))

            reservas.add(Reserva(id, pista, precio, fecha, hora, metodoPago))
        }
        cursor.close()
        return reservas
    }
    fun eliminarReserva(idReserva: Int) {
        val db = this.writableDatabase
        db.delete("reservas", "id=?", arrayOf(idReserva.toString()))
        db.close()
    }

    data class Reserva(
        val id: Int,
        val pista: String,
        val precio: Double,
        val fecha: String,
        val hora: String,
        val metodoPago: String
    )
}
