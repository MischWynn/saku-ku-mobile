package com.example.sakuku.core.notification


interface AppNotifier {
     fun show(notification: AppNotification): Int

        fun cancel(id: Int)
}