package cat.institut.dam.contactes

import android.net.Uri

/**
 * ContactesRepository — dades compartides entre les dues pantalles (singleton).
 *
 * MainActivity hi desa la llista carregada i ContacteDetallActivity hi busca
 * el contacte a partir de l'id que li arriba a l'Intent.
 */
object ContactesRepository {

    private val contactes = mutableListOf<Contacte>()

    /** Un Set no admet duplicats: cada id hi és com a molt una vegada. */
    private val favorits = mutableSetOf<Int>()

    /** Fotos triades per l'usuari a la galeria (id del contacte → URI). */
    private val fotos = mutableMapOf<Int, Uri>()

    val llista: List<Contacte>
        get() = contactes

    fun actualitza(novs: List<Contacte>) {
        contactes.clear()
        contactes.addAll(novs)
    }

    fun contacte(id: Int): Contacte? = contactes.firstOrNull { it.id == id }

    fun esFavorit(id: Int): Boolean = id in favorits

    fun alternaFavorit(id: Int) {
        if (!favorits.remove(id)) favorits.add(id)
    }

    /** Filtra pel nom, la ciutat o el correu. Amb el text buit torna tota la llista. */
    fun cerca(texte: String): List<Contacte> {
        val t = texte.trim()
        if (t.isEmpty()) return llista
        return contactes.filter {
            it.nom.contains(t, ignoreCase = true) ||
                it.ciutat.contains(t, ignoreCase = true) ||
                it.email.contains(t, ignoreCase = true)
        }
    }

    fun desaFoto(id: Int, uri: Uri) {
        fotos[id] = uri
    }

    fun foto(id: Int): Uri? = fotos[id]
}
