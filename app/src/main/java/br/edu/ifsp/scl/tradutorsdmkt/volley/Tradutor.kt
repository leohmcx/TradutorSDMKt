package br.edu.ifsp.scl.tradutorsdmkt.volley

import android.util.Log
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_ID_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_ID_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_KEY_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.APP_KEY_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.END_POINT
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.Constantes.URL_BASE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_TRADUCAO
import br.edu.ifsp.scl.tradutorsdmkt.R
import br.edu.ifsp.scl.tradutorsdmkt.model.Resposta
import br.edu.ifsp.scl.tradutorsdmkt.model.Translation
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.snackbar
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberExtensionProperties
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.functions

class Tradutor(val mainActivity: MainActivity) {
    fun traduzir(palavraOrigem: String, idiomaOrigem: String, idiomaDestino: String) {
        // Monta uma String com uma URL a partir das constantes e parâmetros do usuário
        val urlSb = StringBuilder(URL_BASE)
        with(urlSb) {
            append("${END_POINT}/")
            append("${idiomaOrigem}/")
            append("${palavraOrigem}/")
            append("translations=${idiomaDestino}")
        }
        val url = urlSb.toString()
        val filaRequisicaoTraducao: RequestQueue =
            Volley.newRequestQueue(mainActivity) // Cria uma fila de requisições Volley para enviar a requisição
        var traducaoJORequest: JsonObjectRequest =
        // Monta a requisição que será colocada na fila. Esse objeto é uma instância de uma classe anônima
            object : JsonObjectRequest(
                Request.Method.GET, // Método HTTP de requisição
                url,                // URL
                null,   // Objeto de requisição - somente em POST
                RespostaListener(), // Listener para tratar resposta
                ErroListener()      // Listener para tratar erro
            ) {
                // Corpo do objeto
                // Sobreescrevendo a função para passar cabeçalho na requisição
                override fun getHeaders(): MutableMap<String, String> {
                    // Cabeçalho composto por Map com app_id, app_key e seus valores
                    var parametros: MutableMap<String, String> = mutableMapOf()
                    parametros.put(APP_ID_FIELD, APP_ID_VALUE)
                    parametros.put(APP_KEY_FIELD, APP_KEY_VALUE)
                    return parametros
                }
            }
        // Adiciona a requisição a fila
        filaRequisicaoTraducao.add(traducaoJORequest)
    }

    /**
     *  Trata a resposta de uma requisição quando o acesso ao WS foi realizado.
     *  Complexidade de O(N^5). Pode causar problemas de desempenho com
     *  respostas muito grandes
     * */
    inner class RespostaListener : Response.Listener<JSONObject> {
        override fun onResponse(response: JSONObject?) {
            try {
                val gson: Gson = Gson()  // Cria um objeto Gson que consegue fazer reflexão de um Json para Data Class
                val resposta: Resposta = gson.fromJson(response.toString(), Resposta::class.java) // Reflete a resposta (que é um Json) num objeto da classe Resposta
                var traduzidoSb = StringBuffer() // StringBuffer para armazenar o resultado das traduções
                // Parseando o objeto e adicionando as traduções ao StringBuffer, O(N^5)
                resposta.results?.forEach {
                    it?.lexicalEntries?.forEach {
                        it?.entries?.forEach {
                            it?.senses?.forEach {
                                it?.translations?.forEach {
                                    traduzidoSb.append("${it?.text}, ")

                                    // Reflexão de classe Java e Kotlin
                                    val clTransJ: Class<Translation> = Translation::class.java
                                    val clTransK: KClass<Translation> = clTransJ.kotlin

                                    // Classes
                                    Log.v(mainActivity.getString(R.string.app_name), "Nome da Classe Java: ${clTransJ.name}")
                                    Log.v(mainActivity.getString(R.string.app_name), "Nome da Classe Kotlin: ${clTransK.qualifiedName}")

                                    // Atributos e propriedades
                                    clTransJ.declaredFields.forEach {
                                        Log.v(mainActivity.getString(R.string.app_name), "Nome e tipo do atributo java: ${it.name}, ${it.type}")
                                    }

                                    clTransK.declaredMemberExtensionProperties.forEach {
                                        Log.v(mainActivity.getString(R.string.app_name), "Nome e tipo da propriedade Kotlin: ${it.name}, ${it.returnType}")
                                    }

                                    // Métodos e Funções.
                                    clTransJ.declaredMethods.forEach {
                                        Log.v(mainActivity.getString(R.string.app_name), "Nome e tipo do método java: ${it.name}, ${it.returnType}")
                                    }

                                    clTransK.functions.forEach {
                                        Log.v(mainActivity.getString(R.string.app_name), "Nome e tipo da função Kotlin: ${it.name}, ${it.returnType}")
                                    }

                                    val objTransJ: Translation = clTransJ.newInstance()
                                    val objTransK: Translation = clTransK.createInstance()

                                    objTransJ.text = "Teste Java"
                                    objTransK.text = "Teste Kotlin"

                                    val getTextJ: Method = clTransJ.getDeclaredMethod("getText")
                                    val getTextK: KProperty1.Getter<Translation, Any?> = clTransK.declaredMemberProperties.single{ it.name == "text"}.getter

                                    // Classes
                                    //Log.v(mainActivity.getString(R.string.app_name), "Invocando metodo Java: ${getTextJ.invoke(clTransJ)}")
                                    //Log.v(mainActivity.getString(R.string.app_name), "Invocando função Kotlin: ${getTextK.call(clTransK)}")
                                }
                            }
                        }
                    }
                }
                // Enviando as tradução ao Handler da thread de UI para serem mostrados na tela
                mainActivity.tradutoHandler.obtainMessage(RESPOSTA_TRADUCAO, traduzidoSb.toString().substringBeforeLast(',')).sendToTarget()
            } catch (jse: JSONException) {
                mainActivity.mainLl.snackbar("Erro na conversão JSON")
            }
        }
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Usa um Desserializador O(N^2)
    inner class RespostaListener : Response.Listener<JSONObject> {
        override fun onResponse(response: JSONObject?) {
            try {
                val gsonBuilder: GsonBuilder = GsonBuilder() // Usa um builder que usa o desserializador personalizado para criar um objeto Gson
                val listTranslationType = object : TypeToken<List<Translation>>() {}.type // Usa reflexão para extrair o tipo da classe de um List<Translation>
                gsonBuilder.registerTypeAdapter(listTranslationType, TranslationListDeserializer()) // Seta o desserializador personalizado no builder
                val listTranslation: List<Translation> =  // Usa o builder para criar um Gson e usa o Gson para converter o Json de resposta numa lista de
                    gsonBuilder.create().fromJson(response.toString(), listTranslationType) //Translation usando o desserializador personalizado.
                val listTranslationString: StringBuffer = StringBuffer() // Extrai somente o texto dos objetos Translation
                listTranslation.forEach { listTranslationString.append("${it.text}, ") }
                mainActivity.tradutoHandler.obtainMessage(RESPOSTA_TRADUCAO, listTranslationString.toString().substringBeforeLast(',')).sendToTarget()
            } catch (je: JSONException) {
                mainActivity.mainLl.snackbar("Erro na conversão JSON")
            }
        }
    }*/

    // Trata erros na requisição ao WS
    inner class ErroListener : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            mainActivity.mainLl.snackbar("Erro na requisição: ${error.toString()}")
        }
    }

}