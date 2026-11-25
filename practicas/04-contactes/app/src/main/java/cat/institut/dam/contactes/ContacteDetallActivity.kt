package cat.institut.dam.contactes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ContacteDetallActivity — fitxa d'un contacte.
 *
 * Rep només l'id a l'Intent (EXTRA_ID) i busca el contacte complet a
 * ContactesRepository. Els sis botons obren altres apps amb Intents implícits.
 */
class ContacteDetallActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "cat.institut.dam.contactes.EXTRA_ID"
        private const val REQUEST_FOTO = 200
    }

    private val filPrincipal = Handler(Looper.getMainLooper())
    private lateinit var contacte: Contacte
    private lateinit var ivFoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall)
        Finestres.aplica(this, findViewById(R.id.arrel))

        val id = intent.getIntExtra(EXTRA_ID, -1)
        val trobat = ContactesRepository.contacte(id)
        if (trobat == null) {
            Toast.makeText(this, R.string.error_contacte, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        contacte = trobat

        ivFoto = findViewById(R.id.ivFoto)
        findViewById<TextView>(R.id.tvNom).text = contacte.nom
        findViewById<TextView>(R.id.tvEmail).text = contacte.email
        findViewById<TextView>(R.id.tvTelefon).text = contacte.telefon
        findViewById<TextView>(R.id.tvAdreca).text = contacte.adreçaCompleta
        findViewById<TextView>(R.id.tvWeb).text = contacte.web

        val chkFavorit = findViewById<CheckBox>(R.id.chkFavorit)
        chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
        chkFavorit.setOnCheckedChangeListener { _, _ ->
            ContactesRepository.alternaFavorit(contacte.id)
        }

        mostraFoto()

        findViewById<Button>(R.id.btnTrucar).setOnClickListener {
            obre(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacte.telefon}")))
        }
        findViewById<Button>(R.id.btnCorreu).setOnClickListener {
            obre(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contacte.email}")))
        }
        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            val lat = contacte.latitud
            val lng = contacte.longitud
            val etiqueta = Uri.encode(contacte.nom)
            obre(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($etiqueta)")))
        }
        findViewById<Button>(R.id.btnWeb).setOnClickListener {
            // El servidor torna "hildegard.org": sense esquema el navegador no l'obre
            var web = contacte.web
            if (!web.startsWith("http")) web = "https://$web"
            obre(Intent(Intent.ACTION_VIEW, Uri.parse(web)))
        }
        findViewById<Button>(R.id.btnAlarma).setOnClickListener {
            obre(Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, 9)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarma_missatge, contacte.nom))
            })
        }
        findViewById<Button>(R.id.btnGaleria).setOnClickListener { triaFoto() }
    }

    /** Foto triada per l'usuari si n'hi ha; si no, l'avatar de la xarxa. */
    private fun mostraFoto() {
        val uri = ContactesRepository.foto(contacte.id)
        if (uri != null) {
            ivFoto.setImageURI(uri)
            return
        }
        ivFoto.setImageResource(R.drawable.ic_contacte)
        Thread {
            val bitmap = ContactesService.baixaImatge(contacte.urlAvatar)
            filPrincipal.post {
                // Si l'usuari ha triat una foto mentrestant, no la trepitgem
                if (bitmap != null && !isDestroyed && ContactesRepository.foto(contacte.id) == null) {
                    ivFoto.setImageBitmap(bitmap)
                }
            }
        }.start()
    }

    /** Intent implícit: si no hi ha cap app que l'accepti, avisa en lloc de petar. */
    private fun obre(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_app_no_disponible, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun triaFoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        try {
            startActivityForResult(intent, REQUEST_FOTO)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_app_no_disponible, Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_FOTO || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // alguns selectors no concedeixen un permís persistent
        }
        ContactesRepository.desaFoto(contacte.id, uri)
        ivFoto.setImageURI(uri)
    }
}
