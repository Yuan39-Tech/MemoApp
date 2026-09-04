package com.example.memo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.memo.MainActivity
import com.example.memo.R
import com.example.memo.data.MemoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { update(context, appWidgetManager, it) }
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, MemoWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    ids.forEach { update(context, manager, it) }
                }
            }
        }

        private suspend fun update(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val note = MemoDatabase.get(context).noteDao().getLatest()
            val launchIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.widget_memo).apply {
                setTextViewText(R.id.widget_title, note?.title?.ifBlank { "无标题笔记" } ?: "暂无笔记")
                setTextViewText(R.id.widget_content, note?.content?.ifBlank { "点击打开备忘录" } ?: "点击打开备忘录")
                setOnClickPendingIntent(R.id.widget_root, launchIntent)
            }
            manager.updateAppWidget(widgetId, views)
        }
    }
}
