package br.com.dio.electriccarapp.domain

data class Carro (
    val id: Int,
    val preco: String,
    val bateria: String,
    val potencia: String,
    val recarga: String,
    val urlPhoto: String
)