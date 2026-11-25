# 02 · Ejemplo 2 — Calculadora

📁 Código: `practicas/02-calculadora/`
📄 Enunciado original: `~/Baixades/enunciat.html`

## ¿Qué hace la app?

Una calculadora con un **display** arriba (la operación y el resultado) y un **teclado de 20 botones** abajo. Escribes `(2+3)×4`, pulsas `=` y sale `20`.

## Qué venía hecho y qué hice yo

| Ya venía hecho | Lo hice yo |
|---|---|
| Gradle, manifest y la librería **JAvaluator** | `strings.xml` → los textos de todos los botones |
| Esqueleto de `MainActivity.kt` con la lógica **comentada** | `colors.xml` → los colores que faltaban |
| Estilos `Numero`, `Funcio`, `Operacio` | `themes.xml` → estilo nuevo para el botón `=` |
| 3 botones de ejemplo en el layout | Layout: `textResultat` + los **17 botones** que faltaban |
| `esborrarUltim()` ya hecho | Kotlin: completar 7 puntos (findViewById, `mapOf`, listeners...) |

Orden de trabajo recomendado: **recursos → layout → Kotlin**. Si empiezas por Kotlin te salen errores de `unresolved reference`, porque los ids y los strings todavía no existen.

---

## Lo que aprendí a hacer (código explicado)

### 1 · Recursos: `strings.xml` y `colors.xml`

```xml
<!-- res/values/strings.xml -->
<string name="boto_7">7</string>
<string name="boto_igual">=</string>
<string name="error_expressio">Error</string>

<!-- res/values/colors.xml -->
<color name="boto_igual">#90D5FF</color>
<color name="boto_operacio">#FFFF9F0A</color>
```

En el XML se usan con `@string/boto_7` y `@color/boto_igual`. Desde Kotlin, con `R.string.boto_7` y `R.color.boto_igual`.

Los colores van en formato `#AARRGGBB`: AA es la transparencia (FF = opaco).

### 2 · Estilos con herencia (`themes.xml`)

Un **estilo** agrupa atributos para no repetirlos en cada botón:

```xml
<!-- estilo base: tamaño, forma, márgenes -->
<style name="BotoCalculadora.Boto" parent="Widget.MaterialComponents.Button">
    <item name="android:layout_width">0dp</item>
    <item name="android:layout_height">64dp</item>
    <item name="cornerRadius">16dp</item>
</style>

<!-- estilo hijo: hereda todo lo anterior y solo cambia los colores -->
<style name="BotoCalculadora.Boto.igual" parent="BotoCalculadora.Boto">
    <item name="backgroundTint">@color/boto_igual</item>
    <item name="android:textColor">@color/boto_funcio_textAlter</item>
</style>
```

Y en el botón solo pones:

```xml
<Button
    android:id="@+id/botoigual"
    style="@style/BotoCalculadora.Boto.igual"
    android:layout_columnWeight="1"
    android:text="@string/boto_igual" />
```

`parent="..."` = "copia todo de ese estilo y yo cambio solo lo que escribo".

### 3 · La rejilla de botones: `GridLayout`

```xml
<GridLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:columnCount="4">
    <!-- los botones se colocan por orden: izquierda→derecha, arriba→abajo -->
</GridLayout>
```

- `columnCount="4"` → cada 4 botones empieza una fila nueva.
- **El orden en el XML = el orden en pantalla.**
- `layout_columnWeight="1"` junto con `layout_width="0dp"` hace que todas las columnas midan lo mismo.

### 4 · Textos del display

```xml
<TextView
    android:id="@+id/textResultat"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="end"
    android:maxLines="1"
    android:text="@string/resultat_inicial"
    android:textSize="32sp" />
```

| Atributo | Qué hace |
|---|---|
| `gravity="end"` | Texto pegado a la derecha (como en una calculadora) |
| `maxLines="1"` | Si es muy largo, se corta en vez de romper el diseño |
| `fontFamily="monospace"` | Todas las letras miden lo mismo de ancho |
| `hint` | Texto gris que desaparece al escribir (`text` se ve siempre) |

