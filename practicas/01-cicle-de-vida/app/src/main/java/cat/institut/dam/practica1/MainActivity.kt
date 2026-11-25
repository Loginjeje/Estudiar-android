package cat.institut.dam.practica1

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private var comptadorCreacions = 0
    }

    private val ETIQUETA = "CicleVida"

    private lateinit var textEstat: TextView
    private lateinit var textComptador: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textEstat = findViewById(R.id.textEstat)
        textComptador = findViewById(R.id.textComptador)

        comptadorCreacions++

        Log.d(ETIQUETA, "onCreate() - l'activitat s'ha creat")
        actualitzaEstat(R.string.estado_on_create)
        textComptador.text =
            getString(R.string.comptador_creacions, comptadorCreacions)
    }

    override fun onStart() {
        super.onStart()
        Log.d(ETIQUETA, "onStart() - l'activitat es fa visible")
        actualitzaEstat(R.string.estado_on_start)
    }

    override fun onResume() {
        super.onResume()
        Log.d(ETIQUETA, "onResume() - l'activitat passa a primer pla")
        actualitzaEstat(R.string.estado_on_resume)
    }

    override fun onPause() {
        super.onPause()
        Log.d(ETIQUETA, "onPause() - l'activitat perd el focus")
        actualitzaEstat(R.string.estado_on_pause)
    }

    override fun onStop() {
        super.onStop()
        Log.d(ETIQUETA, "onStop() - l'activitat s'atura")
        actualitzaEstat(R.string.estado_on_stop)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(ETIQUETA, "onDestroy() - l'activitat es destrueix")
        actualitzaEstat(R.string.estado_on_destroy)
    }

    private fun actualitzaEstat(@StringRes recurs: Int) {
        textEstat.text = getString(recurs)
    }
}