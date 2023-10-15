package br.com.dio.electriccarapp.data

import br.com.dio.electriccarapp.domain.Carro
import retrofit2.Call
import retrofit2.http.GET

interface CarsApi { // para o Retrofit funcionar adequadamente

    @GET("cars.json")
    fun getAllCars(): Call<List<Carro>>

}