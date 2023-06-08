package it.prova.appmeteo

import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import it.prova.appmeteo.databinding.ActivityMainBinding
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.but.setOnClickListener {
            val c = binding.CapInput.text.toString()
            codice(c).start()
        }

    }
    private fun codice(nome: String): Thread {
        return Thread {
            val url = URL("https://api.meteo.uniparthenope.it/places/search/byname/$nome")
            val connection = url.openConnection() as HttpsURLConnection
            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, comuni::class.java)
                scelta(request)
            }
            else {
                binding.meteoo.text  = "Connessione non riuscita"
            }
        }
    }


    private fun Tempo1(comune: String, nome_nor: String): Thread {
        return Thread {
            val url = URL("https://api.meteo.uniparthenope.it//products/wrf5/forecast/$comune")
            val connection = url.openConnection() as HttpsURLConnection
            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, Request::class.java)
                if (request.result == "ok") {
                    var stringa = request.forecast.iDate
                    stringa = stringa.removeRange(8, 11)
                    var data = stringa + "Z1400"
                    val string = stringa.removeRange(0, 6) + "/" + stringa.substring(
                        4,
                        6
                    ) + "/" + stringa.removeRange(4, 8)
                    var nome =  nome_nor.substring(0, 1) + nome_nor.substring(1, nome_nor.length).lowercase()
                    val stringameteo = when(request.forecast.text.it) {
                        "Nuvoloso" -> "A ${nome} è previsto un cielo:"
                        "Rovesci" -> "A ${nome} sono previsti:"
                        "Pioggia" -> "A ${nome} è prevista:"
                        "Soleggiato" -> "A ${nome} è previsto un cielo:"
                        "Sereno" -> "Il cielo previsto a ${nome} è:"
                        "Molto nuvoloso" -> "Il cielo previsto a ${nome} è:"
                        "Coperto" -> "il cielo di ${nome} è:"
                        else -> {
                            "Meteo previsto a ${nome}:"
                        }
                    }
                    updateUI(request, string, stringameteo, data)
                } else{
                    update1()
                }
                inputStreamReader.close()
                inputSystem.close()
            } else {
                binding.meteoo.text = "Connessione non riuscita"
            }
        }
    }

    private fun scelta(request: comuni) {
        runOnUiThread {
            kotlin.run {
                val menu = findViewById<Spinner>(R.id.scelta)
                var scelte : MutableList<String> = ArrayList()
                for(i in 0 until request.size){
                    scelte.add(i, request[i].name.it)
                }
                val dropmenu = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line,scelte)
                menu.adapter = dropmenu
                menu.onItemSelectedListener = object : OnItemSelectedListener{
                    override fun onItemSelected(adapter: AdapterView<*>?, view: View?, posizione: Int, id: Long) {
                            Tempo1(request[posizione].id, request[posizione].name.it).start()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
            }
        }

    }

    private fun update1() {
        runOnUiThread {
            kotlin.run{
                binding.ok.visibility = View.INVISIBLE
                binding.meteoo.text = "Cap non riconosciuto"
                val immagine = findViewById<View>(R.id.immagine) as ImageView
                immagine.visibility = View.GONE
            }
        }
    }


    private fun updateUI(request: Request, stringa: String, stringameteo: String, data: String) {
        runOnUiThread {
            kotlin.run {
                binding.ok.visibility = View.VISIBLE
                binding.ok.text = stringameteo
                binding.lastupdate.text = "ultimo aggiornamento: $stringa "
                binding.meteoo.text =  request.forecast.text.it
                immagine(data)
                val immagine = findViewById<View>(R.id.immagine) as ImageView
                immagine.visibility = View.VISIBLE


            }
        }
    }

    private fun immagine(data: String) {
        val url = "https://api.meteo.uniparthenope.it/products/wrf5/forecast/it000/plot/image?output=gen&opt=bars&date=${data}"
        val immagine = findViewById<View>(R.id.immagine) as ImageView
        Picasso.with(binding.root.context).load(url).into(immagine)
    }

}


class Data(
   var anno: String = "z"
)