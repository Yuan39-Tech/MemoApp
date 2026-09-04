package com.example.memo.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.memo.notification.ReminderWorker
import com.example.memo.widget.MemoWidgetProvider
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class NoteRepository(context: Context, private val noteDao: NoteDao) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)
    val notes: Flow<List<Note>> = noteDao.observeAll()

    suspend fun save(id: Long, title: String, content: String, reminderAt: Long?): Long {
        val note = Note(
            id = id,
            title = title.trim(),
            content = content.trim(),
            reminderAt = reminderAt
        )
        val noteId = if (id == 0L) noteDao.insert(note) else {
            noteDao.update(note)
            id
        }
        scheduleReminder(noteId, reminderAt)
        MemoWidgetProvider.refresh(appContext)
        return noteId
    }

    suspend fun delete(note: Note) {
        workManager.cancelUniqueWork(workName(note.id))
        noteDao.delete(note)
        MemoWidgetProvider.refresh(appContext)
    }

    private fun scheduleReminder(noteId: Long, reminderAt: Long?) {
        val name = workName(noteId)
        workManager.cancelUniqueWork(name)
        if (reminderAt == null || reminderAt <= System.currentTimeMillis()) return

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(reminderAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_NOTE_ID to noteId))
            .build()
        workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(noteId: Long) = "note_reminder_$noteId"
}
