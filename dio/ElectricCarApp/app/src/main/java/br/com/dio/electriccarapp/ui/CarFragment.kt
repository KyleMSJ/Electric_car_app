package br.com.dio.electriccarapp.ui

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64InputStream
import android.util.JsonReader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import br.com.dio.electriccarapp.R
import br.com.dio.electriccarapp.data.CarFactory
import br.com.dio.electriccarapp.domain.Carro
import br.com.dio.electriccarapp.ui.adapter.CarAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class CarFragment : Fragment() {
    lateinit var fabCalcular: FloatingActionButton
    lateinit var listaCarros: RecyclerView

    var carrosArray: ArrayList<Carro> =  ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.car_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
        callService()
        //setupList() -> não faz mais sentido ser chamada aqui, visto que agora os dados dos carros serão obtidos via JSON da Web
        setupListeners()
    }

    fun setupView(view: View) {
        view.apply {
            fabCalcular = findViewById(R.id.fab_calcular)
            listaCarros = findViewById(R.id.rv_lista_carros)
        }
    }

    fun setupList() {
        //var adapter = CarAdapter(CarFactory.list) -> Mockando oos dados
        var adapter = CarAdapter(carrosArray)
        listaCarros.adapter = adapter
    }

    fun setupListeners(){
        fabCalcular.setOnClickListener{
            startActivity(Intent(context,CalcularAutonomiaActivity::class.java))
        }
    }

    fun callService() {
        val urlBase = "https://igorbag.github.io/cars-api/cars.json"
        MyTask().execute(urlBase)
    }

    inner class MyTask : AsyncTask<String, String, String>() { // <Entrada, valor passado para o método "onProgressUpdate", valor retornado pelo método "doInBackground">

        override fun onPreExecute() { // executado na thread principal antes da execução da tarefa assíncrona
            super.onPreExecute()
            Log.d("MyTask", "Inciando...")
        }
        override fun doInBackground(vararg url: String?): String { /* método principal com a lógica a ser executada em segundo plano
        Recebe uma lista de argumentos do tipo "String" (no caso URLs) */
            var urlConnection: HttpURLConnection? = null // será usada para abrir e gerenciar uma conexão HTTP

            try {
                val urlBase = URL(url[0]) // Cria um objeto URL com base na primeira URL passada como argumento para o método doInBackground.
                // url[0] é a primeira URL na lista de URLs passadas como argumento (vararg)

                urlConnection = urlBase.openConnection() as HttpURLConnection /** Abre uma conexão HTTP com a URL especificada.
                Este é um passo fundamental para iniciar uma conexão com um servidor web.
                A expressão "as HttpURLConnection" é usada para fazer um cast do objeto retornado por openConnection() para o tipo HttpURLConnection,
                que é a classe específica usada para manipular conexões HTTP.
                 */
                urlConnection.connectTimeout = 60000 // 60 segundos
                urlConnection.readTimeout = 60000
                urlConnection.setRequestProperty( // permite especificar informações adicionais sobre a solicitação HTTP que está sendo feita
                    "Accept", // chave -> usado para indicar ao servidor o tipo de conteúdo que o cliente (ou navegador) deseja receber em resposta à solicitação.
                    "application/json" // valor -> indica que o cliente está solicitando que o servidor envie a resposta no formato JSON, se possível.
                )

                val responseCode = urlConnection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) { // Código 200
                    var response = urlConnection.inputStream.bufferedReader().use { it.readText() } // maneira mais concisa e eficiente de ler o conteúdo.
                    publishProgress(response) // notifica a thread principal sobre o progresso da tarefa assíncrona
                } else {
                    Log.e("Erro", "Serviço indisponível no momento ... ")
                }
            } catch (ex: Exception) {
                Log.e("Erro", "Erro ao realizar processamento ....")
            } finally { // garante que a conexão seja desconectada, independentemente de ter ocorrido um erro ou não
                urlConnection?.disconnect()  // se 'urlConnection' não for nulo, desconecta
            }

            return " "
        }

        override fun onProgressUpdate(vararg values: String?) { /* o método é chamado ao chamar 'publishProgress' acima
        Recebe valores do tipo String (progresso da tarefa) e pode ser usado para atualizar a UI com informações do progresso */
            try {
                val jsonArray = JSONTokener(values[0]).nextValue() as JSONArray // a primeira string no array 'values'
                // é convertida em um objeto JSONArray através da classe JSONTokener (permite analisar uma string JSON)

                for ( i in 0 until jsonArray.length()) {
                    val id = jsonArray.getJSONObject(i).getString("id")
                    Log.d("ID ->", id)

                    val preco = jsonArray.getJSONObject(i).getString("preco")
                    Log.d("Preço ->", preco)

                    val bateria = jsonArray.getJSONObject(i).getString("bateria")
                    Log.d("Bateria ->", bateria)

                    val potencia = jsonArray.getJSONObject(i).getString("potencia")
                    Log.d("Potência ->", potencia)

                    val recarga = jsonArray.getJSONObject(i).getString("recarga")
                    Log.d("Recarga ->", recarga)

                    val urlPhoto = jsonArray.getJSONObject(i).getString("urlPhoto")
                    Log.d("urlPhoto ->", urlPhoto)
                    val model = Carro(
                        id = id.toInt(),
                        preco = preco,
                        bateria = bateria,
                        potencia = potencia,
                        recarga = recarga,
                        urlPhoto = urlPhoto
                    )
                    carrosArray.add(model)
                    Log.d("Model ->", model.toString())
                }
                setupList()

            } catch (ex: Exception) {
                Log.e("Erro ->", ex.message.toString())
            }
        }

    }
}