# 04 · Práctica 1C — Contactes

📁 Código: `practicas/04-contactes/`
📄 Enunciado original: `~/Baixades/enunciat-prac1c.html`
📄 Resumen del profe: `~/Escriptori/Practica1C-Contactes-codi.pdf`

## ¿Qué hace la app?

- **Pantalla 1 (lista):** descarga contactos de internet y los muestra en una lista con **buscador** y **favoritos**. Si no hay internet, usa una copia local.
- **Pantalla 2 (ficha):** muestra un contacto con su foto y **6 botones** que abren otras apps: llamar, correo, mapa, web, alarma y galería.
- Al terminar de cargar, muestra una **notificación** (pidiendo permiso antes).

## Qué venía hecho y qué hice yo

| Ya venía hecho | Lo hice yo |
|---|---|
| Gradle, permisos en el manifest | `Contacte.kt` → modelo de datos + leer el JSON |
| Layout de la lista (`activity_main.xml`) | `ContactesRepository.kt` → datos compartidos (singleton) |
| `Finestres.kt` (barras del sistema) | `ContactesService.kt` → HTTP, copia local e imágenes |
| `assets/contactes.json` (copia local) | `item_contacte.xml` + `ContacteAdapter.kt` → la lista |
| strings, colores, temas, iconos | `MainActivity.kt` → los 6 TODO (cargar, buscar, permiso...) |
| | `activity_detall.xml` + `ContacteDetallActivity.kt` → la ficha |
| | `Notificacions.kt` + declarar el detalle en el manifest (TODO 7) |

Cómo se reparte el trabajo entre las piezas:

```
 Internet / assets ──► ContactesService ──► ContactesRepository ◄── ContacteDetallActivity
                        (descarga)          (guarda la lista)         (busca por id)
                                                   │
                                            MainActivity ──► ContacteAdapter ──► RecyclerView
```

---

## Lo que aprendí a hacer (código explicado)

### 1 · El modelo: `data class` + leer JSON (`Contacte.kt`)

```kotlin
data class Contacte(
    val id: Int,
    val nom: String,
    val email: String,
    val latitud: Double,
    ...
) {
    // propiedad calculada: no se guarda, se calcula cada vez que la pides
    val inicials: String
        get() = nom.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
```

- `data class` hace que Kotlin genere solo `equals`, `toString` y `copy()`. Es ideal para guardar datos.
- `get() = ...` calcula el valor al pedirlo. Por ejemplo, `"Leanne Graham"` da `"LG"`.

Convertir el JSON en un objeto:

```kotlin
companion object {
    fun desDeJson(json: JSONObject): Contacte {
        val adreça = json.getJSONObject("address")      // objeto dentro de objeto
        val geo = adreça.getJSONObject("geo")
        return Contacte(
            id = json.getInt("id"),                      // getXxx → si falta, PETA
            nom = json.getString("name"),
            email = json.optString("email"),             // optXxx → si falta, da "" (no peta)
            latitud = geo.optString("lat").toDoubleOrNull() ?: 0.0,
            ...
        )
    }
}
```

| Método | Si el campo no existe |
|---|---|
| `getString("x")` | Lanza una excepción (úsalo para campos obligatorios) |
| `optString("x")` | Devuelve `""` (úsalo para campos opcionales) |

`?: 0.0` es el **operador Elvis**: "si lo de la izquierda es `null`, usa `0.0`".

### 2 · Compartir datos entre pantallas: singleton (`ContactesRepository.kt`) ⭐

```kotlin
object ContactesRepository {                      // object = solo existe UNA instancia
    private val contactes = mutableListOf<Contacte>()
    private val favorits = mutableSetOf<Int>()    // Set: sin repetidos

    fun contacte(id: Int): Contacte? = contactes.firstOrNull { it.id == id }

    fun alternaFavorit(id: Int) {
        if (!favorits.remove(id)) favorits.add(id)    // si estaba lo quita; si no, lo pone
    }

    fun cerca(texte: String): List<Contacte> {
        val t = texte.trim()
        if (t.isEmpty()) return llista
        return contactes.filter {
            it.nom.contains(t, ignoreCase = true) ||
            it.ciutat.contains(t, ignoreCase = true) ||
            it.email.contains(t, ignoreCase = true)
        }
    }
}
```

