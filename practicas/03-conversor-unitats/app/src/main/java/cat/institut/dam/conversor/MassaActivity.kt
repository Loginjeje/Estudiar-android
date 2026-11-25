package cat.institut.dam.conversor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MassaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_massa)

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
            val mateixaUnitat = (de == R.id.radioQuilogram && a == R.id.radioQuilogramA) ||
                (de == R.id.radioLliura && a == R.id.radioLliuraA) ||
                (de == R.id.radioUnca && a == R.id.radioUncaA)
            if (mateixaUnitat) {
                Toast.makeText(this, R.string.error_unitats, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val quilograms = when (de) {
                    R.id.radioQuilogram -> valor
                    R.id.radioLliura -> valor * 0.453592
                    R.id.radioUnca -> valor * 0.0283495
                    else -> throw IllegalArgumentException()
                }
                val resultat = when (a) {
                    R.id.radioQuilogramA -> quilograms
                    R.id.radioLliuraA -> quilograms * 2.20462
                    R.id.radioUncaA -> quilograms * 35.27396
                    else -> throw IllegalArgumentException()
                }
                val unitat = when (a) {
                    R.id.radioQuilogramA -> "kg"
                    R.id.radioLliuraA -> "lb"
                    R.id.radioUncaA -> "oz"
                    else -> throw IllegalArgumentException()
                }
                textResultat.text = String.format(Locale.US, "%.4f %s", resultat, unitat)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
            }
        }
    }
}