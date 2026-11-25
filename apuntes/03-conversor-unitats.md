# 03 · Práctica 1B — Conversor de unidades

📁 Código: `practicas/03-conversor-unitats/`
📄 Enunciado original: `~/Baixades/enunciat-prac1b.html`

## ¿Qué hace la app?

- Una **pantalla de menú** con 3 botones: Temperatura, Longitud y Masa.
- Cada botón abre **otra pantalla** que convierte unidades (°C/°F/K, km/mi/yd, kg/lb/oz).
- Está en **3 idiomas** (catalán, castellano, inglés) y tiene **modo oscuro**.

## Qué venía hecho y qué hice yo

| Ya venía hecho | Lo hice yo |
|---|---|
| Gradle y dependencias | Menú con 3 botones + navegación con **Intent** |
| `MainActivity.kt` vacía con TODO | Las 3 pantallas: layouts + Activities con los cálculos |
| `strings.xml` en catalán (con todas las claves) | Declarar las 3 Activities en el **manifest** |
| `colors.xml` y `themes.xml` del modo claro | Traducciones: `values-es/` y `values-en/` |
| | Modo oscuro: `values-night/colors.xml` y `themes.xml` |

---

## Lo que aprendí a hacer (código explicado)

### 1 · Abrir otra pantalla: Intent explícito ⭐

```kotlin
// MainActivity.kt
findViewById<Button>(R.id.botoTemperatura).setOnClickListener {
    startActivity(Intent(this, TemperaturaActivity::class.java))
}
```

- `Intent(this, Destino::class.java)` quiere decir "desde **esta** pantalla, abre **Destino**".
- Es **explícito** porque dices exactamente qué Activity abrir (una de tu app).
- `startActivity(...)` abre la pantalla nueva **encima** de la otra. El botón Atrás vuelve al menú.

### 2 · Declarar las Activities en el manifest ⭐

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".TemperaturaActivity"
    android:exported="false"
    android:label="@string/temperatura_titol" />

<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

- **Una Activity que no está en el manifest no se puede abrir**: la app peta con `ActivityNotFoundException`.
- `MAIN` + `LAUNCHER` marcan la pantalla que se abre al tocar el icono.
- `exported="false"` significa que solo la puede abrir tu propia app.
- `label` es el título que aparece arriba.

### 3 · Layout con `RadioGroup` (elegir solo una opción)

```xml
<RadioGroup android:id="@+id/radioGroupDe" ...>
    <RadioButton android:id="@+id/radioCelsius"    android:text="@string/celsius" android:checked="true"/>
    <RadioButton android:id="@+id/radioFahrenheit" android:text="@string/fahrenheit"/>
    <RadioButton android:id="@+id/radioKelvin"     android:text="@string/kelvin"/>
</RadioGroup>
```

Dentro de un `RadioGroup` **solo puede haber uno marcado**. En Kotlin sabes cuál con:

```kotlin
val de = radioGroupDe.checkedRadioButtonId    // devuelve el id del que está marcado
```

Hay dos grupos: **"De"** (origen) y **"A"** (destino). Por eso los ids acaban en `A`: `radioCelsiusA`.

### 4 · El botón "Convertir": validar paso a paso ⭐

Orden que pedía el enunciado: **vacío → no numérico → misma unidad → calcular**.

```kotlin
findViewById<Button>(R.id.botoConvertir).setOnClickListener {

    // 1. ¿Está vacío?
    val text = editValor.text.toString().trim()
    if (text.isEmpty()) {
        Toast.makeText(this, R.string.error_buid, Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }

    // 2. ¿Es un número?
    val valor = text.toDoubleOrNull()
    if (valor == null) {
        Toast.makeText(this, R.string.error_numeric, Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }

    // 3. ¿Ha elegido la misma unidad en los dos lados?
    val de = radioGroupDe.checkedRadioButtonId
    val a  = radioGroupA.checkedRadioButtonId
    val mateixaUnitat = (de == R.id.radioCelsius && a == R.id.radioCelsiusA) || ...
    if (mateixaUnitat) {
        Toast.makeText(this, R.string.error_unitats, Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }

    // 4. Calcular (ver el siguiente punto)
}
```