- `object` crea un **singleton**: un único objeto compartido por toda la app. Las dos pantallas ven los mismos datos.
- Así, al abrir la ficha **solo se pasa el `id`** y la ficha busca el resto en el repositorio.
- `filter { }` devuelve los elementos que cumplen la condición.
- `contains(t, ignoreCase = true)` busca sin distinguir mayúsculas de minúsculas.

### 3 · Petición HTTP (`ContactesService.kt`)

```kotlin
fun desDeXarxa(): List<Contacte> {
    val connexio = URL(URL_CONTACTES).openConnection() as HttpURLConnection
    try {
        connexio.requestMethod = "GET"
        connexio.connectTimeout = 8000            // espera como mucho 8 s
        if (connexio.responseCode != HttpURLConnection.HTTP_OK) {
            throw IllegalStateException("Error ${connexio.responseCode}")
        }
        val text = connexio.inputStream.bufferedReader().use { it.readText() }
        return parseja(text)
    } finally {
        connexio.disconnect()                     // se ejecuta SIEMPRE, vaya bien o mal
    }
}

fun desDeRecurs(context: Context): List<Contacte> {       // copia local
    val text = context.assets.open("contactes.json").bufferedReader().use { it.readText() }
    return parseja(text)
}
```

- Hace falta `<uses-permission android:name="android.permission.INTERNET" />` en el manifest.
- `finally` se ejecuta siempre, con o sin error. Sirve para cerrar la conexión.
- `.use { }` cierra el lector automáticamente al terminar.
- `assets/` es una carpeta para archivos propios (aquí un JSON) que se leen con `context.assets.open(...)`.

### 4 · No bloquear la pantalla: `Thread` + `Handler` ⭐⭐

**Regla de Android:** la red **no** se puede usar desde el hilo principal (el de la interfaz). Si lo haces, peta con `NetworkOnMainThreadException`. Pero la interfaz **solo** se puede tocar desde el hilo principal.

La solución es ir al hilo secundario, descargar allí y volver al principal:

```kotlin
private val filPrincipal = Handler(Looper.getMainLooper())   // "buzón" del hilo principal

private fun carrega() {
    pbCarga.visibility = View.VISIBLE                 // muestro el "cargando..."

    Thread {                                          // ── HILO SECUNDARIO ──
        var local = false
        val contactes = try {
            ContactesService.desDeXarxa()             // intento internet
        } catch (e: Exception) {
            local = true
            try { ContactesService.desDeRecurs(applicationContext) }   // si falla, copia local
            catch (e2: Exception) { null }
        }

        filPrincipal.post {                           // ── VUELVO AL HILO PRINCIPAL ──
            if (isDestroyed) return@post              // la pantalla ya no existe
            pbCarga.visibility = View.GONE
            if (local) txtAvis.visibility = View.VISIBLE   // "datos sin conexión"
            ContactesRepository.actualitza(contactes)
            mostraLlista()
        }
    }.start()                                         // ¡no olvides start()!
}
```

- `Thread { ... }.start()` ejecuta ese código en segundo plano.
- `handler.post { ... }` ejecuta ese código en el hilo principal.
- `val x = try { a } catch { b }`: en Kotlin, `try` **devuelve un valor**.
- `VISIBLE` muestra la vista. `GONE` la oculta y además no ocupa espacio.

### 5 · Listas con `RecyclerView` + Adapter ⭐⭐

Un `RecyclerView` **recicla** las filas: solo crea las que caben en pantalla y, al hacer scroll, reutiliza las que salen por arriba para las que entran por abajo.

