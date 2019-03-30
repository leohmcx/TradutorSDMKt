package br.edu.ifsp.scl.tradutorsdmkt.retrofit

import retrofit2.Call
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface TradutorApi {
    /* Com campos de cabeçalho estáticos, é possível defini-los na Interface antes da função de requisição */
    /*@Headers(
        "app_id: 71...", // Preencha com seu app_id
        "app_key: ff37..." // Preencha com seu app_key
    )*/
    /* Função de requisição. Começa com uma anotação denotando o método HTTP e endpoint da requisição
    As anotações dos parâmetros da função descrevem o mapeamento para parâmetros do Path da requisição.*/
    @GET("entries/{source_translation_language}/{word_id}/translations={target_translation_language}")
    fun getTraducaoByPath(
        @Path("source_translation_language") stl: String,// stl -> source_translation_language
        @Path("word_id") wId: String, // wId -> word_id
        @Path("target_translation_language") ttl: String // ttl -> target_translation_language
    ): Call<ResponseBody>
    // Outras funções de requisição poderiam ser colocadas aqui inclusive para outros métodos.
}
