package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.Note
import com.example.ui.NoteCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleNote = Note(
      id = 1,
      title = "Urlaubsplanung",
      content = "Flug buchen, Packliste überprüfen, Hotels in Rom vergleichen und Tickets für das Kolosseum reservieren.",
      category = "Reise",
      colorHex = "#FFF9A6", // Pastel Yellow
      isPinned = true,
      updatedAt = 1716201600000L // static date
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = false) {
        NoteCard(
          note = sampleNote,
          isDark = false,
          onClick = {},
          onDelete = {},
          onPinToggle = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
