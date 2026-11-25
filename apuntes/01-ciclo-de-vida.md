# 01 · Práctica 1 — Ciclo de vida de una Activity

📁 Código: `practicas/01-cicle-de-vida/`
📄 Enunciado original: `~/Baixades/enunciat-revisat.html`

## ¿Qué hace la app?

Una app "de laboratorio" (`CicleVidaApp`) que no sirve para nada útil: sirve para **ver** cuándo Android llama a cada método del ciclo de vida.

- Escribe en el **Logcat** cada método que se ejecuta.
- Muestra en pantalla el **último estado**.
- Cuenta **cuántas veces se ha creado** la pantalla (y el número no se pierde al girar el móvil).

## Qué venía hecho y qué hice yo

| Ya venía hecho | Lo hice yo |
|---|---|
| Proyecto, Gradle, manifest | **T1** · `Log.d()` en los 6 métodos |
| Los 6 métodos `onCreate`, `onStart`... vacíos con `// TODO` | **T2** · `findViewById` + mostrar el estado en pantalla |
| Un `TextView` (`textEstat`) en el layout | **T3** · Contador de creaciones en el `companion object` |
| | **T4** · Segundo `TextView` (`textComptador`) en el layout |
| | **T5** · Todos los textos en `strings.xml` |

---

## Teoría mínima: el ciclo de vida

Una **Activity** es una pantalla. Android la va cambiando de estado y, en cada cambio, llama a un método:

```
 onCreate()  → se crea (aquí montas la pantalla)
 onStart()   → se hace visible
 onResume()  → está delante y se puede tocar
      ... la app funcionando ...
 onPause()   → pierde el foco
 onStop()    → ya no se ve
 onDestroy() → se destruye
```

Van **por parejas**: `onCreate/onDestroy`, `onStart/onStop`, `onResume/onPause`.

---

## Lo que aprendí a hacer (código explicado)

### T1 · Escribir en el Logcat

```kotlin
private val ETIQUETA = "CicleVida"     // se declara UNA vez y se reutiliza

override fun onPause() {
    super.onPause()                    // ⚠️ obligatorio en todos
    Log.d(ETIQUETA, "onPause() - l'activitat perd el focus")
    actualitzaEstat(R.string.estado_on_pause)
}
```

- `Log.d(etiqueta, mensaje)` escribe un mensaje de depuración en el Logcat.
- En Android Studio filtra con `tag:CicleVida` para ver solo tus mensajes.
- La etiqueta va en **una sola variable**: si mañana cambia, la cambias en un sitio (DRY = *Don't Repeat Yourself*).
- **No olvides `super.onXxx()`**. Si falta, la app se cierra con un error.

### T2 · Conectar el XML con Kotlin

```kotlin
private lateinit var textEstat: TextView        // "la inicializo más tarde"

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)       // 1º cargar el XML
    textEstat = findViewById(R.id.textEstat)     // 2º buscar la vista por su id
    ...
}
```

- **Orden importante:** primero `setContentView`, luego `findViewById`. Al revés devuelve `null` y la app peta.
- `lateinit var` significa que la variable aún no tiene valor y que se lo darás antes de usarla. Así te ahorras usar `TextView?` y null.
- `R.id.textEstat` sale del `android:id="@+id/textEstat"` del XML.

Como los 6 métodos hacen lo mismo, se saca a una **función auxiliar**:

```kotlin
private fun actualitzaEstat(@StringRes recurs: Int) {
    textEstat.text = getString(recurs)
}
```

`@StringRes` avisa si le pasas un número que no es un `R.string.algo`.

### T3 · Contador que sobrevive al giro (lo más importante)

```kotlin
companion object {
    private var comptadorCreacions = 0
}

// dentro de onCreate:
comptadorCreacions++
```

**¿Por qué en el `companion object`?**

Al **girar la pantalla**, Android **destruye** la Activity y crea **una nueva**. Lo que guarda la Activity se pierde con ella:

| Dónde guardas el valor | ¿Sobrevive al girar? |
|---|---|
| `private var x = 0` (propiedad normal) | ❌ No, muere con la Activity |
| `companion object { var x = 0 }` | ✅ Sí, pertenece a la **clase**, no al objeto |
| Cerrar la app del todo | ❌ Ninguno de los dos sobrevive (haría falta guardarlo en disco) |

El `companion object` es como un `static` de Java.

### T4 + T5 · Layout y textos en `strings.xml`

Nuevo `TextView` en `res/layout/activity_main.xml`:

```xml
<TextView
    android:id="@+id/textComptador"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    android:textSize="18sp" />
```

Texto con un número dentro, en `strings.xml`:

```xml
<string name="comptador_creacions">Creaciones: %1$d</string>
```

```kotlin
textComptador.text = getString(R.string.comptador_creacions, comptadorCreacions)
```

- `%1$d` es un hueco para el **1er** argumento, que es un número entero (`%d` = número, `%s` = texto). `getString` lo rellena.
- **Regla:** ningún texto visible escrito en el código ni en `android:text="..."`. Todo va a `strings.xml`.
- `sp` para tamaños de **texto** (respeta el tamaño de letra del usuario). `dp` para todo lo demás.

---

## Secuencias que había que comprobar

| Acción | Lo que sale en el Logcat |
|---|---|
| Abrir la app | onCreate → onStart → onResume |
| Botón Home | onPause → onStop |
| Volver a la app | onStart → onResume *(no onCreate)* |
| **Girar la pantalla** | onPause → onStop → onDestroy → **onCreate** → onStart → onResume |
| Botón Atrás (salir) | onPause → onStop → onDestroy |

## Preguntas de repaso

1. ¿Por qué el contador sube al girar el móvil? → Porque Android destruye y recrea la Activity, y `onCreate` se vuelve a ejecutar.
2. ¿Por qué no vuelve a 0? → Porque vive en el `companion object` (en la clase, no en la Activity).
3. ¿Qué pasa si llamas a `findViewById` antes que a `setContentView`? → Devuelve null y la app peta.
4. ¿Por qué guardar la etiqueta en una variable? → Para no repetirla 6 veces (DRY).
