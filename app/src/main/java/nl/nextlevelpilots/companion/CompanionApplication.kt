package nl.nextlevelpilots.companion

import android.app.Application
import nl.nextlevelpilots.companion.notifications.NotificationHelper

class CompanionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
