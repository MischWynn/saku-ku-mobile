package com.example.kotlintest.core.notification

interface AppNotifier {
     fun show(notification: AppNotification): Int

        fun cancel(id: Int)
}