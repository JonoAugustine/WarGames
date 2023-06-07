package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import logic.Game
import logic.GameState.PLANNING
import logic.GameState.RUNNING
import logic.models.BattleUnit
import ui.sprite

@Composable
fun GameScreen(game: Game) {
  Box(Modifier.fillMaxSize().background(game.background))
  Column {
    Button(
      enabled = game.state != PLANNING,
      onClick = {
        game.state = PLANNING
      }) {
      Text("Planning")
    }
    Button(
      enabled = game.state != RUNNING,
      onClick = {
        game.state = RUNNING
      }) {
      Text("Start Game")
    }
  }
  game.entities
    .filterIsInstance<BattleUnit>()
    .forEach { it.sprite() }
  PathLayer(game)
}

@Composable
fun PathLayer(game: Game) {
  Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
    game.entities.filterIsInstance<BattleUnit>()
      .forEach { bu ->
        if (bu.path.size <= 1) return@forEach
        drawPath(
          color = Color.Blue,
          style = Stroke(3f),
          path = Path().apply {
            bu.path.first().run { moveTo(x, y) }
            bu.path.subList(1, bu.path.size)
              .filterIndexed { index, _ -> index % 2 == 0 }
              .forEach { lineTo(it.x, it.y) }
            bu.path.last().let { last ->
              //draw X
              moveTo(last.x - 10, last.y - 10)
              lineTo(last.x + 10, last.y + 10)
              moveTo(last.x + 10, last.y - 10)
              lineTo(last.x - 10, last.y + 10)
              // draw outline
              val olxRoot = last.x - bu.size.width / 2
              val olyRoot = last.y - bu.size.height / 2
              moveTo(olxRoot, olyRoot)
              lineTo(olxRoot + bu.size.width, olyRoot)
              lineTo(olxRoot + bu.size.width, olyRoot + bu.size.height)
              lineTo(olxRoot, olyRoot + bu.size.height)
              lineTo(olxRoot, olyRoot)
            }
          }
        )
      }
  })
}
