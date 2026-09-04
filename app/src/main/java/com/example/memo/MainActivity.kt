package com.example.memo

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memo.data.MemoDatabase
import com.example.memo.data.NoteRepository
import com.example.memo.ui.NotesScreen
import com.example.memo.ui.NotesViewModel
import com.example.memo.ui.NotesViewModelFactory

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val repository = NoteRepository(
            applicationContext,
            MemoDatabase.get(applicationContext).noteDao()
        )
        setContent {
            val vm: NotesViewModel = viewModel(factory = NotesViewModelFactory(repository))
            MaterialTheme { NotesScreen(vm) }
        }
    }
}
