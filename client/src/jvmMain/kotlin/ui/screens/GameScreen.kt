package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import logic.Match
import logic.MatchState
import logic.models.BattleUnit
import ui.sprite
import util.Vector
import java.util.*
import kotlin.random.Random

context(Match)
@Composable
fun GameScreen() {
  Box(Modifier.fillMaxSize().background(background))
  if (state == MatchState.PLACING) PlacementLayer()
  Column {
    Button(
      enabled = state != MatchState.PLACING,
      onClick = {
        state = MatchState.PLACING
      }) { Text("Start Placing") }
    Button(
      enabled = state != MatchState.PLANNING,
      onClick = {
        state = MatchState.PLANNING
      }) { Text("Start Planning") }
    Button(
      colors = buttonColors(backgroundColor = Color.Green),
      enabled = state != MatchState.RUNNING,
      onClick = {
        state = MatchState.RUNNING
      }) { Text("Start Round") }
  }
  entities
    .filterIsInstance<BattleUnit>()
    .forEach { it.sprite() }
  PathLayer()
}

context(Match)
@Composable
fun PlacementLayer() {
  Box(
    Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures {
          val size = Size(if (Random.nextBoolean()) 50f else 100f, 50f)
          entities = entities + BattleUnit(
            id = UUID.randomUUID().toString(),
            position = Vector(it.x - size.width / 2, it.y - size.height / 2),
            size = size
          )
        }
      }
  )
}

context(Match)
@Composable
fun PathLayer() {
  Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
    entities.filterIsInstance<BattleUnit>()
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
