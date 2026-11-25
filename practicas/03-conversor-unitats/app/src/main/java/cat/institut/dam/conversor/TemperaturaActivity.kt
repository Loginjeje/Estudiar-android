package cat.institut.dam.conversor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class TemperaturaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura)

        val editValor = findViewById<EditText>(R.id.editValor)
        val radioGroupDe = findViewById<RadioGroup>(R.id.radioGroupDe)
        val radioGroupA = findViewById<RadioGroup>(R.id.radioGroupA)
        val textResultat = findViewById<TextView>(R.id.textResultat)

        findViewById<Button>(R.id.botoConvertir).setOnClickListener {
            val text = editValor.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_buid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val valor = text.toDoubleOrNull()
            if (valor == null) {
                Toast.makeText(this, R.string.error_numeric, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val de = radioGroupDe.checkedRadioButtonId
            val a = radioGroupA.checkedRadioButtonId
            val mateixaUnitat = (de == R.id.radioCelsius && a == R.id.radioCelsiusA) ||
                (de == R.id.radioFahrenheit && a == R.id.radioFahrenheitA) ||
                (de == R.id.radioKelvin && a == R.id.radioKelvinA)
            if (mateixaUnitat) {
                Toast.makeText(this, R.string.error_unitats, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val celsius = when (de) {
                    R.id.radioCelsius -> valor
                    R.id.radioFahrenheit -> (valor - 32.0) * 5.0 / 9.0
                    R.id.radioKelvin -> valor - 273.15
                    else -> throw IllegalArgumentException()
                }
                val resultat = when (a) {
                    R.id.radioCelsiusA -> celsius
                    R.id.radioFahrenheitA -> celsius * 9.0 / 5.0 + 32.0
                    R.id.radioKelvinA -> celsius + 273.15
                    else -> throw IllegalArgumentException()
                }
                val unitat = when (a) {
                    R.id.radioCelsiusA -> "°C"
                    R.id.radioFahrenheitA -> "°F"
                    R.id.radioKelvinA -> "K"
                    else -> throw IllegalArgumentException()
                }
                textResultat.text = String.format(Locale.US, "%.2f %s", resultat, unitat)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
            }
        }
    }
}