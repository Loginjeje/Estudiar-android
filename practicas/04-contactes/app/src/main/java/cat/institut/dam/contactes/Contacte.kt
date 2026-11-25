package cat.institut.dam.contactes

import org.json.JSONObject

/**
 * Contacte — model de dades d'un contacte de l'agenda.
 *
 * És una data class: el compilador genera equals, hashCode, toString i copy().
 */
data class Contacte(
    val id: Int,
    val nom: String,
    val username: String,
    val email: String,
    val telefon: String,
    val web: String,
    val carrer: String,
    val ciutat: String,
    val codiPostal: String,
    val latitud: Double,
    val longitud: Double
) {

    /** Inicials de fins a dues paraules del nom (per al cercle de la fila). */
    val inicials: String
        get() = nom.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    /** Carrer, ciutat i codi postal en una sola línia. */
    val adreçaCompleta: String
        get() = listOf(carrer, ciutat, codiPostal)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    /** Adreça de la imatge del contacte. */
    val urlAvatar: String
        get() = "https://i.pravatar.cc/300?u=$username"

    companion object {
        /** Converteix un objecte JSON (format jsonplaceholder) en un Contacte. */
        fun desDeJson(json: JSONObject): Contacte {
            val adreça = json.getJSONObject("address")
            val geo = adreça.getJSONObject("geo")
            return Contacte(
                id = json.getInt("id"),
                nom = json.getString("name"),
                username = json.optString("username"),
                email = json.optString("email"),
                telefon = json.optString("phone"),
                web = json.optString("website"),
                carrer = adreça.optString("street"),
                ciutat = adreça.optString("city"),
                codiPostal = adreça.optString("zipcode"),
                // lat i lng arriben com a text: "41.3874"
                latitud = geo.optString("lat").toDoubleOrNull() ?: 0.0,
                longitud = geo.optString("lng").toDoubleOrNull() ?: 0.0
            )
        }
    }
}