| Cosa | Qué hace |
|---|---|
| `.trim()` | Quita los espacios del principio y del final |
| `toDoubleOrNull()` | Convierte a número; si no puede (`"abc"`), da `null` **en lugar de petar** |
| `Toast.makeText(...).show()` | Mensaje corto que aparece abajo unos segundos. ¡No olvides `.show()`! |
| `return@setOnClickListener` | Sale **solo del listener**. Dentro de `{ }` no se puede poner `return` a secas |

### 5 · Convertir pasando por una unidad base ⭐

Primero paso **todo a una unidad base** (Celsius) y después **de la base al destino**:

```kotlin
val celsius = when (de) {                        // origen → base
    R.id.radioCelsius    -> valor
    R.id.radioFahrenheit -> (valor - 32.0) * 5.0 / 9.0
    R.id.radioKelvin     -> valor - 273.15
    else -> throw IllegalArgumentException()
}
val resultat = when (a) {                        // base → destino
    R.id.radioCelsiusA    -> celsius
    R.id.radioFahrenheitA -> celsius * 9.0 / 5.0 + 32.0
    R.id.radioKelvinA     -> celsius + 273.15
    else -> throw IllegalArgumentException()
}
```

- **¿Por qué así?** Con 3 unidades habría que escribir una fórmula para cada pareja (6). Pasando por la base solo hacen falta 2 por unidad, y es más fácil añadir unidades.
- `when` es como un `switch`, pero **devuelve un valor**.
- Las bases son **Celsius** en Temperatura, **km** en Longitud y **kg** en Masa.
- Todo va dentro de un `try/catch`. Si algo falla, sale un Toast con `error_generic`.

### 6 · Mostrar el resultado con formato

```kotlin
textResultat.text = String.format(Locale.US, "%.2f %s", resultat, unitat)
// → "25.50 °C"
```

- `%.2f` es un número con 2 decimales (Longitud y Masa usan `%.4f`). `%s` es texto.
- `Locale.US` hace que el separador decimal sea siempre un **punto**, aunque el móvil esté en catalán (que usaría coma).

### 7 · Idiomas: carpetas `values-xx` ⭐

```
res/values/strings.xml       → catalán (por defecto)
res/values-es/strings.xml    → castellano
res/values-en/strings.xml    → inglés
```

```xml
<!-- values/strings.xml -->         <string name="error_buid">Introdueix un valor</string>
<!-- values-es/strings.xml -->      <string name="error_buid">Introduce un valor</string>
<!-- values-en/strings.xml -->      <string name="error_buid">Enter a value</string>
```

- **Mismo `name`** en los tres archivos y solo cambia el texto.
- Android elige la carpeta él solo según el idioma del móvil. En el código siempre usas `R.string.error_buid`.
- Si falta una traducción, usa la de `values/` (la de por defecto).

### 8 · Modo oscuro: carpeta `values-night`

```
res/values/colors.xml          → colores del modo claro
res/values-night/colors.xml    → MISMOS nombres, valores oscuros
res/values-night/themes.xml    → el tema para el modo oscuro
```

```xml
<!-- values/colors.xml -->        <color name="background">#FFFEF7FF</color>
<!-- values-night/colors.xml -->  <color name="background">#FF1D1B20</color>
```

- Funciona igual que los idiomas: **mismo nombre, otro valor**, y Android elige.
- Por eso **nunca** se pone `#RRGGBB` a mano en un layout: si lo haces, ese color no cambiará en modo oscuro.
- Para probarlo en el emulador: Settings → Display → Dark theme.

---

## Preguntas de repaso

1. ¿Cómo abres otra pantalla? → `startActivity(Intent(this, Otra::class.java))`.
2. ¿Qué pasa si olvidas declararla en el manifest? → Crash (`ActivityNotFoundException`).
3. ¿Qué diferencia hay entre `toDouble()` y `toDoubleOrNull()`? → El primero peta con `"abc"`; el segundo devuelve `null`.
4. ¿Por qué `return@setOnClickListener` y no `return`? → Porque estás dentro de una lambda.
5. ¿Cómo traduces la app? → Con carpetas `values-es`, `values-en`... con los mismos `name`.
6. ¿Por qué `Locale.US` en el `format`? → Para que el decimal sea siempre un punto.
