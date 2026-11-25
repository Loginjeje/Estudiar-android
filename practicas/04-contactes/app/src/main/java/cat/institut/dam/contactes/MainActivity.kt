package cat.institut.dam.contactes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * MainActivity — la llista de contactes.
 *
 * Aquesta classe és l'esquelet de la pràctica: la interfície ja està
 * connectada, però la part de les dades (carregar, filtrar i obrir el
 * detall) la has de fer tu. Busca els TODO.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /** Codi de la petició de permís de notificacions. */
        private const val REQUEST_NOTIFICACIONS = 100
    }

    private lateinit var etCerca: EditText
    private lateinit var btnActualitza: Button
    private lateinit var pbCarga: ProgressBar
    private lateinit var txtBuit: TextView
    private lateinit var txtAvis: TextView
    private lateinit var rvContactes: RecyclerView
    private lateinit var adapter: ContacteAdapter

    /** Cua de missatges del fil principal: hi tornem des del fil de segon pla. */
    private val filPrincipal = Handler(Looper.getMainLooper())

    /** Contactes de l'última càrrega, per a la notificació. */
    private var quantitatCarregada = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Finestres.aplica(this, findViewById<View>(R.id.arrel))

        etCerca = findViewById(R.id.etCerca)
        btnActualitza = findViewById(R.id.btnActualitza)
        pbCarga = findViewById(R.id.pbCarga)
        txtBuit = findViewById(R.id.txtBuit)
        txtAvis = findViewById(R.id.txtAvis)
        rvContactes = findViewById(R.id.rvContactes)

        // TODO 1: adapter i RecyclerView
        adapter = ContacteAdapter(
            onContacteClick = { contacte ->
                // Només passem l'id: el detall busca la resta al repositori
                val intent = Intent(this, ContacteDetallActivity::class.java)
                intent.putExtra(ContacteDetallActivity.EXTRA_ID, contacte.id)
                startActivity(intent)
            },
            onFavoritClick = { contacte -> ContactesRepository.alternaFavorit(contacte.id) }
        )
        rvContactes.layoutManager = LinearLayoutManager(this)
        rvContactes.adapter = adapter

        // TODO 2: filtra a cada tecla
        etCerca.doAfterTextChanged { mostraLlista() }

        // TODO 3: recarrega amb el botó
        btnActualitza.setOnClickListener { carrega() }

        // TODO 4: el permís es demana a carrega(), quan les dades ja hi són
        carrega()
    }

    /**
     * TODO 5: carrega els contactes.
     *
     * La petició HTTP no es pot fer al fil principal: has de fer-la en un
     * fil de segon pla i tornar al fil principal amb un Handler.
     *
     * Si no hi ha xarxa, carrega els contactes del fitxer local
     * `assets/contactes.json` i avisa l'usuari amb `txtAvis`.
     *
     * Quan la llista s'hagi carregat correctament, crida
     * `demanaPermisNotificacions()` abans de mostrar la notificació.
     */
    private fun carrega() {
        pbCarga.visibility = View.VISIBLE
        btnActualitza.isEnabled = false
        txtAvis.visibility = View.GONE

        Thread {
            var local = false
            val contactes = try {
                ContactesService.desDeXarxa()
            } catch (e: Exception) {
                local = true
                try {
                    ContactesService.desDeRecurs(applicationContext)
                } catch (e2: Exception) {
                    null
                }
            }

            filPrincipal.post {
                if (isDestroyed) return@post
                pbCarga.visibility = View.GONE
                btnActualitza.isEnabled = true
                if (contactes == null) {
                    Toast.makeText(this, R.string.error_carrega, Toast.LENGTH_LONG).show()
                    return@post
                }
                if (local) {
                    txtAvis.setText(R.string.avis_dades_locals)
                    txtAvis.visibility = View.VISIBLE
                }
                ContactesRepository.actualitza(contactes)
                mostraLlista()

                quantitatCarregada = contactes.size
                if (tePermisNotificacions()) {
                    Notificacions.contactesCarregats(this, quantitatCarregada)
                } else {
                    demanaPermisNotificacions()
                }
            }
        }.start()
    }

    /** En tornar de la fitxa, el favorit pot haver canviat. */
    override fun onResume() {
        super.onResume()
        mostraLlista()
    }

    /** Aplica la cerca actual i mostra el text de llista buida si cal. */
    private fun mostraLlista() {
        val texte = etCerca.text?.toString().orEmpty()
        val filtrats = ContactesRepository.cerca(texte)
        adapter.actualitza(filtrats)
        if (filtrats.isEmpty()) {
            txtBuit.setText(if (texte.isBlank()) R.string.txt_buit else R.string.txt_buit_cerca)
            txtBuit.visibility = View.VISIBLE
        } else {
            txtBuit.visibility = View.GONE
        }
    }

    private fun tePermisNotificacions(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * A partir d'Android 13 (API 33) cal demanar permís en temps d'execució
     * per poder mostrar notificacions. En versions anteriors el permís no
     * existeix i no cal demanar-lo.
     */
    private fun demanaPermisNotificacions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permissio = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permissio) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permissio), REQUEST_NOTIFICACIONS)
    }

    /**
     * Resposta de l'usuari a la petició de permís: si el denega, avisa'l amb
     * un Toast.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICACIONS) {
            val concedit = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (concedit) {
                Notificacions.contactesCarregats(this, quantitatCarregada)
            } else {
                // TODO 6: un permís denegat no és un error: l'app continua igual
                Toast.makeText(this, R.string.permis_denegada, Toast.LENGTH_LONG).show()
            }
        }
    }
}
