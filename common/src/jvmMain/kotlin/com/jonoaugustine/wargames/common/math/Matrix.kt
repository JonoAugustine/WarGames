package com.jonoaugustine.wargames.common.math

typealias FloatMatrix = Array<Array<Float>>

infix fun FloatMatrix.dot(other: FloatMatrix): FloatMatrix {
  require(columns == other.rows) { "Matrix dimensions are incompatible for dot product" }
  val result = Array(rows) { Array(other.columns) { 0f } }
  for (i in 0 until rows) {
    for (j in 0 until other.columns) {
      for (k in 0 until columns) {
        result[i][j] += this[i][k] * other[k][j]
      }
    }
  }
  return result
}

infix fun FloatMatrix.matrixAdd(other: FloatMatrix): FloatMatrix {
  val result = Array(rows) { Array(columns) { 0f } }
  for (i in 0 until rows) {
    for (j in 0 until columns) {
      result[i][j] = this[i][j] + other[i][j]
    }
  }
  return result
}

infix fun FloatMatrix.matrixSubtract(other: FloatMatrix): FloatMatrix {
  val result = Array(rows) { Array(columns) { 0f } }
  for (i in 0 until rows) {
    for (j in 0 until columns) {
      result[i][j] = this[i][j] - other[i][j]
    }
  }
  return result
}

val <T> Array<Array<T>>.rows get() = size
val <T> Array<Array<T>>.columns get() = first().size