```kotlin
class ContacteAdapter(
    private val onContacteClick: (Contacte) -> Unit,     // función que recibe de la Activity
    private val onFavoritClick: (Contacte) -> Unit
) : RecyclerView.Adapter<ContacteAdapter.ContacteViewHolder>() {

    private var contactes: List<Contacte> = emptyList()

    fun actualitza(novs: List<Contacte>) {
        contactes = novs
        notifyDataSetChanged()                       // "los datos han cambiado, repinta"
    }

    // ViewHolder: guarda las vistas de UNA fila (para no hacer findViewById cada vez)
    class ContacteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNom: TextView = itemView.findViewById(R.id.tvNom)
        val chkFavorit: CheckBox = itemView.findViewById(R.id.chkFavorit)
    }

    // 1) CREAR una fila nueva a partir del XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContacteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacte, parent, false)
        return ContacteViewHolder(vista)
    }

    // 2) RELLENAR una fila con los datos de la posición X
    override fun onBindViewHolder(holder: ContacteViewHolder, position: Int) {
        val contacte = contactes[position]
        holder.tvNom.text = contacte.nom
        holder.itemView.setOnClickListener { onContacteClick(contacte) }
    }

    // 3) ¿CUÁNTAS filas hay?
    override fun getItemCount(): Int = contactes.size
}
```

Los 3 métodos obligatorios son **crear, rellenar y contar**.

En la Activity:

```kotlin
rvContactes.layoutManager = LinearLayoutManager(this)   // lista vertical
rvContactes.adapter = adapter
```

**⚠️ El problema del CheckBox reciclado:**

```kotlin
holder.chkFavorit.setOnCheckedChangeListener(null)                  // 1. quito el listener viejo
holder.chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)  // 2. marco sin disparar nada
holder.chkFavorit.setOnCheckedChangeListener { _, _ -> onFavoritClick(contacte) }  // 3. pongo el nuevo
```

La fila reciclada todavía tiene el listener del contacto **anterior**. Si cambias `isChecked` sin quitarlo antes, se marca como favorito el contacto equivocado.

### 6 · Buscar mientras escribes

```kotlin
etCerca.doAfterTextChanged { mostraLlista() }     // se llama con cada tecla

private fun mostraLlista() {
    val filtrats = ContactesRepository.cerca(etCerca.text.toString())
    adapter.actualitza(filtrats)
    txtBuit.visibility = if (filtrats.isEmpty()) View.VISIBLE else View.GONE
}
```

### 7 · Pasar datos a otra pantalla: `putExtra` / `getIntExtra`

```kotlin
// MainActivity: envío solo el id
val intent = Intent(this, ContacteDetallActivity::class.java)
intent.putExtra(ContacteDetallActivity.EXTRA_ID, contacte.id)
startActivity(intent)

// ContacteDetallActivity: lo recibo
val id = intent.getIntExtra(EXTRA_ID, -1)            // -1 si no llega nada
val trobat = ContactesRepository.contacte(id)
if (trobat == null) { finish(); return }             // finish() cierra esta pantalla
```

La clave (`EXTRA_ID`) se guarda en una constante para no equivocarte al escribirla dos veces.

### 8 · Intents implícitos: abrir otras apps ⭐⭐

**Explícito** = "abre ESTA Activity". **Implícito** = "quiero hacer ESTA acción, que lo haga la app que pueda".

```kotlin
// Llamar (abre el marcador, NO llama directamente → no necesita permiso)
obre(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacte.telefon}")))

// Correo
obre(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contacte.email}")))

// Mapa
obre(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($etiqueta)")))

// Web (si falta "https://", el navegador no la abre)
var web = contacte.web
if (!web.startsWith("http")) web = "https://$web"
obre(Intent(Intent.ACTION_VIEW, Uri.parse(web)))

// Alarma (con datos extra)
obre(Intent(AlarmClock.ACTION_SET_ALARM).apply {
    putExtra(AlarmClock.EXTRA_HOUR, 9)
    putExtra(AlarmClock.EXTRA_MINUTES, 0)
})
```

| Acción | URI | Abre |
|---|---|---|
| `ACTION_DIAL` | `tel:...` | Teléfono |
| `ACTION_SENDTO` | `mailto:...` | Correo |
| `ACTION_VIEW` | `geo:lat,lng` | Mapas |
| `ACTION_VIEW` | `https://...` | Navegador |
| `ACTION_SET_ALARM` | (extras) | Reloj |
| `ACTION_OPEN_DOCUMENT` | `type = "image/*"` | Galería / archivos |

**Si no hay ninguna app que lo pueda hacer**, la app peta, así que siempre hay que usar `try/catch`:

```kotlin
private fun obre(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.error_app_no_disponible, Toast.LENGTH_LONG).show()
    }
}
```

