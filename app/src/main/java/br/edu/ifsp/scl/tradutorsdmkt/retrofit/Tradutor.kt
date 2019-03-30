package br.edu.ifsp.scl.tradutorsdmkt.retrofit

import br.edu.ifsp.scl.tradutorsdmkt.MainActivity
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_ID_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_ID_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_KEY_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_KEY_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.URL_BASE
import br.edu.ifsp.scl.tradutorsdmkt.model.Resposta
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.jetbrains.anko.design.snackbar
import org.json.JSONException
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response

class Tradutor(val mainActivity: MainActivity) {
    // Cria um objeto Retrofit usando a URL base.
    //val retrofit: Retrofit = Retrofit.Builder().baseUrl(URL_BASE).build()

    // Cliente Http que conterá o interceptador de requisição para cabeçalhos dinâmicos
    val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    // Instanciando o cliente HTTP
    init {
        // Adiciona um interceptador que é um objeto de uma classe anônima
        okHttpClientBuilder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                // Resgatando requisição interceptada
                val reqInterceptada: Request = chain.request()
                // Criando nova requisição a partir da interceptada e adicionando campos de cabeçalho
                val novaReq: Request = reqInterceptada.newBuilder()
                    .header(APP_ID_FIELD, APP_ID_VALUE)
                    .header(APP_KEY_FIELD, APP_KEY_VALUE)
                    .method(reqInterceptada.method(), reqInterceptada.body())
                    .build()
                // retornando a nova requisição preenchdia
                return chain.proceed(novaReq)
            }
        })
    }
    // Novo objeto Retrofit usando a URL base e o HttpClient com interceptador
    val retrofit: Retrofit = Retrofit.Builder().baseUrl(URL_BASE).client(okHttpClientBuilder.build()).build()
    // Cria um objeto, a partir da Interface Retrofit, que contém as funções de requisição
    val tradutorApi: TradutorApi = retrofit.create(TradutorApi::class.java)

    fun traduzir(palavraOrigem: String, idiomaOrigem: String, idiomaDestino: String) {
        /*Chama a função de requisição definida na Interface passando os parâmetros escolhidos pelo usuário e
        enfileira a requisição que recebe um objeto de uma implementação anônima de Callback<ResponseBody>*/
        tradutorApi.getTraducaoByPath(idiomaOrigem, palavraOrigem, idiomaDestino).enqueue(
            object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) { // Função chamada no caso de erro
                    mainActivity.mainLl.snackbar("Erro na resposta - Retrofit")
                }
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) { // Função chamada no caso de resposta
                    try {
                        val gson: Gson = Gson() // Cria um objeto Gson que consegue fazer reflexão de um Json para Data Class
                        val resposta: Resposta = gson.fromJson(response.body()?.string(), Resposta::class.java) // Reflete a resposta (que é um Json) num objeto da classe Resposta
                        var traduzidoSb = StringBuffer() // StringBuffer para armazenar o resultado das traduções
                        // Parseando o objeto e adicionando as traduções ao StringBuffer O(N^5)
                        resposta.results?.forEach {
                            it?.lexicalEntries?.forEach {
                                it?.entries?.forEach {
                                    it?.senses?.forEach {
                                        it?.translations?.forEach {
                                            traduzidoSb.append("${it?.text}, ")
                                        }
                                    }
                                }
                            }
                        }
                        // Enviando as tradução ao Handler da thread de UI para serem mostrados na tela
                        mainActivity.tradutoHandler.obtainMessage(MainActivity.codigosMensagen.RESPOSTA_TRADUCAO, traduzidoSb.toString().substringBeforeLast(',')).sendToTarget()
                    } catch (jse: JSONException) {
                        mainActivity.mainLl.snackbar("Erro na resposta - Retrofit")
                    }
                }
            } // Fim da classe anônima
        ) // Fim dos parâmetros de enqueue
    } // Fim da função traduzir
}
