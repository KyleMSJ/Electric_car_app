package br.com.dio.electriccarapp.data

import br.com.dio.electriccarapp.domain.Carro

object CarFactory {

    val list = listOf(
        Carro(
            id = 1,
            preco = "R$300.000,00",
            bateria = "300 kWh",
            potencia = "200cv",
            recarga = "30min",
            urlPhoto = "www.google.com.br"
        ),
        Carro(
            id = 2,
            preco = "R$200.000,00",
            bateria = "200 kWh",
            potencia = "150cv",
            recarga = "20min",
            urlPhoto = "www.google.com.br"
        )
    )

    // Verbos HTTP
        // - GET -> Para recuperar informações
        // - POST -> Para enviar informações para o servidor
        // - DELETE -> Para deletar algum recurso
        // - PUT -> Alterar uma entidade como um todo
        // - PATCH -> Alterar um atributo da entidade
}