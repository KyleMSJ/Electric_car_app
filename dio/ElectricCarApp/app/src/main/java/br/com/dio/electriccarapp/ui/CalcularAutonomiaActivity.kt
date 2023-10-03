package br.com.dio.electriccarapp.ui

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.dio.electriccarapp.R
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class CalcularAutonomiaActivity: AppCompatActivity() {

    lateinit var preco: EditText
    lateinit var kmPercorrido: EditText
    lateinit var btnCalcular: Button
    lateinit var resultado: TextView
    lateinit var btnClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calcular_autonomia)

        setupView()
        setupListeners()
    }

    fun setupView(){
        kmPercorrido = findViewById(R.id.et_km_percorrido)
        preco = findViewById(R.id.et_preco_kwh)
        btnCalcular = findViewById(R.id.btn_calcular)
        resultado = findViewById(R.id.tv_resultado)
        btnCalcular = findViewById(R.id.btn_calcular)
        btnClose = findViewById(R.id.iv_close)
    }
    fun setupListeners(){
        btnCalcular.setOnClickListener{
            calcular()
        }

        btnClose.setOnClickListener{
            finish()
        }
    }

    fun calcular(){
        val preco = preco.text.toString().toFloat()
        val km = kmPercorrido.text.toString().toFloat()

        val result = preco / km
        resultado.text = result.toString()
    }


}