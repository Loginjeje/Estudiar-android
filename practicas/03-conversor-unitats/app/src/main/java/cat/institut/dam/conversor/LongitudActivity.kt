package cat.institut.dam.conversor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LongitudActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_longitud)

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
            val mateixaUnitat = (de == R.id.radioQuilometre && a == R.id.radioQuilometreA) ||
                (de == R.id.radioMilla && a == R.id.radioMillaA) ||
                (de == R.id.radioYarda && a == R.id.radioYardaA)
            if (mateixaUnitat) {
                Toast.makeText(this, R.string.error_unitats, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val quilometres = when (de) {
                    R.id.radioQuilometre -> valor
                    R.id.radioMilla -> valor * 1.60934
                    R.id.radioYarda -> valor * 0.0009144
                    else -> throw IllegalArgumentException()
                }
                val resultat = when (a) {
                    R.id.radioQuilometreA -> quilometres
                    R.id.radioMillaA -> quilometres * 0.621371
                    R.id.radioYardaA -> quilometres * 1093.6133
                    else -> throw IllegalArgumentException()
                }
                val unitat = when (a) {
                    R.id.radioQuilometreA -> "km"
                    R.id.radioMillaA -> "mi"
                    R.id.radioYardaA -> "yd"
                    else -> throw IllegalArgumentException()
                }
                textResultat.text = String.format(Locale.US, "%.4f %s", resultat, unitat)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
            }
        }
    }
}