`.apply { ... }` sirve para configurar un objeto recién creado sin repetir su nombre.

### 9 · Elegir foto de la galería y recibir el resultado

```kotlin
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "image/*"                                   // solo imágenes
}
startActivityForResult(intent, REQUEST_FOTO)           // abre y ESPERA respuesta

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode != REQUEST_FOTO || resultCode != RESULT_OK) return
    val uri = data?.data ?: return                     // la imagen elegida
    ivFoto.setImageURI(uri)
}
```

- `REQUEST_FOTO` es un número para saber **qué** petición está respondiendo.
- `RESULT_OK` indica que el usuario eligió algo y no canceló.
- `data?.data ?: return`: si algo es `null`, sale de la función.

### 10 · Permiso en tiempo de ejecución (notificaciones) ⭐

Hay **2 tipos** de permiso:

| Tipo | Ejemplo | Qué hay que hacer |
|---|---|---|
| Normal | `INTERNET` | Solo declararlo en el manifest |
| Peligroso / en ejecución | `POST_NOTIFICATIONS` (Android 13+) | Declararlo **y preguntar** al usuario |

```kotlin
// ¿Ya lo tengo?
private fun tePermisNotificacions(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||          // antes de Android 13 no hace falta
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

// Pedirlo (sale el diálogo del sistema)
requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICACIONS)

// Respuesta del usuario
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_NOTIFICACIONS) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Notificacions.contactesCarregats(this, quantitatCarregada)
        } else {
            Toast.makeText(this, R.string.permis_denegada, Toast.LENGTH_LONG).show()
        }
    }
}
```

- El permiso se pide **después** de cargar los datos (lo pedía el enunciado): primero el usuario ve la app funcionando.
- Un permiso denegado **no es un error**: la app sigue funcionando, simplemente sin notificación.

### 11 · Notificación (`Notificacions.kt`)

```kotlin
// 1) Canal (obligatorio desde Android 8)
val canal = NotificationChannel(CANAL, nombre, NotificationManager.IMPORTANCE_DEFAULT)
context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)

// 2) Construir y mostrar
val notificacio = NotificationCompat.Builder(context, CANAL)
    .setSmallIcon(R.drawable.ic_notificacio)        // obligatorio
    .setContentTitle("...")
    .setContentText("...")
    .setAutoCancel(true)                            // desaparece al tocarla
    .build()
NotificationManagerCompat.from(context).notify(ID_NOTIFICACIO, notificacio)
```

Los pasos son **canal → builder → notify**. Si usas el mismo `ID`, la notificación nueva sustituye a la anterior.

### 12 · Manifest (TODO 7)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<activity
    android:name=".ContacteDetallActivity"
    android:exported="false" />
```

---

## Preguntas de repaso

1. ¿Por qué la descarga va en un `Thread`? → Porque la red está prohibida en el hilo principal (`NetworkOnMainThreadException`).
2. ¿Y por qué volver con `Handler.post`? → Porque la interfaz solo se puede tocar desde el hilo principal.
3. ¿Cuáles son los 3 métodos de un Adapter? → `onCreateViewHolder`, `onBindViewHolder` y `getItemCount`.
4. ¿Para qué sirve el ViewHolder? → Para guardar las vistas de una fila y no repetir `findViewById`.
5. ¿Por qué `setOnCheckedChangeListener(null)` antes de `isChecked`? → Porque la fila reciclada lleva el listener del contacto anterior.
6. ¿Qué diferencia hay entre un Intent explícito y uno implícito? → El explícito dice **qué Activity** abrir; el implícito dice **qué acción** hacer.
7. ¿Por qué se usa `try/catch` con los implícitos? → Por si no hay ninguna app que pueda hacerlo (`ActivityNotFoundException`).
8. ¿`INTERNET` hay que pedirlo al usuario? → No, es un permiso normal. `POST_NOTIFICATIONS` sí.
9. ¿Por qué se pasa solo el `id` en el Intent? → Porque la ficha busca el contacto completo en el repositorio (singleton).
10. ¿`getString` u `optString`? → `get` para campos obligatorios (peta si falta) y `opt` para opcionales (da `""`).
