package cat.institut.dam.contactes

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Notificacions — canal i avís de «contactes carregats».
 */
object Notificacions {

    private const val CANAL = "contactes_carregats"
    private const val ID_NOTIFICACIO = 1

    /** Crea el canal (Android 8+). Es pot cridar més d'una vegada sense problema. */
    fun creaCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL,
                context.getString(R.string.canal_notificacions),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    /** Mostra la notificació. Si l'usuari no ho permet, no fa res. */
    @SuppressLint("MissingPermission") // ja es comprova amb areNotificationsEnabled()
    fun contactesCarregats(context: Context, quantitat: Int) {
        val gestor = NotificationManagerCompat.from(context)
        if (!gestor.areNotificationsEnabled()) return
        creaCanal(context)
        val notificacio = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacio)
            .setContentTitle(context.getString(R.string.notificacio_titol))
            .setContentText(context.getString(R.string.notificacio_text, quantitat))
            .setAutoCancel(true)
            .build()
        gestor.notify(ID_NOTIFICACIO, notificacio)
    }
}
