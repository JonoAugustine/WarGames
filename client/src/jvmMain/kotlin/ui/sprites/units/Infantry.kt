package ui.sprites.units

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.Match.State.PLANNING
import com.jonoaugustine.wargames.common.ecs.GameState.PLACING
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.components.centeredOn
import com.jonoaugustine.wargames.common.ecs.gameState
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.network.missives.MoveUnit
import com.jonoaugustine.wargames.common.network.missives.SetUnitPath
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import state.AppState
import state.send
import ui.components.HoverBox
import util.composeColor
import util.dp

context(AppState, DefaultClientWebSocketSession)
@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
fun World.infantrySpriteOf(entity: Entity) {
  var dragPos by remember(entity) { mutableStateOf(entity[TransformCmpnt].position) }
  var movePreview by remember { mutableStateOf(Vector.ZERO) }
  val sprite = entity[SpriteCmpnt]
  val interactionSource = remember { MutableInteractionSource() }

  HoverBox(
    Modifier.offset(
      entity[TransformCmpnt].position.x.dp,
      entity[TransformCmpnt].position.y.dp
    )
      .size(sprite.size.dp)
      .rotate(entity[TransformCmpnt].rotation)
      .background(sprite.color.composeColor)
      .border(1.dp,
        if (selectedEntity == entity) Color.White
        else if (entity[CollisionCmpnt].colliding) Color.Yellow
        else Color.Black)
      .onClick(
        matcher = PointerMatcher.mouse(PointerButton.Primary),
        interactionSource = interactionSource,
        onClick = { selectedEntity = entity }
      )
      .onClick(
        matcher = PointerMatcher.mouse(PointerButton.Secondary),
        interactionSource = interactionSource
      ) {
        println("Right Click")
      }
      .pointerInput(entity, entity[TransformCmpnt], entity[SpriteCmpnt]) {
        if (world.gameState?.state == PLACING)
          detectDragGestures(
            onDragEnd = {
              val dragTo = Vector(
                movePreview.x + entity[TransformCmpnt].position.x,
                movePreview.y + entity[TransformCmpnt].position.y
              )
              GlobalScope.launch(Dispatchers.IO) { send(MoveUnit(entity.id, dragTo)) }
              movePreview = Vector.ZERO
            }
          ) { change, _ ->
            movePreview = Vector(
              change.position.x - sprite.size.width / 2,
              change.position.y - sprite.size.height / 2
            )
            dragPos = Vector(
              (change.position.x + entity[TransformCmpnt].position.x) - (sprite.size.width / 2),
              (change.position.y + entity[TransformCmpnt].position.y) - (sprite.size.height / 2),
            )
          }
      }
  ) { hovering ->
    Text(
      "${entity.id} ${entity[TransformCmpnt].position}",
      modifier = Modifier.offset(y = (sprite.size.height).dp),
      overflow = TextOverflow.Visible,
      fontSize = TextUnit(0.8f, TextUnitType.Em)
    )
    UnitEnsign(sprite)
    if (movePreview.x + movePreview.y != 0f) UnitEnsign(sprite, movePreview)

    // Show collisions
    //entity[CollisionCmpnt].hitboxes.forEach { (key, box) ->
    //  Box(
    //    Modifier.offset(box.offset.x.dp, box.offset.y.dp)
    //      .size(box.size.dp)
    //      .border(1.dp, Color.Yellow),
    //    content = { Text(key) }
    //  )
    //}

    // Path selector icon
    if (state.match!!.state == PLANNING) {
      val pathSelectorSize = Size(10f, 10f)
      Box(Modifier.clip(CircleShape)
        .background(Color.Black.copy(alpha = if (hovering) 0.7f else 0f))
        .size(pathSelectorSize.dp)
        .pointerInput(entity) { recordPath(entity.id, entity[TransformCmpnt], sprite) }
        // TODO path start point offset not working
        .offset(100.dp, 100.dp)
      )
    }
  }
}

@Composable
private fun UnitEnsign(sprite: SpriteCmpnt, offset: Vector = Vector()) {
  Canvas(Modifier.fillMaxSize().offset(offset.x.dp, offset.y.dp)) {
    drawPath(color = Color.Black, style = Stroke(1f), path = Path().apply {
      moveTo(0f, 0f)
      lineTo(sprite.size.width.toFloat(), sprite.size.height.toFloat())
      moveTo(0f, sprite.size.height.toFloat())
      lineTo(sprite.size.width.toFloat(), 0f)
    })
  }
}

context(DefaultClientWebSocketSession, PointerInputScope)
@OptIn(ExperimentalComposeUiApi::class, DelicateCoroutinesApi::class)
private suspend fun recordPath(
  eid: Int,
  transform: TransformCmpnt,
  sprite: SpriteCmpnt,
) {
  var path by mutableStateOf(listOf<Vector>())
  var initialOffset = Offset(0f, 0f)
  val center = transform centeredOn sprite
  detectDragGestures(
    onDragStart = { offset ->
      initialOffset = offset
      path = mutableStateListOf(Vector(center.x, center.y))
    },
    onDrag = { change, _ ->
      path = path + change.historical.map { it.position }.map {
        Vector(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      }.plus(change.position.let {
        Vector(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      })
    },
    onDragEnd = {
      GlobalScope.launch(Dispatchers.IO) { send(SetUnitPath(eid, path)) }
    })
}

