package ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import state.AppState
import state.Page
import java.util.*

context(AppState)
@Composable
fun BackButton(
  page: Page,
  text: String = page.name,
  modifier: Modifier = Modifier,
  colors: ButtonColors = buttonColors(Color.White, Color.Black),
) = Button(
  modifier = modifier.offset(10.dp, 10.dp),
  colors = buttonColors(
    backgroundColor = Color.White,
    contentColor = Color.Black
  ),
  onClick = { goTo(page) },
  content = { Text(text.lowercase(Locale.US)) }
)
