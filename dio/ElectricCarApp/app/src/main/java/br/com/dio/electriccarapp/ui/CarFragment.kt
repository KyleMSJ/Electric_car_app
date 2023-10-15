package br.com.dio.electriccarapp.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import br.com.dio.electriccarapp.R
import br.com.dio.electriccarapp.data.CarsApi
import br.com.dio.electriccarapp.data.local.CarRepository
import br.com.dio.electriccarapp.domain.Carro
import br.com.dio.electriccarapp.ui.adapter.CarAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

class CarFragment : Fragment() {
    lateinit var fabCalcular: FloatingActionButton
    lateinit var listaCarros: RecyclerView
    lateinit var progress: ProgressBar
    lateinit var noInternetImage: ImageView
    lateinit var noInternetText: TextView
    lateinit var carsApi: CarsApi

    var carrosArray: ArrayList<Carro> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.car_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // chamado após o método onCreateView
        super.onViewCreated(view, savedInstanceState) /* Configuração e interação com os elementos visuais, definição de ouvintes de clique e outras tarefas relacionadas à interface do usuário.
Inicializar componentes de interface do usuário que foram inflados no onCreateView.*/
        setupRetrofit()
        setupView(view)
        //setupList() -> não faz mais sentido ser chamada aqui, visto que agora os dados dos carros serão obtidos via JSON da Web
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (checkForInternet(context)) {
            //callService() -> essa é outra forma de chamar serviço
            getAllCars()
        } else {
            emptyState()
        }
    }

    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://igorbag.github.io/cars-api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        carsApi = retrofit.create(CarsApi::class.java)
    }

    fun getAllCars() {
        carsApi.getAllCars().enqueue(object : Callback<List<Carro>> {
            override fun onResponse(call: Call<List<Carro>>, response: Response<List<Carro>>) {
                if (response.isSuccessful) {
                    progress.isVisible = false
                    noInternetImage.isVisible = false
                    noInternetText.isVisible = false

                    response.body()?.let {
                        setupList(it)
                    }
                } else {
                    Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Carro>>, t: Throwable) {
                Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun setupView(view: View) {
        view.apply {
            fabCalcular = findViewById(R.id.fab_calcular)
            listaCarros = findViewById(R.id.rv_lista_carros)
            progress = findViewById(R.id.pb_loader)
            noInternetImage = findViewById(R.id.iv_empty_state)
            noInternetText = findViewById(R.id.tv_no_wifi)
        }
    }

    fun emptyState() {
        progress.isVisible = false
        listaCarros.isVisible = false
        noInternetImage.isVisible = true
        noInternetText.isVisible = true
    }

    fun setupList(lista: List<Carro>) {
        //var adapter = CarAdapter(CarFactory.list) -> Mockando oos dados
        val carroAdapter = CarAdapter(lista)

        listaCarros.apply {
            isVisible = true
            adapter = carroAdapter
        }
        carroAdapter.carItemListener = { carro ->
            val isSaved = CarRepository(requireContext()).saveIfNotExist(carro)
        }
    }

    fun setupListeners() {
        fabCalcular.setOnClickListener {
            startActivity(Intent(context, CalcularAutonomiaActivity::class.java))
        }
    }

    fun callService() {
        val urlBase = "https://igorbag.github.io/cars-api/cars.json"
        progress.isVisible = true
        MyTask().execute(urlBase)
    }

    fun checkForInternet(context: Context?): Boolean {
        // Obtém uma referência ao ConnectivityManager usando o contexto fornecido
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager // O ConnectivityManager é responsável por gerenciar a conectividade de rede no dispositivo.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // verifica se a versão do sistema Android em execução é igual ou superior à versão 23 (Marshmallow).

            val network = connectivityManager.activeNetwork ?: return false /*obtém-se uma referência à rede ativa atual.
            Se não houver uma rede ativa, a função retorna false, indicando que não há conectividade.*/

            val activeNetowrk = connectivityManager.getNetworkCapabilities(network)
                ?: return false // obtém-se as capacidades da rede ativa

            return when {
                activeNetowrk.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetowrk.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else { // Este bloco de código é executado apenas se a versão do Android for anterior a Marshmallow (ou seja, SDK_INT < 23).
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    // Utilizar o retrofit como abstração do AsyncTask!
    inner class MyTask :
        AsyncTask<String, String, String>() { // <Entrada, valor passado para o método "onProgressUpdate", valor retornado pelo método "doInBackground">

        override fun onPreExecute() { // executado na thread principal antes da execução da tarefa assíncrona
            super.onPreExecute()
            Log.d("MyTask", "Inciando...")
            progress.isVisible = true
        }

        override fun doInBackground(vararg url: String?): String { /* método principal com a lógica a ser executada em segundo plano
        Recebe uma lista de argumentos do tipo "String" (no caso URLs) */
            var urlConnection: HttpURLConnection? =
                null // será usada para abrir e gerenciar uma conexão HTTP

            try {
                val urlBase =
                    URL(url[0]) // Cria um objeto URL com base na primeira URL passada como argumento para o método doInBackground.
                // url[0] é a primeira URL na lista de URLs passadas como argumento (vararg)

                urlConnection = urlBase.openConnection() as HttpURLConnection
                /** Abre uma conexão HTTP com a URL especificada.
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
                    var response = urlConnection.inputStream.bufferedReader()
                        .use { it.readText() } // maneira mais concisa e eficiente de ler o conteúdo.
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
                val jsonArray =
                    JSONTokener(values[0]).nextValue() as JSONArray // a primeira string no array 'values'
                // é convertida em um objeto JSONArray através da classe JSONTokener (permite analisar uma string JSON)

                for (i in 0 until jsonArray.length()) {
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
                        urlPhoto = urlPhoto,
                        isFavorite = false
                    )
                    carrosArray.add(model)
                    Log.d("Model ->", model.toString())
                }
                progress.isVisible = false
                noInternetImage.isVisible = false
                noInternetText.isVisible = false
                //setupList()

            } catch (ex: Exception) {
                Log.e("Erro ->", ex.message.toString())
            }
        }

    }
}