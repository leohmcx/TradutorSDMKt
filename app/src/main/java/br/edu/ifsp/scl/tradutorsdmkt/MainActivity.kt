package br.edu.ifsp.scl.tradutorsdmkt

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_TRADUCAO
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity() {
    object codigosMensagen {
        // Constante usada para envio de mensagens ao Handler
        val RESPOSTA_TRADUCAO = 0
    }

    object Constantes {
        // Constantes usadas para acesso ao WS de tradução
        val URL_BASE = "https://od-api.oxforddictionaries.com/api/v1/"
        val END_POINT = "entries"
        val APP_ID_FIELD = "app_id"
        val APP_ID_VALUE = "PREENCHER_COM_SEU_APP_ID" // Preencher com seu app_id
        val APP_KEY_FIELD = "app_key"
        val APP_KEY_VALUE = "PREENCHER_COM_SEU_APP_KEY" // Preeencher com seu app_key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tradutoHandler = TradutoHandler() // Instancia o handler da thread de UI usado pelo tradutor
        idiomaOrigemSp.adapter = ArrayAdapter( // Cria e seta um Adapter com os idiomas de origem para um Spinner
            this,
            android.R.layout.simple_spinner_item,
            idiomas
        )
        idiomaOrigemSp.setSelection(0) //pt
        idiomaDestinoSp.adapter = ArrayAdapter(  // Cria e seta um Adapter com os idiomas de origem para um Spinner
            this,
            android.R.layout.simple_spinner_item,
            idiomas
        )
        idiomaDestinoSp.setSelection(1) //en
        // Seta o Listener para o botão
        traduzirBt.setOnClickListener {
            if (originalEt.text.isNotEmpty()) { // Testa se o usuário digitou alguma coisa para traduzir
                val tradutor: Tradutor =
                    Tradutor(this) // Instancia um tradutor para fazer a chamada ao WS
                tradutor.traduzir(
                    originalEt.text.toString(), // Solicita a tradução com base nos parâmetros selecionados pelo usuário
                    idiomaOrigemSp.selectedItem.toString(),
                    idiomaDestinoSp.selectedItem.toString()
                )
            } else {
                mainLl.snackbar("É preciso digitar uma palavra para ser traduzida") // Senão, mostra uma mensagem na parte debaixo do LinearLayout
            }
        }
    }

    val idiomas = listOf("pt", "en") // Idiomas de origem e destino. Dependem da API do Oxford Dict.
    lateinit var tradutoHandler: TradutoHandler // Handler da thread de UI

    inner class TradutoHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == RESPOSTA_TRADUCAO) {
                traduzidoTv.text = msg.obj.toString() // Alterar o conteúdo do TextView
            }
        }
    }
}