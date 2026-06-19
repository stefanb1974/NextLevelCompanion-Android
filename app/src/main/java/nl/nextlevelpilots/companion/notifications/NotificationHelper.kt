package nl.nextlevelpilots.companion.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import nl.nextlevelpilots.companion.MainActivity
import nl.nextlevelpilots.companion.R

object NotificationHelper {

    const val CHANNEL_ID = "nextlevel_pilots"
    const val CHANNEL_NAME = "NextLevel Pilots"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Meldingen van NextLevel Pilots"
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (title + body).hashCode(),
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(notificationId, notification)
    }
}