> ⚠️ En mi layout puse `android:textColor="#FFFFFFFF"` directamente. Lo correcto es usar `@color/...` y nunca escribir colores a mano en el layout.

### 5 · Conectar muchos botones con `mapOf` + `for` ⭐ (lo más importante)

En vez de escribir 10 `setOnClickListener` iguales:

```kotlin
val botonsNumerics = mapOf(
    R.id.boto0 to "0",
    R.id.boto1 to "1",
    // ...
    R.id.boto9 to "9"
)

for ((id, digit) in botonsNumerics) {
    findViewById<Button>(id).setOnClickListener {
        afegirCaracter(digit)
    }
}
```

- `mapOf(clave to valor)` crea un diccionario de **id del botón → texto que añade**.
- `for ((id, digit) in mapa)` recorre el mapa sacando la clave y el valor a la vez.
- `setOnClickListener { ... }` ejecuta el código de dentro cuando pulsas el botón.

Lo mismo para `+ − × ÷`. Los botones "especiales" (`C`, `⌫`, `=`, paréntesis) van uno a uno porque cada uno hace algo distinto.

### 6 · Patrón: cambiar los datos → actualizar la pantalla

```kotlin
private val expressio = StringBuilder()     // la operación que se va escribiendo

private fun afegirCaracter(caracter: String) {
    expressio.append(caracter)                    // 1. cambio los datos
    textExpressio.text = expressio.toString()     // 2. actualizo la pantalla
}

private fun netejarTot() {
    expressio.clear()
    textExpressio.text = ""
    textResultat.text = getString(R.string.resultat_inicial)   // desde recursos, no "0" a mano
}
```

Si te olvidas del paso 2, los datos cambian pero **la pantalla no**.

`StringBuilder` es un texto que se puede modificar con `append`, `deleteCharAt` y `clear`.

### 7 · Calcular sin que pete: `try / catch`

```kotlin
private fun calcularResultat() {
    if (expressio.isEmpty()) return

    try {
        val expressioNeta = expressio.toString()
            .replace("−", "-")      // el usuario ve símbolos bonitos...
            .replace("×", "*")      // ...pero la librería solo entiende * y /
            .replace("÷", "/")
        val resultat = evaluator.evaluate(expressioNeta)
        textResultat.text = formatNumber.format(resultat)
    } catch (e: Exception) {
        textResultat.text = getString(R.string.error_expressio)   // ← esta línea la añadí yo
    }
}
```

- Si escribes `3++` o `(2`, la librería lanza una **excepción**. Sin el `catch`, la app se cierra; con él, sale "Error".
- `5 ÷ 0` **no** peta: con `Double` el resultado es `Infinity` (∞).
- `DecimalFormat("#.##########")` hace que `5.0` se muestre como `5`.

---

## La clase `R`

Android genera `R` solo, a partir de la carpeta `res/`:

| En el XML | En Kotlin |
|---|---|
| `android:id="@+id/boto7"` | `R.id.boto7` |
| `res/layout/activity_main.xml` | `R.layout.activity_main` |
| `<string name="boto_7">` | `R.string.boto_7` |

- `@+id/` (con `+`) **crea** un id nuevo.
- `@id/` (sin `+`) **usa** uno que ya existe.

## Preguntas de repaso

1. ¿Cómo das el mismo comportamiento a 10 botones? → `mapOf(id to valor)` + `for`.
2. ¿Para qué sirve `parent` en un estilo? → Para heredar los atributos de otro estilo.
3. ¿Por qué se hace `replace("×", "*")`? → Porque JAvaluator no entiende los símbolos bonitos.
4. ¿Qué pasa sin el `catch`? → Con una expresión mal escrita, la app se cierra.
5. ¿Por qué el orden importa en el `GridLayout`? → Porque los botones se colocan en el orden en que aparecen en el XML.
