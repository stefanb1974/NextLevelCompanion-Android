package nl.nextlevelpilots.companion.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CompanionFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "NextLevel Pilots"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: ""

        Log.d(TAG, "Message received")
        Log.d(TAG, "Title=$title")
        Log.d(TAG, "Body=$body")

        if (title.isBlank() && body.isBlank()) return

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
        )
    }

    companion object {
        private const val TAG = "CompanionFcmService"
    }
}
