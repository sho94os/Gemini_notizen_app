package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.ui.NoteScreen
import com.example.ui.NoteViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val database by lazy { NoteDatabase.getDatabase(this) }
  private val repository by lazy { NoteRepository(database.noteDao()) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val viewModel = ViewModelProvider(
      this, 
      NoteViewModel.Factory(repository)
    )[NoteViewModel::class.java]

    setContent {
      MyApplicationTheme {
        NoteScreen(viewModel = viewModel)
      }
    }
  }
}
