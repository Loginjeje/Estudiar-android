// Fitxer de configuració de Gradle a nivell d'arrel del projecte.
// Aquí només es declaren els plugins que podran aplicar-se als mòduls (app),
// sense aplicar-los encara. Les versions provenen del Version Catalog
// (gradle/libs.versions.toml).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}