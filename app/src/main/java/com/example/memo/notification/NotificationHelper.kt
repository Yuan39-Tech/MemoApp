package com.example.memo.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.memo.data.Note

object NotificationHelper {
    private const val CHANNEL_ID = "note_reminders"

    fun show(context: Context, note: Note) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "笔记提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(note.title.ifBlank { "备忘录提醒" })
            .setContentText(note.content.ifBlank { "你有一条笔记提醒" })
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(note.id.toInt(), notification)
    }
}
