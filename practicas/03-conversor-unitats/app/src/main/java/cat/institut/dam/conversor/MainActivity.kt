package cat.institut.dam.conversor

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity — Pantalla d'inici (menú) del Conversor.
 *
 * És l'Activity marcada amb MAIN / LAUNCHER a l'AndroidManifest.xml:
 * en arrencar l'aplicació, Android obre aquesta pantalla.
 *
 * ---------------------------------------------------------------------------
 * TASCA (Pràctica 1B)
 * ---------------------------------------------------------------------------
 * 1. Afegeix al layout `activity_main.xml` els tres botons del menú
 *    (temperatura, longitud i massa) amb els identificadors indicats
 *    a l'enunciat.
 * 2. associate cada botó amb el listener que obre la conversora
 *    corresponent mitjançant un INTENT EXPLÍCIT.
 *
 * Pista: un Intent explícit es crea amb
 *      Intent(this, ActivityDesti::class.java)
 * i s'engega amb
 *      startActivity(intent)
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.botoTemperatura).setOnClickListener {
            startActivity(Intent(this, TemperaturaActivity::class.java))
        }
        findViewById<Button>(R.id.botoLongitud).setOnClickListener {
            startActivity(Intent(this, LongitudActivity::class.java))
        }
        findViewById<Button>(R.id.botoMassa).setOnClickListener {
            startActivity(Intent(this, MassaActivity::class.java))
        }
    }
}
