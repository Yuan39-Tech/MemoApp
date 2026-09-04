package com.example.memo.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.memo.data.MemoDatabase

class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val noteId = inputData.getLong(KEY_NOTE_ID, 0L)
        val note = MemoDatabase.get(applicationContext).noteDao().getById(noteId)
            ?: return Result.success()
        NotificationHelper.show(applicationContext, note)
        return Result.success()
    }

    companion object {
        const val KEY_NOTE_ID = "note_id"
    }
}
