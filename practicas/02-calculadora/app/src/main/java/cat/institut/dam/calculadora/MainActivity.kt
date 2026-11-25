package cat.institut.dam.calculadora

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fathzer.soft.javaluator.DoubleEvaluator
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ETIQUETA = "CalculadoraB"
    }

    // Referències als elements de la UI
    private lateinit var textExpressio: TextView
    private lateinit var textResultat: TextView

    // L'expressió completa que l'usuari va construint
    private val expressio = StringBuilder()

    // Avaluador d'expressions (JAvaluator)
    private val evaluator = DoubleEvaluator()

    // Format per mostrar resultats
    private val formatNumber = DecimalFormat("#.##########")

    // -----------------------------------------------------------------------
    // Cicle de vida
    // -----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textExpressio = findViewById(R.id.textExpressio)
        textResultat = findViewById(R.id.textResultat)

        configurarBotonsNumerics()
        configurarBotonsOperacions()
        configurarBotonsEspecials()
    }

    // -----------------------------------------------------------------------
    // Configuració dels botons
    // -----------------------------------------------------------------------

    private fun configurarBotonsNumerics() {
        val botonsNumerics = mapOf(
            R.id.boto0 to "0",
             R.id.boto1 to "1",
            R.id.boto2 to "2",
             R.id.boto3 to "3",
             R.id.boto4 to "4",
            R.id.boto5 to "5",
             R.id.boto6 to "6",
            R.id.boto7 to "7",
            R.id.boto8 to "8",
            R.id.boto9 to "9"
        )

        for ((id, digit) in botonsNumerics) {
            findViewById<Button>(id).setOnClickListener {
                afegirCaracter(digit)
            }
        }
    }

    private fun configurarBotonsOperacions() {
        val operadors = mapOf(
            R.id.botoSumar to "+",
            R.id.botoRestar to "-",
            R.id.botomultiplicar to "×",
            R.id.botodividir to "÷"

        )

        for ((id, op) in operadors) {
            findViewById<Button>(id).setOnClickListener {
                afegirCaracter(op)
            }
        }
    }

    private fun configurarBotonsEspecials() {
        findViewById<Button>(R.id.botoParentesiObert).setOnClickListener {
            afegirCaracter("(")
        }

         findViewById<Button>(R.id.botoParentesitancat).setOnClickListener {
            afegirCaracter(")")
        }

        findViewById<Button>(R.id.botoC).setOnClickListener {
            netejarTot()
        }

        findViewById<Button>(R.id.botoborrar).setOnClickListener {
            esborrarUltim()
        }

        findViewById<Button>(R.id.botoComa).setOnClickListener {
            afegirCaracter(".")
        }

        findViewById<Button>(R.id.botopunt).setOnClickListener {
            afegirCaracter(".")
        }

        findViewById<Button>(R.id.botoigual).setOnClickListener {
            calcularResultat()
        }


    }

    // -----------------------------------------------------------------------
    // Lògica de la calculadora
    // -----------------------------------------------------------------------

    /**
     * Afegeix un caràcter a l'expressió i actualitza el display.
     */
    private fun afegirCaracter(caracter: String) {
        expressio.append(caracter)
        textExpressio.text = expressio.toString()
    }

    /**
     * Esborra l'últim caràcter de l'expressió.
     */
    private fun esborrarUltim() {
        if (expressio.isNotEmpty()) {
            expressio.deleteCharAt(expressio.length - 1)
            textExpressio.text = expressio.toString()
        }
    }

    /**
     * Neteja tot: expressió i resultat.
     */
    private fun netejarTot() {
        expressio.clear()
        textExpressio.text = ""
        textResultat.text = getString(R.string.resultat_inicial)
    }

    /**
     * Avalua l'expressió i mostra el resultat.
     *
     * Usa JAvaluator per avaluar l'expressió. Si és incorrecta
     * (parèntesis desbalancejats, operadors consecutius, etc.),
     * mostra un missatge d'error.
     */
    private fun calcularResultat() {
        if (expressio.isEmpty()) return

        try {
            // Substituir símbols UX per operadors estàndard
            val expressioNeta = expressio.toString()
                .replace("−", "-")
                .replace("×", "*")
                .replace("÷", "/")

            val resultat = evaluator.evaluate(expressioNeta)
            textResultat.text = formatNumber.format(resultat)
        } catch (e: Exception) {
            textResultat.text = getString(R.string.error_expressio)
        }
    }
}
