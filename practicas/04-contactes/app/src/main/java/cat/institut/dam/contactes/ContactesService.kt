package cat.institut.dam.contactes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * ContactesService — d'on surten les dades: xarxa, còpia local i imatges.
 *
 * Totes les funcions que fan xarxa són bloquejants: s'han de cridar des
 * d'un fil de segon pla, mai des del fil principal.
 */
object ContactesService {

    const val URL_CONTACTES = "https://jsonplaceholder.typicode.com/users"

    private const val TEMPS_ESPERA = 8000

    /** Fa la petició HTTP. Llença una excepció si no hi ha xarxa o el servidor falla. */
    fun desDeXarxa(): List<Contacte> {
        val connexio = URL(URL_CONTACTES).openConnection() as HttpURLConnection
        try {
            connexio.requestMethod = "GET"
            connexio.connectTimeout = TEMPS_ESPERA
            connexio.readTimeout = TEMPS_ESPERA
            if (connexio.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("El servidor ha respost amb ${connexio.responseCode}")
            }
            val text = connexio.inputStream.bufferedReader().use { it.readText() }
            return parseja(text)
        } finally {
            connexio.disconnect()
        }
    }

    /** Llegeix la còpia local de app/src/main/assets/contactes.json. */
    fun desDeRecurs(context: Context): List<Contacte> {
        val text = context.assets.open("contactes.json").bufferedReader().use { it.readText() }
        return parseja(text)
    }

    /** Converteix el text JSON (un array d'objectes) en una llista de contactes. */
    fun parseja(text: String): List<Contacte> {
        val array = JSONArray(text)
        return (0 until array.length()).map { i -> Contacte.desDeJson(array.getJSONObject(i)) }
    }

    /** Baixa una imatge. Torna null si falla (sense xarxa, URL dolenta...). */
    fun baixaImatge(url: String): Bitmap? {
        return try {
            val connexio = URL(url).openConnection() as HttpURLConnection
            try {
                connexio.connectTimeout = TEMPS_ESPERA
                connexio.readTimeout = TEMPS_ESPERA
                connexio.inputStream.use { BitmapFactory.decodeStream(it) }
            } finally {
                connexio.disconnect()
            }
        } catch (e: Exception) {
            null
        }
    }
}
